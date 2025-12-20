import '../config/env.js';

console.log('GOOGLE_API_KEY present?', !!process.env.GOOGLE_API_KEY);
console.log('GEMINI_API_KEY present?', !!process.env.GEMINI_API_KEY);
console.log('GEMINI_API_ENDPOINT present?', !!process.env.GEMINI_API_ENDPOINT);
console.log('All env keys:', {
  GOOGLE_API_KEY: process.env.GOOGLE_API_KEY ? '***' : null,
  GEMINI_API_KEY: process.env.GEMINI_API_KEY ? '***' : null,
  GEMINI_API_ENDPOINT: process.env.GEMINI_API_ENDPOINT || null
});
