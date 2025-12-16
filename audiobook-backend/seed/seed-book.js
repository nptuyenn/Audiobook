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
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Cover_book/Alice_in_wonderland.webp",
      description: "Cuộc phiêu lưu kỳ diệu của Alice trong thế giới cổ tích.",
      category: "Truyện cổ tích",
      avgRating: 0,
      totalListens: 0
    },
    {
      title: "Dế mèn phiêu lưu ký",
      author: "Tô Hoài",
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Cover_book/De_men_phieu_luu_ky.jpg",
      description: "Những chuyến phiêu lưu và trải nghiệm của Dế mèn trên đường đời.",
      category: "Phiêu lưu",
      avgRating: 0,
      totalListens: 0
    },
    {
      title: "Hoàng tử bé",
      author: "Antoine de Saint-Exupéry",
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Cover_book/Hoang_Tu_Be.jpg",
      description: "Câu chuyện ý nghĩa về tình bạn, sự trưởng thành và suy ngẫm.",
      category: "Thiếu nhi",
      avgRating: 0,
      totalListens: 0
    },
    {
      title: "Thỏ và rùa",
      author: "Aesop (Ngụ ngôn)",
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Cover_book/Tho_va_Rua.jpg",
      description: "Ngụ ngôn kinh điển về sự kiên trì và khiêm nhường.",
      category: "Ngụ ngôn",
      avgRating: 0,
      totalListens: 0
    },
    {
      title: "Thần thoại Hy Lạp",
      author: "Nhiều tác giả",
      coverUrl: "https://github.com/nptuyenn/Audiobook/releases/download/Cover_book/Than_thoai_hy_lap.jpg",
      description: "Tuyển chọn những truyền thuyết và câu chuyện nổi bật trong thần thoại Hy Lạp.",
      category: "Thần thoại/Giáo dục",
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