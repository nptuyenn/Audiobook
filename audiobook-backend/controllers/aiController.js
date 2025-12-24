import { generateStory } from "../services/geminiService.js";
import { generateGroqChatCompletion } from "../services/groqService.js";
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
  const { audioBase64, audioMime } = req.body;
  if (!audioBase64) return res.status(400).json({ message: "audioBase64 is required" });

  console.log('[STT] Received request to transcribe voice.');
  console.log(`[STT] Request details - audioMime: ${audioMime}, audio length (base64): ${audioBase64.length}`);

  try {
    console.log('[STT] Step 1: Decoding base64 audio...');
    const audioBuffer = Buffer.from(audioBase64, 'base64');
    console.log(`[STT] Step 1 SUCCESS. Audio buffer size: ${audioBuffer.length} bytes.`);
    
    console.log(`[STT] Step 2: Calling speech-to-text service...`);
    const transcript = await transcribeBuffer(audioBuffer, audioMime);

    if (!transcript) {
      console.warn('[STT] Step 2 FAILED. Transcription failed. No speech was recognized.');
      return res.status(400).json({ message: 'No speech recognized' });
    }

    console.log(`[STT] Step 2 SUCCESS. Transcription successful. Text: "${transcript}"`);
    console.log('[STT] Process complete. Sending transcribed text back to client.');
    
    // Chỉ trả về văn bản đã được chuyển đổi
    return res.json({ transcribedText: transcript });

  } catch (err) {
    console.error('[STT] An error occurred in the transcription process:', err);
    res.status(500).json({ message: err.message || 'Server error' });
  }
}

export async function transcribeSpeech(req, res) {
  const { audioBase64, audioMime } = req.body;
  if (!audioBase64) return res.status(400).json({ message: "audioBase64 is required" });

  console.log('[STT-Only] Received request for transcription.');
  console.log(`[STT-Only] Request details - audioMime: ${audioMime}, audio length (base64): ${audioBase64.length}`);

  try {
    console.log('[STT-Only] Step 1: Decoding base64 audio...');
    const audioBuffer = Buffer.from(audioBase64, 'base64');
    console.log(`[STT-Only] Step 1 SUCCESS. Audio buffer size: ${audioBuffer.length} bytes.`);
    
    console.log(`[STT-Only] Step 2: Calling speech-to-text service...`);
    const transcript = await transcribeBuffer(audioBuffer, audioMime);

    if (!transcript) {
      console.warn('[STT-Only] Step 2 FAILED. Transcription failed. No speech was recognized.');
      return res.status(400).json({ message: 'No speech recognized' });
    }

    console.log(`[STT-Only] Step 2 SUCCESS. Transcription successful. Text: "${transcript}"`);
    
    return res.json({ transcribedText: transcript });

  } catch (err) {
    console.error('[STT-Only] An error occurred in transcribeSpeech process:', err);
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
    // Gọi đến service mới của Groq
    const answer = await generateGroqChatCompletion(message, history); 
    res.json({ text: answer });
  } catch (err) {
    console.error('Groq chat error:', err);
    res.status(500).json({ message: err.message || 'Chat failed' });
  }
}