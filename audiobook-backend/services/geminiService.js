import axios from 'axios';

const defaultSystemPrompt = `Bạn là nhà văn chuyên viết truyện thiếu nhi, hãy viết một câu chuyện ngắn dành cho bé, giọng văn thân thiện, đơn giản và nhiều hình ảnh cảm xúc. Độ dài ~300-700 từ. Trả về chỉ phần nội dung truyện.`;

export async function generateStory(topic, systemPrompt = defaultSystemPrompt) {
  const prompt = `${systemPrompt}\n\nChủ đề: ${topic}`;

  // Read env vars at call-time (ensures values are available even if loaded later)
  const GOOGLE_API_KEY = process.env.GOOGLE_API_KEY;
  const GOOGLE_MODEL = process.env.GOOGLE_MODEL || 'models/gemini-2.5-flash';
  const GEMINI_ENDPOINT = process.env.GEMINI_API_ENDPOINT;
  const GEMINI_KEY = process.env.GEMINI_API_KEY;

  // Debug: log presence (avoid printing actual keys)
  console.log('generateStory called -> GOOGLE_API_KEY:', !!GOOGLE_API_KEY, 'GEMINI_KEY:', !!GEMINI_KEY, 'GEMINI_ENDPOINT:', !!GEMINI_ENDPOINT);

  // Preferred: use Google Generative API via API key
  if (GOOGLE_API_KEY) {
    // Support both 'models/gemini-2.0-flash' and 'gemini-2.0-flash' values in GOOGLE_MODEL
    const GOOGLE_MODEL_RAW = process.env.GOOGLE_MODEL || 'gemini-2.5-flash';
    const modelName = GOOGLE_MODEL_RAW.replace(/^models\//, '');
    const GOOGLE_BASE = process.env.GOOGLE_API_ENDPOINT || 'https://generativelanguage.googleapis.com';
    const url = `${GOOGLE_BASE}/v1beta/models/${modelName}:generateContent?key=${GOOGLE_API_KEY}`;
    try {
      const payload = {
        contents: [
          {
            parts: [
              { text: prompt }
            ]
          }
        ],
        // Use snake_case field names required by the v1beta generateContent API
        generationConfig:{
        maxOutputTokens: 800,
        temperature: 0.2,
        candidateCount: 1}
      };

      const res = await axios.post(url, payload, {
        headers: {
          'Content-Type': 'application/json',
          'X-Goog-Api-Key': GOOGLE_API_KEY
        },
        timeout: 30000
      });

      const data = res.data || {};
      let text = '';

      if (Array.isArray(data.candidates) && data.candidates.length) {
        const cand = data.candidates[0];
        if (Array.isArray(cand.content)) {
          text = cand.content.map(c => c.text || '').join('');
        } else if (cand.output) {
          text = cand.output;
        } else if (cand.text) {
          text = cand.text;
        }
      }

      if (!text) {
        text = data.output_text || data.output || data.text || JSON.stringify(data);
      }

      return String(text).trim();
    } catch (err) {
      console.error('Google Generative API failed:', {
        message: err?.message,
        status: err?.response?.status,
        statusText: err?.response?.statusText,
        data: err?.response?.data
      });
      throw new Error(`Lỗi khi gọi Google Generative API: ${err?.response?.status || err?.message}`);
    }
  }

  // Fallback: use configured GEMINI_ENDPOINT + GEMINI_KEY (for proxies or other providers)
  if (GEMINI_ENDPOINT && GEMINI_KEY) {
    try {
      const res = await axios.post(GEMINI_ENDPOINT, {
        prompt,
        max_output_tokens: 1000
      }, {
        headers: {
          Authorization: `Bearer ${GEMINI_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000
      });

      const data = res.data || {};
      const text = data.output_text || data.text || (data.choices && data.choices[0] && (data.choices[0].message?.content || data.choices[0].text)) || JSON.stringify(data);
      return String(text).trim();
    } catch (err) {
      console.error('Gemini fallback failed:', {
        message: err?.message,
        status: err?.response?.status,
        statusText: err?.response?.statusText,
        data: err?.response?.data
      });
      throw new Error(`Lỗi khi gọi Gemini proxy: ${err?.response?.status || err?.message}`);
    }
  }

  throw new Error('No generative AI credentials configured. Set GOOGLE_API_KEY or GEMINI_API_* in .env');
}

