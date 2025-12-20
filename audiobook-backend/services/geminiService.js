import axios from 'axios';

const defaultSystemPrompt = `Bạn là nhà văn chuyên viết truyện thiếu nhi, hãy viết một câu chuyện ngắn dành cho bé, giọng văn thân thiện, đơn giản và nhiều hình ảnh cảm xúc. Độ dài ~300-700 từ. Trả về chỉ phần nội dung truyện.`;

export async function generateStory(topic, systemPrompt = defaultSystemPrompt, attempt = 0) {
  const prompt = `${systemPrompt}\n\nChủ đề: ${topic}`;

  // Read env vars at call-time (ensures values are available even if loaded later)
  const GOOGLE_API_KEY = process.env.GOOGLE_API_KEY;
  const GOOGLE_MODEL = process.env.GOOGLE_MODEL || 'models/gemini-2.5-flash';
  const GEMINI_ENDPOINT = process.env.GEMINI_API_ENDPOINT;
  const GEMINI_KEY = process.env.GEMINI_API_KEY;

  // Debug: log presence (avoid printing actual keys)
  console.log('generateStory called -> GOOGLE_API_KEY:', !!GOOGLE_API_KEY, 'GEMINI_KEY:', !!GEMINI_KEY, 'GEMINI_ENDPOINT:', !!GEMINI_ENDPOINT, 'attempt:', attempt);

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
        maxOutputTokens: 1200,
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

      // Robust parsing: try multiple shapes
      if (Array.isArray(data.candidates) && data.candidates.length) {
        const cand = data.candidates[0];
        if (Array.isArray(cand.content)) {
          text = cand.content.map(c => c.text || c).join('');
        } else if (cand.content && Array.isArray(cand.content.parts)) {
          text = cand.content.parts.map(p => p.text || '').join('');
        } else if (cand.output) {
          text = cand.output;
        } else if (cand.text) {
          text = cand.text;
        } else if (typeof cand === 'string') {
          text = cand;
        }
      }

      if (!text) {
        text = data.output_text || data.output || data.text || JSON.stringify(data);
      }

      text = cleanGeneratedText(String(text));

      // If text is very short, attempt one expansion pass
      if (wordCount(text) < 200 && attempt === 0) {
        console.log('Generated story short (words:', wordCount(text), '); attempting expansion...');
        const expandedSystem = `${systemPrompt}\n\nYêu cầu: Truyện hiện tại quá ngắn - hãy mở rộng nó lên khoảng 300-700 từ, thêm chi tiết miêu tả và cảm xúc, giữ nguyên nội dung chính và giọng văn thân thiện cho trẻ em. Trả về chỉ phần nội dung truyện.`;
        return generateStory(topic, expandedSystem, attempt + 1);
      }

      return text.trim();
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
        max_output_tokens: 1200
      }, {
        headers: {
          Authorization: `Bearer ${GEMINI_KEY}`,
          'Content-Type': 'application/json'
        },
        timeout: 30000
      });

      const data = res.data || {};
      let text = data.output_text || data.text || (data.choices && data.choices[0] && (data.choices[0].message?.content || data.choices[0].text)) || JSON.stringify(data);
      text = cleanGeneratedText(String(text));

      if (wordCount(text) < 200 && attempt === 0) {
        console.log('Generated story short (words:', wordCount(text), '); attempting expansion with Gemini fallback...');
        const expandedSystem = `${systemPrompt}\n\nYêu cầu: Truyện hiện tại quá ngắn - hãy mở rộng nó lên khoảng 300-700 từ, thêm chi tiết miêu tả và cảm xúc, giữ nguyên nội dung chính và giọng văn thân thiện cho trẻ em. Trả về chỉ phần nội dung truyện.`;
        return generateStory(topic, expandedSystem, attempt + 1);
      }

      return text.trim();
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


// ----------------- Helpers ------------------
function cleanGeneratedText(raw) {
  let s = String(raw || '').trim();

  // If string looks like serialized JSON, try to parse and extract text
  try {
    if (/^\{/.test(s) || /^\[/.test(s)) {
      const obj = JSON.parse(s);
      const extracted = extractTextFromObject(obj);
      if (extracted) s = extracted;
    }
  } catch (e) {
    // try unescaping quoted JSON
    try {
      const unquoted = s.replace(/^"|"$/g, '').replace(/\\"/g, '"');
      if (/^\{/.test(unquoted) || /^\[/.test(unquoted)) {
        const obj2 = JSON.parse(unquoted);
        const extracted2 = extractTextFromObject(obj2);
        if (extracted2) s = extracted2;
      }
    } catch (e2) {
      // ignore
    }
  }

  // If still contains JSON-like wrapper, try regex extraction of text fields
  const m = s.match(/"?text"?\s*:\s*"([\s\S]{50,})"/);
  if (m) {
    s = m[1].replace(/\\"/g, '"');
  }

  // Replace escaped newlines and excessive whitespace
  s = s.replace(/\\n/g, '\n').replace(/\s{2,}/g, ' ').trim();

  // Strip wrapping quotes
  s = s.replace(/^['"`]+|['"`]+$/g, '').trim();

  return s;
}

function extractTextFromObject(obj) {
  if (!obj) return null;
  if (typeof obj === 'string') return obj;
  if (obj.output_text) return obj.output_text;
  if (obj.output) return obj.output;
  if (obj.text) return obj.text;

  if (Array.isArray(obj.candidates) && obj.candidates.length) {
    const cand = obj.candidates[0];
    if (cand.content) {
      if (Array.isArray(cand.content)) {
        return cand.content.map(c => (typeof c === 'string' ? c : c.text || '')).join('');
      }
      if (cand.content.parts && Array.isArray(cand.content.parts)) {
        return cand.content.parts.map(p => p.text || '').join('');
      }
      return cand.content.text || null;
    }
    if (cand.output_text) return cand.output_text;
    if (cand.output) return cand.output;
    if (cand.text) return cand.text;
  }

  if (Array.isArray(obj.choices) && obj.choices.length) {
    const c = obj.choices[0];
    if (c.message && c.message.content) return c.message.content;
    if (c.text) return c.text;
  }

  // Recursive search
  for (const k of Object.keys(obj)) {
    try {
      const res = extractTextFromObject(obj[k]);
      if (res) return res;
    } catch (e) { /* ignore */ }
  }

  return null;
}

function wordCount(s) {
  return String(s || '')
    .trim()
    .split(/\s+/)
    .filter(Boolean).length;
}

