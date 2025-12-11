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
  }
  // thêm sách khác nếu cần
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