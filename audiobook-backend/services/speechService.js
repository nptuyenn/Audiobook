import axios from 'axios';
import fs from 'fs';
import path from 'path';
import stream from 'stream';
import ffmpeg from 'fluent-ffmpeg';
import ffmpegInstaller from '@ffmpeg-installer/ffmpeg';

ffmpeg.setFfmpegPath(ffmpegInstaller.path);

const REGION = process.env.AZURE_SPEECH_REGION;
const KEY = process.env.AZURE_SPEECH_KEY;
const LANG = process.env.AZURE_SPEECH_LANGUAGE || 'vi-VN';

/**
 * Converts an audio buffer from any format to a 16kHz WAV buffer using ffmpeg.
 * @param {Buffer} inputBuffer The audio buffer to convert.
 * @returns {Promise<Buffer>} A promise that resolves with the WAV audio buffer.
 */
function convertAudioToWav(inputBuffer) {
  return new Promise((resolve, reject) => {
    const inputSteam = new stream.PassThrough();
    inputSteam.end(inputBuffer);

    const outputStream = new stream.PassThrough();
    const chunks = [];

    outputStream.on('data', (chunk) => {
      chunks.push(chunk);
    });

    outputStream.on('end', () => {
      resolve(Buffer.concat(chunks));
    });

    ffmpeg(inputSteam)
      .toFormat('wav')
      .audioFrequency(16000)
      .audioChannels(1)
      .on('error', (err) => {
        console.error('ffmpeg error:', err);
        reject(new Error('Failed to convert audio to WAV.'));
      })
      .pipe(outputStream, { end: true });
  });
}

export async function transcribeBuffer(buffer, contentType = 'audio/wav; codecs=audio/pcm; samplerate=16000') {
  if (!REGION || !KEY) throw new Error('Azure Speech config missing in env');

  try {
    // --- START DEBUG CODE ---
    try {
      const extension = contentType.split('/')[1]?.split(';')[0] || 'raw';
      const debugFilename = `debug_audio_before_convert_${Date.now()}.${extension}`;
      const debugFilePath = path.join(process.cwd(), debugFilename);
      fs.writeFileSync(debugFilePath, buffer);
      console.log(`[DEBUG] Original audio buffer saved to ${debugFilePath}. Size: ${buffer.length} bytes. Content-Type: ${contentType}`);
    } catch (e) {
      console.error(`[DEBUG] Failed to save debug audio file.`, e);
    }
    // --- END DEBUG CODE ---

    console.log('[STT] Converting audio buffer to 16kHz WAV format...');
    const wavBuffer = await convertAudioToWav(buffer);
    console.log(`[STT] Conversion successful. New buffer size: ${wavBuffer.length} bytes.`);

    const azureContentType = 'audio/wav; codecs=audio/pcm; samplerate=16000';
    const url = `https://${REGION}.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=${encodeURIComponent(LANG)}`;

    const res = await axios.post(url, wavBuffer, {
      headers: {
        'Ocp-Apim-Subscription-Key': KEY,
        'Content-Type': azureContentType
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
    if (err.message.includes('Failed to convert audio')) {
       throw new Error('Lỗi khi chuyển đổi định dạng âm thanh. Vui lòng thử lại với định dạng khác.');
    }
    throw new Error('Lỗi khi gọi Azure Speech-to-Text');
  }
}