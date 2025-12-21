import { generateStory } from "../services/geminiService.js";
import { synthesizeToMp3 } from "../services/ttsService.js";
import { transcribeBuffer } from "../services/speechService.js";
import { uploadBuffer } from "../services/cloudinaryService.js";
import AIContent from "../models/AIContent.js";

export async function createStory(req, res) {
  const { topic, title } = req.body;
  if (!topic) return res.status(400).json({ message: "Topic is required" });

  try {
    console.log('createStory env -> GOOGLE_API_KEY:', !!process.env.GOOGLE_API_KEY, 'GEMINI_KEY:', !!process.env.GEMINI_API_KEY, 'GEMINI_ENDPOINT:', !!process.env.GEMINI_API_ENDPOINT);
    const text = await generateStory(topic);
    const audioBuffer = await synthesizeToMp3(text);

    const uploadRes = await uploadBuffer(audioBuffer, { folder: 'ai-stories' });

    return res.json({
      text,
      audioUrl: uploadRes.secure_url,
      audioMime: 'audio/mpeg'
    });

  } catch (err) {
    console.error('createStory error:', err);
    res.status(500).json({ message: err.message || 'Server error' });
  }
}

export async function createStoryFromSpeech(req, res) {
  const { audioBase64, audioMime, title } = req.body;
  if (!audioBase64) return res.status(400).json({ message: "audioBase64 is required" });

  try {
    const audioBuffer = Buffer.from(audioBase64, 'base64');
    console.log('createStoryFromSpeech -> transcribing audio, mime:', audioMime);
    const transcript = await transcribeBuffer(audioBuffer, audioMime);

    if (!transcript) return res.status(400).json({ message: 'No speech recognized' });

    const text = await generateStory(transcript);
    const outputAudio = await synthesizeToMp3(text);

    const uploadRes = await uploadBuffer(outputAudio, { folder: 'ai-stories' });

      return res.json({
        transcript,
        text,
        audioUrl: uploadRes.secure_url,
        audioMime: 'audio/mpeg'
      });

  } catch (err) {
    console.error('createStoryFromSpeech error:', err);
    res.status(500).json({ message: err.message || 'Server error' });
  }
}

export async function saveStory(req, res) {
  const { title, text, audioUrl } = req.body;
  const userId = req.user?.id;

  if (!text || !audioUrl) return res.status(400).json({ message: 'text and audioUrl are required' });

  try {
    const aiContent = new AIContent({
      userId,
      title: title || `Truyện: ${new Date().toISOString()}`,
      storyText: text,
      audioUrl
    });

    await aiContent.save();

    // Tạo record Audio
    try {
      const Audio = (await import('../models/Audio.js')).default;
      const audioDoc = new Audio({
        userId,
        title: aiContent.title,
        audioUrl,
        voice: process.env.AZURE_VOICE_NAME || 'HoaiMy'
      });
      await audioDoc.save();
    } catch (err) {
      console.warn('Audio doc creation failed:', err.message || err);
    }

    res.json({ message: 'Saved', aiContent });
  } catch (err) {
    console.error('saveStory error:', err);
    res.status(500).json({ message: 'Save failed' });
  }
}


export async function chat(req, res) {
  const { message, history } = req.body;
  if (!message) return res.status(400).json({ message: 'message is required' });

  try {
    const systemPrompt = `Bạn là Gấu Nhỏ, trợ lý thân thiện của một ứng dụng Audiobook cho bé: trả lời ngắn gọn, dễ hiểu, an toàn, và khuyến khích khám phá.`;
    const prompt = `${systemPrompt}\n\nLịch sử: ${JSON.stringify(history || [])}\n\nCâu hỏi: ${message}`;
    const answer = await generateStory(prompt, systemPrompt); // reuse generateStory for simple chat

    res.json({ text: answer });
  } catch (err) {
    console.error('chat error:', err);
    res.status(500).json({ message: 'Chat failed' });
  }
}
