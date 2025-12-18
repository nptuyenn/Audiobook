import '../config/env.js';
import { generateStory } from '../services/geminiService.js';

(async () => {
  try {
    const text = await generateStory('Chủ đề thử nghiệm');
    console.log('Generated text length:', text && text.length);
  } catch (err) {
    console.error('generateStory error:', err.message || err);
  }
})();