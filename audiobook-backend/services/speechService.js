import axios from 'axios';

const REGION = process.env.AZURE_SPEECH_REGION;
const KEY = process.env.AZURE_SPEECH_KEY;
const LANG = process.env.AZURE_SPEECH_LANGUAGE || 'vi-VN';

export async function transcribeBuffer(buffer, contentType = 'audio/webm') {
  if (!REGION || !KEY) throw new Error('Azure Speech config missing in env');

  const url = `https://${REGION}.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=${encodeURIComponent(LANG)}`;

  try {
    const res = await axios.post(url, buffer, {
      headers: {
        'Ocp-Apim-Subscription-Key': KEY,
        'Content-Type': contentType
      },
      timeout: 60000
    });

    const data = res.data || {};

    // Common shapes: { RecognitionStatus: 'Success', DisplayText: '...' } or { DisplayText: '...' }
    if (data.DisplayText) return String(data.DisplayText).trim();
    if (data.RecognitionStatus === 'Success' && data.NBest && data.NBest[0] && data.NBest[0].Display) return String(data.NBest[0].Display).trim();

    // Some proxies return a string directly
    if (typeof data === 'string' && data.length) return data;

    // Try to find text in nested object
    const found = (function findText(obj) {
      if (!obj) return null;
      if (typeof obj === 'string') return obj;
      if (obj.DisplayText) return obj.DisplayText;
      if (obj.NBest && Array.isArray(obj.NBest) && obj.NBest[0] && obj.NBest[0].Display) return obj.NBest[0].Display;
      for (const k of Object.keys(obj)) {
        try {
          const v = findText(obj[k]);
          if (v) return v;
        } catch (e) {}
      }
      return null;
    })(data);

    if (found) return String(found).trim();

    throw new Error('No transcription returned');
  } catch (err) {
    console.error('Azure STT failed:', err?.response?.data || err?.message);
    throw new Error('Lỗi khi gọi Azure Speech-to-Text');
  }
}
