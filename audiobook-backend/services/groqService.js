import Groq from 'groq-sdk';

// Khởi tạo Groq client
const groq = new Groq({
  apiKey: process.env.GROQ_API_KEY,
});

/**
 * Tạo phản hồi chat từ Groq API.
 * @param {string} userMessage - Tin nhắn của người dùng.
 * @param {Array} history - Lịch sử cuộc trò chuyện (tùy chọn).
 * @returns {Promise<string>} - Phản hồi từ AI.
 */
export async function generateGroqChatCompletion(userMessage, history = []) {
  const GROQ_API_KEY = process.env.GROQ_API_KEY;
  if (!GROQ_API_KEY) {
    throw new Error('Chưa cấu hình GROQ_API_KEY trong file .env');
  }

  const systemPrompt = `Bạn là Gấu Nhỏ, trợ lý AI thân thiện trong một ứng dụng audiobook dành cho trẻ em. 
  Nhiệm vụ của bạn là trả lời các câu hỏi của bé một cách ngắn gọn, dễ hiểu, an toàn và luôn khuyến khích sự tò mò, khám phá. 
  Hãy dùng ngôn ngữ hồn nhiên, vui tươi và tích cực.`;

  // Xây dựng messages payload từ lịch sử và tin nhắn mới
  const messages = [
    {
      role: 'system',
      content: systemPrompt,
    },
    // Chuyển đổi lịch sử cũ (nếu có) sang định dạng của Groq
    ...history.map(item => ({
      role: item.role === 'user' ? 'user' : 'assistant',
      content: item.parts[0].text
    })),
    {
      role: 'user',
      content: userMessage,
    },
  ];

  console.log('[Groq] Đang gửi yêu cầu chat...');

  try {
    const chatCompletion = await groq.chat.completions.create({
      messages,
      model: 'llama-3.1-8b-instant', // Sử dụng model nhanh và hiệu quả cho chat
      temperature: 0.7,
      max_tokens: 250, // Giới hạn token cho câu trả lời ngắn
    });

    const responseText = chatCompletion.choices[0]?.message?.content || '';

    if (!responseText) {
      throw new Error('API không trả về nội dung.');
    }

    console.log('[Groq] Hoàn thành!');
    return responseText.trim();

  } catch (err) {
    if (err.response?.status === 429) {
      console.error('[Groq] Lỗi 429: Hết hạn mức request (Quota Exceeded).');
      throw new Error("Hệ thống đang bận. Bé vui lòng đợi một lát rồi thử lại nhé!");
    }

    console.error('Groq API Error:', err.response?.data || err.message);
    throw new Error(`Lỗi khi chat với AI: ${err.message}`);
  }
}
