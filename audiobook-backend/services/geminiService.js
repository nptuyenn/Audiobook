import axios from 'axios';

/**
 * Prompt được thiết kế lại để ép AI viết dài ngay lần đầu.
 * Sử dụng cấu trúc đoạn văn để định hình độ dài tốt hơn là chỉ đếm từ.
 */
const defaultSystemPrompt = `Bạn là nhà văn chuyên viết truyện thiếu nhi xuất sắc. 
Hãy viết một câu chuyện ngắn dành cho bé với giọng văn thân thiện, đơn giản và giàu cảm xúc.

YÊU CẦU BẮT BUỘC:
1. Độ dài: Câu chuyện phải cực kỳ chi tiết, gồm ít nhất 3 đến 4 đoạn văn dài.
2. Nội dung: Miêu tả kỹ bối cảnh thiên nhiên, hành động cụ thể và suy nghĩ nội tâm của nhân vật.
3. Hình thức: Trả về duy nhất nội dung truyện, không thêm lời chào hay lời dẫn.
4. Ngôn ngữ: Tiếng Việt, sử dụng từ ngữ giàu hình ảnh.`;

export async function generateStory(topic, systemPrompt = defaultSystemPrompt) {
  // Lấy API Key từ biến môi trường
  const GOOGLE_API_KEY = process.env.GOOGLE_API_KEY;
  

  const modelName = 'gemini-2.5-flash';
  const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${GOOGLE_API_KEY}`;

  if (!GOOGLE_API_KEY) {
    throw new Error('Chưa cấu hình GOOGLE_API_KEY trong file .env');
  }

  // Kết hợp prompt
  const fullPrompt = `${systemPrompt}\n\nChủ đề câu chuyện: ${topic}`;

  console.log(`[Gemini] Đang tạo truyện với chủ đề: "${topic}"...`);

  try {
    const payload = {
      contents: [
        {
          parts: [{ text: fullPrompt }]
        }
      ],
      generationConfig: {
        maxOutputTokens: 1700, 
        temperature: 0.7,      
        topP: 0.8,
        candidateCount: 1
      }
    };

    const res = await axios.post(url, payload, {
      headers: { 'Content-Type': 'application/json' },
      timeout: 45000 // Tăng timeout vì truyện dài cần thời gian xử lý lâu hơn
    });

    // Trích xuất dữ liệu từ cấu trúc chuẩn của Gemini API
    const data = res.data;
    let text = '';

    if (data.candidates && data.candidates[0]?.content?.parts?.[0]?.text) {
      text = data.candidates[0].content.parts[0].text;
    } else {
      // Fallback nếu cấu trúc trả về lạ
      text = extractTextFromObject(data) || "";
    }

    if (!text) {
      throw new Error("API không trả về nội dung văn bản.");
    }

    // Làm sạch văn bản (xóa các ký tự thừa, markdown lộn xộn)
    text = cleanGeneratedText(text);

    console.log(`[Gemini] Hoàn thành! Độ dài: ${wordCount(text)} từ.`);
    return text.trim();

  } catch (err) {
    // Xử lý lỗi Rate Limit (Quota) để thông báo cho người dùng
    if (err.response?.status === 429) {
      console.error('[Gemini] Lỗi 429: Hết hạn mức request (Quota Exceeded).');
      throw new Error("Hệ thống đang bận do quá nhiều yêu cầu. Bé vui lòng đợi 1 phút rồi thử lại nhé!");
    }

    console.error('Google Generative API Error:', err.response?.data || err.message);
    throw new Error(`Lỗi khi tạo nội dung từ AI: ${err.message}`);
  }
}

// ----------------- Helpers (Giữ nguyên các hàm bổ trợ của bạn) ------------------

function cleanGeneratedText(raw) {
  let s = String(raw || '').trim();
  
  // Xử lý nếu kết quả bị bọc trong JSON string
  try {
    if (/^\{/.test(s) || /^\[/.test(s)) {
      const obj = JSON.parse(s);
      const extracted = extractTextFromObject(obj);
      if (extracted) s = extracted;
    }
  } catch (e) {}

  // Xóa dấu ngoặc kép bọc ngoài nếu có
  s = s.replace(/^['"`]+|['"`]+$/g, '').trim();
  // Xử lý xuống dòng
  s = s.replace(/\\n/g, '\n').replace(/\n{3,}/g, '\n\n');
  
  return s;
}

function extractTextFromObject(obj) {
  if (!obj) return null;
  if (typeof obj === 'string') return obj;
  if (obj.text) return obj.text;

  if (Array.isArray(obj.candidates) && obj.candidates.length > 0) {
    const cand = obj.candidates[0];
    if (cand.content?.parts) {
      return cand.content.parts.map(p => p.text || '').join('');
    }
  }
  return null;
}

function wordCount(s) {
  return String(s || '').trim().split(/\s+/).filter(Boolean).length;
}