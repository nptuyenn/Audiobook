// VỊ TRÍ: audiobook-backend/seed/seed-books.js
import mongoose from "mongoose";
import Book from "../models/Book.js";
import dotenv from "dotenv";

dotenv.config();

const seedBooks = async () => {
  await mongoose.connect(process.env.MONGO_URI);
  console.log("MongoDB connected");

  await Book.deleteMany({}); // Xóa sách cũ (nếu cần)

  const books = [
    {
      title: "Alice in Wonderland",
      author: "Lewis Carroll",
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Audio_Cotich/alice_cover.jpg",
      description: "Cuộc phiêu lưu kỳ diệu của Alice trong thế giới cổ tích.",
      category: "Truyện cổ tích",
      avgRating: 0,
      totalListens: 0
    },
    {
      title: "The Little Prince",
      author: "Antoine de Saint-Exupéry",
      coverUrl: "https://example.com/little-prince.jpg",
      description: "Câu chuyện về hoàng tử bé từ hành tinh khác.",
      category: "Thiếu nhi",
      avgRating: 0,
      totalListens: 0
    }
  ];

  for (const book of books) {
    const newBook = await Book.create(book);
    console.log(`Đã thêm sách: ${newBook.title} (ID: ${newBook._id})`);
  }

  console.log("HOÀN TẤT! ĐÃ THÊM SÁCH");
  process.exit();
};
dotenv.config({ path: "../.env" });
seedBooks();