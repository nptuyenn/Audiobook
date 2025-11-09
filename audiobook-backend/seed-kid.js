// seed-kids-5.js
import mongoose from "mongoose";
import dotenv from "dotenv";
import Book from "./models/Book.js";
import Audio from "./models/Audio.js";
import fetch from "node-fetch";

dotenv.config();

const seedKidsBooks = async () => {
  try {
    // Kết nối DB
    await mongoose.connect(process.env.MONGO_URI);
    console.log("MongoDB connected");

    // Xóa dữ liệu cũ
    await Book.deleteMany({});
    await Audio.deleteMany({});
    console.log("Cleared old data");

    // Gọi Gutendex API - truyện thiếu nhi có ảnh
    const res = await fetch("https://gutendex.com/books/?topic=children&mime_type=image/jpeg&page=1");
    const data = await res.json();
    const books = data.results.slice(0, 5); // Chỉ lấy 5 cuốn

    console.log(`Found ${books.length} children's books with covers`);

    for (const book of books) {
      // ÁNH XẠ DỮ LIỆU
      const title = book.title;
      const author = book.authors[0]?.name || "Unknown Author";
      const coverUrl = book.formats["image/jpeg"] || "https://via.placeholder.com/300x450?text=No+Cover";
      const description = book.summaries?.[0] || "A wonderful children's story.";
      
      // Lấy category từ subjects hoặc bookshelves
      const categoryKeywords = ["children", "fairy", "juvenile", "adventure"];
      let category = "Thiếu nhi";
      for (const subject of [...(book.subjects || []), ...(book.bookshelves || [])]) {
        if (categoryKeywords.some(kw => subject.toLowerCase().includes(kw))) {
          category = "Truyện cổ tích";
          break;
        }
      }

      // Tạo sách
      const newBook = await Book.create({
        title,
        author,
        coverUrl,
        description,
        category,
        avgRating: Number((Math.random() * 1.5 + 3.5).toFixed(1)), // 3.5 - 5.0
        totalListens: Math.floor(Math.random() * 3000) + 100
      });

      console.log(`Created: ${title}`);

      // Tạo 3 chương audio giả lập
      for (let i = 1; i <= 3; i++) {
        await Audio.create({
          bookId: newBook._id,
          chapter: i,
          title: `Chương ${i}: ${title}`,
          audioUrl: `https://fake-audio.com/kids/${newBook._id}-chapter-${i}.mp3`
        });
      }
    }

    console.log("5 CUỐN SÁCH THIẾU NHI + 15 AUDIO ĐÃ ĐƯỢC TẠO THÀNH CÔNG!");
    process.exit(0);
  } catch (err) {
    console.error("Error:", err.message);
    process.exit(1);
  }
};


seedKidsBooks();