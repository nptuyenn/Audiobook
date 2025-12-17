// VỊ TRÍ: audiobook-backend/seed/seed-audio-github.js
import mongoose from "mongoose";
import Book from "../models/Book.js";
import Audio from "../models/Audio.js";
import dotenv from "dotenv";

dotenv.config();

const audioData = [
  {
    title: "Alice in Wonderland",
    chapters: [
      { chapter: 1, title: "Xuống hang thỏ", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Cotich/Alices1_chunk_001.mp3" },
      { chapter: 2, title: "Hồ nước mắt", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Cotich/Alices1_chunk_002.mp3" },
      { chapter: 3, title: "Cuộc đua", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Cotich/Alices1_chunk_003.mp3" },
      // thêm tiếp...
    ]
  },
  {
    title: "Dế mèn phiêu lưu ký",
    chapters: [
      { chapter: 1, title: "Khởi hành", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Phieuluu/de_men_phieu_luu_ky_000.mp3" },
      { chapter: 2, title: "Gặp bạn mới", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Phieuluu/de_men_phieu_luu_ky_000.mp3" },
      { chapter: 3, title: "Thử thách trên đường", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Phieuluu/de_men_phieu_luu_ky_000.mp3" },
      { chapter: 4, title: "Trở về và bài học", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Phieuluu/de_men_phieu_luu_ky_000.mp3" }
    ]
  },
  {
    title: "Hoàng tử bé",
    chapters: [
      { chapter: 1, title: "Cuộc gặp đầu tiên", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Hoang_tu_be_000.mp3" },
      { chapter: 2, title: "Hành trình kỳ lạ", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Hoang_tu_be_001.mp3" },
      { chapter: 3, title: "Bài học về tình bạn", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Hoang_tu_be_002.mp3" },
      { chapter: 4, title: "Nỗi nhớ quê nhà", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Hoang_tu_be_003.mp3" },
      { chapter: 5, title: "Lời chia tay", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Hoang_tu_be_004.mp3" }
    ]
  },
  {
    title: "Thỏ và rùa",
    chapters: [
      { chapter: 1, title: "Khởi tranh", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Tho_va_rua_001.mp3" },
      { chapter: 2, title: "Cố gắng bền bỉ", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Tho_va_rua_001.mp3" },
      { chapter: 3, title: "Bài học chiến thắng", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Ngungon/Tho_va_rua_001.mp3" }
    ]
  },
  {
    title: "Thần thoại Hy Lạp",
    chapters: [
      { chapter: 1, title: "Nguồn gốc các vị thần", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_000.mp3" },
      { chapter: 2, title: "Những câu chuyện về Zeus", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_001.mp3" },
      { chapter: 3, title: "Hercules và những thử thách", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_002.mp3" },
      { chapter: 4, title: "Tình yêu và thảm kịch", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_003.mp3" },
      { chapter: 5, title: "Trí tuệ Athena", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_004.mp3" },
      { chapter: 6, title: "Những truyền thuyết biển cả", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_005.mp3" },
      { chapter: 7, title: "Kết thúc huyền thoại", url: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Giaoduc/Than_thoai_Hy_Lap_006.mp3" }
    ]
  }
];

const seedAudio = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("MongoDB connected");

    for (const item of audioData) {
      const book = await Book.findOne({ title: { $regex: item.title, $options: "i" } });
      if (!book) {
        console.log(`Không tìm thấy sách: ${item.title}`);
        continue;
      }

      // Xóa audio cũ của sách này (nếu cần)
      await Audio.deleteMany({ bookId: book._id });

      // Thêm từng chương
      for (const chap of item.chapters) {
        await Audio.create({
          bookId: book._id,
          chapter: chap.chapter,
          title: chap.title,
          audioUrl: chap.url
        });
        console.log(`Added: ${book.title} - ${chap.title}`);
      }
    }

    console.log("HOÀN TẤT! ĐÃ THÊM AUDIO TỪ GITHUB RELEASE");
    process.exit(0);
  } catch (err) {
    console.error("Lỗi:", err.message);
    process.exit(1);
  }
};
dotenv.config({ path: "../.env" });
seedAudio();