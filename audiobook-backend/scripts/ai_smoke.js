import { generateStory } from '../services/geminiService.js';
import { synthesizeToMp3 } from '../services/ttsService.js';

const run = async () => {
  if (!((process.env.GOOGLE_API_KEY || process.env.GEMINI_API_KEY)) || !process.env.AZURE_SPEECH_KEY) {
    console.log('Skipping smoke test: set GOOGLE_API_KEY or GEMINI_API_KEY, and AZURE_SPEECH_KEY in .env to run actual calls.');
    return;
  }

  try {
    const text = await generateStory('Tình bạn');
    console.log('Generated text length:', text.length);
    const audio = await synthesizeToMp3(text.slice(0, 200));
    console.log('Synthesized bytes:', audio.length);
  } catch (err) {
    console.error('Smoke failed:', err.message || err);
  }
};

run();
