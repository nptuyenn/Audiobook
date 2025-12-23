import axios from "axios";
import '../config/env.js';
import fs from 'fs/promises';
const REGION = process.env.AZURE_SPEECH_REGION;
const KEY = process.env.AZURE_SPEECH_KEY;
const VOICE = process.env.AZURE_VOICE_NAME || "vi-VN-HoaiMyNeural"; // default

export async function synthesizeToMp3(text, voice = VOICE) {
  console.log("--- Starting Azure TTS Synthesis ---");
  if (!REGION || !KEY) {
    console.error("Azure Speech config missing in env. REGION or KEY is not set.");
    throw new Error("Azure Speech config missing in env");
  }

  const ssml = `<speak version='1.0' xml:lang='vi-VN'><voice name='${voice}'>${escapeXml(text)}</voice></speak>`;
  
  console.log(`Region: ${REGION}, Voice: ${voice}`);
  console.log(`Text length: ${text.length} characters.`);
  // console.log(`SSML: ${ssml}`); // Uncomment for deep debugging

  try {
    const url = `https://${REGION}.tts.speech.microsoft.com/cognitiveservices/v1`;
    const res = await axios.post(url, ssml, {
      headers: {
        'Ocp-Apim-Subscription-Key': KEY,
        'Content-Type': 'application/ssml+xml',
        'X-Microsoft-OutputFormat': 'audio-16khz-128kbitrate-mono-mp3'
      },
      responseType: 'arraybuffer',
      timeout: 30000 // 30 second timeout
    });

    console.log(`Azure TTS synthesis successful. Received ${res.data.length} bytes of audio data.`);
    console.log("--- Finished Azure TTS Synthesis ---");
    return Buffer.from(res.data);
  } catch (err) {
    console.error("--- Azure TTS Synthesis FAILED ---");
    if (err.response) {
      // Request was made and server responded with a non-2xx status code
      console.error(`Error Status: ${err.response.status}`);
      console.error(`Error Headers: ${JSON.stringify(err.response.headers, null, 2)}`);
      
      if (err.response.data instanceof Buffer) {
          const errorString = Buffer.from(err.response.data).toString();
          console.error('Azure TTS Error Detail:', errorString);
      } else {
        console.error('Azure TTS Error Detail:', err.response.data);
      }
    } else if (err.request) {
      // Request was made but no response was received
      console.error("Azure TTS Error: No response was received from the server.", err.message);
    } else if (err.code === 'ECONNABORTED') {
      console.error("Azure TTS Error: Connection timed out. The request took longer than the 30-second timeout.");
    }
    else {
      // Something else happened
      console.error('Azure TTS Error: An unexpected error occurred during setup.', err.message);
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
