import { generateStory } from "../services/geminiService.js";
import { synthesizeToMp3 } from "../services/ttsService.js";
import { uploadBuffer } from "../services/cloudinaryService.js";
import AIContent from "../models/AIContent.js";

export async function createStory(req, res) {
  const { topic, title } = req.body;
  if (!topic) return res.status(400).json({ message: "Topic is required" });

  try {
    console.log('createStory env -> GOOGLE_API_KEY:', !!process.env.GOOGLE_API_KEY, 'GEMINI_KEY:', !!process.env.GEMINI_API_KEY, 'GEMINI_ENDPOINT:', !!process.env.GEMINI_API_ENDPOINT);
    const text = await generateStory(topic);
    const audioBuffer = await synthesizeToMp3(text);

    // Return text + audio as base64 so Android can play immediately
    return res.json({
      text,
      audioBase64: audioBuffer.toString('base64'),
      audioMime: 'audio/mpeg'
    });
  } catch (err) {
    console.error('createStory error:', err);
    res.status(500).json({ message: err.message || 'Server error' });
  }
}

export async function saveStory(req, res) {
  // Accept title, text and audioBase64 from client OR re-generate if not provided
  const { title, text, audioBase64 } = req.body;
  const userId = req.user?.id;

  if (!text || !audioBase64) return res.status(400).json({ message: 'text and audioBase64 are required' });

  try {
    const buffer = Buffer.from(audioBase64, 'base64');
    const uploadRes = await uploadBuffer(buffer, { folder: 'ai-stories' });

    const aiContent = new AIContent({
      userId,
      title: title || `Truyện: ${new Date().toISOString()}`,
      storyText: text,
      audioUrl: uploadRes.secure_url
    });

    await aiContent.save();

    // Also create an Audio asset record
    try {
      const Audio = (await import('../models/Audio.js')).default;
      const audioDoc = new Audio({
        userId,
        title: aiContent.title,
        audioUrl: uploadRes.secure_url,
        sizeBytes: uploadRes.bytes || uploadRes.size || null,
        voice: process.env.AZURE_VOICE_NAME || 'HoaiMy'
      });
      await audioDoc.save();
    } catch (err) {
      console.warn('Audio doc creation failed:', err.message || err);
    }

    res.json({ message: 'Saved', aiContent });
  } catch (err) {
    console.error('saveStory error:', err);
    res.status(500).json({ message: 'Upload or save failed' });
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
