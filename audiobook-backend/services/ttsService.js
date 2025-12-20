import axios from "axios";

const REGION = process.env.AZURE_SPEECH_REGION;
const KEY = process.env.AZURE_SPEECH_KEY;
const VOICE = process.env.AZURE_VOICE_NAME || "vi-VN-HoaiMyNeural"; // default

export async function synthesizeToMp3(text, voice = VOICE) {
  if (!REGION || !KEY) throw new Error("Azure Speech config missing in env");

  const ssml = `<speak version='1.0' xml:lang='vi-VN'><voice name='${voice}'>${escapeXml(text)}</voice></speak>`;

  try {
    const url = `https://${REGION}.tts.speech.microsoft.com/cognitiveservices/v1`;
    const res = await axios.post(url, ssml, {
      headers: {
        'Ocp-Apim-Subscription-Key': KEY,
        'Content-Type': 'application/ssml+xml',
        'X-Microsoft-OutputFormat': 'audio-16khz-128kbitrate-mono-mp3'
      },
      responseType: 'arraybuffer',
      timeout: 30000
    });

    return Buffer.from(res.data);
  } catch (err) {
    // Chuyển Buffer lỗi sang chữ để đọc
    if (err.response && err.response.data instanceof Buffer) {
        const errorString = Buffer.from(err.response.data).toString();
        console.error('Azure TTS Error Detail:', errorString);
    } else {
        console.error('Azure TTS failed:', err.message);
    }
    throw new Error('Lỗi khi gọi Azure TTS');
}
}

function escapeXml(unsafe) {
  return String(unsafe)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}
