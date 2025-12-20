import dotenv from 'dotenv';
dotenv.config({ path: './audiobook-backend/.env' });
import { GoogleGenAI } from "@google/genai";

// Use env vars so the key isn't hardcoded. Set GOOGLE_API_KEY or GEMINI_API_KEY in your .env.
const API_KEY = process.env.GOOGLE_API_KEY || process.env.GEMINI_API_KEY;
const MODEL = process.env.GOOGLE_MODEL || 'gemini-2.5-flash';

if (!API_KEY) {
  console.error('No API key found. Set GOOGLE_API_KEY or GEMINI_API_KEY in .env to run this test.');
  process.exit(1);
}

const ai = new GoogleGenAI({ apiKey: API_KEY });

async function main() {
  try {
    const response = await ai.models.generateContent({
      model: MODEL,
      contents: "Explain how AI works in a few words",
    });

    // Some SDK responses expose `.text`, others use different fields — be defensive.
    const text = response?.text ?? response?.output ?? response?.outputText ?? JSON.stringify(response);
    console.log(text);
  } catch (err) {
    console.error('AI call failed:', err?.response?.data || err?.message || err);
  }
}

main();