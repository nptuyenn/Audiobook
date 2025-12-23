import '../config/env.js';
import fs from 'fs/promises';
import { synthesizeToMp3 } from '../services/ttsService.js';

(async () => {
  const outputFile = 'tts_only_test.mp3';
  // Sử dụng một đoạn text tĩnh để chỉ test dịch vụ TTS, không gọi đến AI model.
  const staticTestText = 'Xin chào, đây là bài kiểm tra cho dịch vụ chuyển văn bản thành giọng nói của Azure. Nếu bạn có thể nghe được file âm thanh này, điều đó có nghĩa là cấu hình đã chính xác và dịch vụ đang hoạt động tốt.';

  try {
    console.log('--- Starting TTS-only test ---');
    console.log(`Using static text for synthesis (Length: ${staticTestText.length} characters).`);
    
    const audioBuffer = await synthesizeToMp3(staticTestText);
    
    await fs.writeFile(outputFile, audioBuffer);
    console.log(`--- Test Finished Successfully. Audio saved to ${outputFile} ---`);

  } catch (err) {
    console.error('\n--- TTS TEST FAILED ---');
    console.error('An error occurred during the TTS test:', err.message || err);
    if (err.stack) {
        console.error(err.stack);
    }
    console.error('--- END OF TEST ---');
  }
})();