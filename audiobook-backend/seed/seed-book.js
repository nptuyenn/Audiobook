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
    title: "Những Cuộc Phiêu Lưu Lí Kỳ Của Mumi Bố",
    author: "Tove Jansson",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306220/Truyen_phieu_luu_Nhung_cuoc_phieu_luu_li_ki_cua_Mumi_Bo_k3d6bz.jpg",
    description: "Hành trình phiêu lưu đầy bất ngờ của gia đình Mumi trong thế giới kỳ diệu.",
    category: "Phiêu lưu",
    avgRating: 4.7,
    totalListens: 0
  },
  {
    title: "Dế Mèn Phiêu Lưu Ký",
    author: "Tô Hoài",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306218/Truyen_phieu_luu_De_men_phieu_luu_ky_cfbi9a.jpg",
    description: "Những chuyến phiêu lưu và trải nghiệm của Dế Mèn trên đường đời.",
    category: "Phiêu lưu",
    avgRating: 4.9,
    totalListens: 0
  },
  {
    title: "Cuộc Phiêu Lưu Của Pinocchio",
    author: "Carlo Collodi",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306217/Truyen_phieu_luu_Cuoc_Phieu_Luu_Cua_Pinocchio_pqcvqc.webp",
    description: "Hành trình trưởng thành đầy bài học của cậu bé người gỗ Pinocchio.",
    category: "Phiêu lưu",
    avgRating: 4.5,
    totalListens: 0
  },
  {
    title: "80 Ngày Vòng Quanh Thế Giới",
    author: "Jules Verne",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306215/Truyen_phieu_luu_80_ngay_vong_quanh_the_gioi_qwrjjq.webp",
    description: "Cuộc đua nghẹt thở vòng quanh thế giới trong 80 ngày của Phileas Fogg.",
    category: "Phiêu lưu",
    avgRating: 4.8,
    totalListens: 0
  },
  {
    title: "Robinson Crusoe",
    author: "Daniel Defoe",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306214/Truyen_nuoc_ngoai_Ro_bin_xon_Co_ru_xo_xs4vet.webp",
    description: "Cuộc sống sinh tồn trên đảo hoang của chàng thủy thủ Robinson Crusoe.",
    category: "Nước ngoài",
    avgRating: 4.3,
    totalListens: 0
  },
  {
    title: "Lũ Trẻ Nhà Penderwicks",
    author: "Jeanne Birdsall",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306212/Truyen_nuoc_ngoai_Lu_tre_nha_Penderwicks_quypl3.webp",
    description: "Kỳ nghỉ hè đầy niềm vui và rắc rối của bốn chị em nhà Penderwicks.",
    category: "Nước ngoài",
    avgRating: 4.6,
    totalListens: 0
  },
  {
    title: "Julie, Con Của Bầy Sói",
    author: "Jean Craighead George",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306210/Truyen_nuoc_ngoai_Julie_con_cua_bay_soi_urme7b.webp",
    description: "Cô bé Julie sống hòa mình với thiên nhiên và bầy sói ở Alaska.",
    category: "Nước ngoài",
    avgRating: 4.4,
    totalListens: 0
  },
  {
    title: "Alice Ở Xứ Sở Thần Tiên",
    author: "Lewis Carroll",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306209/Truyen_nuoc_ngoai_Alice_in_wonderland_xdvyo2.webp",
    description: "Cuộc phiêu lưu kỳ diệu của Alice trong thế giới cổ tích đầy bất ngờ.",
    category: "Nước ngoài",
    avgRating: 4.9,
    totalListens: 0
  },
  {
    title: "Rùa Và Thỏ",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306206/Truyen_ngu_ngon_rua_va_tho_g6oprj.jpg",
    description: "Câu chuyện ngụ ngôn về sự kiêu ngạo và kiên trì trong cuộc đua.",
    category: "Ngụ ngôn",
    avgRating: 4.2,
    totalListens: 0
  },
  {
    title: "Triều Đình Của Sư Tử",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306207/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_idbabj.jpg",
    description: "Bài học về lòng biết ơn qua câu chuyện sư tử và chuột.",
    category: "Ngụ ngôn",
    avgRating: 4.5,
    totalListens: 0
  },
  {
    title: "Ếch Và Chuột",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306205/Truyen_ngu_ngon_Ech_va_chuot_wprrsb.jpg",
    description: "Ngụ ngôn cảnh báo về sự tin tưởng mù quáng và hậu quả của lòng tham.",
    category: "Ngụ ngôn",
    avgRating: 4.1,
    totalListens: 0
  },
  {
    title: "Dơi Và Hai Con Chồn",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306203/Truyen_ngu_ngon_Doi_va_hai_con_chon_z4stty.jpg",
    description: "Bài học về sự lật lọng và không trung thành của con dơi.",
    category: "Ngụ ngôn",
    avgRating: 4.0,
    totalListens: 0
  },
  {
    title: "Cáo Và Dê",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306201/Truyen_ngu_ngon_Cao_va_de_ezm3hy.jpg",
    description: "Ngụ ngôn về sự khôn ngoan và thoát hiểm khỏi cạm bẫy.",
    category: "Ngụ ngôn",
    avgRating: 4.3,
    totalListens: 0
  },
  {
    title: "Bác Thỏ Cây Và Các Con",
    author: "Aesop",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306200/Truyen_ngu_ngon_Bac_tho_cay_va_cac_con_enqac5.jpg",
    description: "Câu chuyện về lòng trung thực và hậu quả của sự dối trá.",
    category: "Ngụ ngôn",
    avgRating: 4.6,
    totalListens: 0
  },
  {
    title: "Thần Thoại Hy Lạp",
    author: "Nhiều tác giả",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306199/Truyen_giao_duc_Than_thoai_hy_lap_q0rcfe.jpg",
    description: "Tập hợp những câu chuyện thần thoại cổ điển Hy Lạp đầy hấp dẫn.",
    category: "Giáo dục",
    avgRating: 4.8,
    totalListens: 0
  },
  {
    title: "Làm Bạn Với Bầu Trời",
    author: "Nguyễn Nhật Ánh",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306198/Truyen_giao_duc_Lam_ban_voi_bau_troi_qzn9mq.webp",
    description: "Hành trình khám phá tình bạn và ước mơ qua những chuyến bay diều.",
    category: "Giáo dục",
    avgRating: 4.7,
    totalListens: 0
  },
  {
    title: "Hoàng Tử Bé",
    author: "Antoine de Saint-Exupéry",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306196/Truyen_giao_duc_Hoang_Tu_Be_sunxvx.jpg",
    description: "Câu chuyện triết lý sâu sắc về tình bạn, tình yêu và cuộc sống.",
    category: "Giáo dục",
    avgRating: 4.9,
    totalListens: 0
  },
  {
    title: "Sự Tích Hồ Ba Bể",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306194/Truyen_co_tich_su_tich_ho_ba_be_uuzogk.webp",
    description: "Truyền thuyết giải thích nguồn gốc hồ Ba Bể và bài học về lòng tốt.",
    category: "Cổ tích",
    avgRating: 4.4,
    totalListens: 0
  },
  {
    title: "Sự Tích Con Tằm",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306193/Truyen_co_tich_su_tich_con_tam_ghrmbi.webp",
    description: "Câu chuyện về nguồn gốc nghề nuôi tằm dệt lụa của người Việt.",
    category: "Cổ tích",
    avgRating: 4.2,
    totalListens: 0
  },
  {
    title: "Sơn Tinh Thủy Tinh",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306191/Truyen_co_tich_son_tinh_thuy_tinh_ewwdz9.webp",
    description: "Truyền thuyết về cuộc chiến giành công chúa Mị Nương và nguồn gốc lũ lụt.",
    category: "Cổ tích",
    avgRating: 4.6,
    totalListens: 0
  },
  {
    title: "Nàng Tiên Thứ Chín",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306191/Truyen_co_tich_nang_tien_thu_chin_v1t2rl.jpg",
    description: "Câu chuyện về lòng hiếu thảo và sự hy sinh của nàng tiên út.",
    category: "Cổ tích",
    avgRating: 4.5,
    totalListens: 0
  },
  {
    title: "Ba Chàng Dũng Sĩ",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306190/Truyen_co_tich_ba_chang_dung_si_ksudxw.jpg",
    description: "Hành trình trừ gian diệt bạo của ba chàng trai dũng cảm.",
    category: "Cổ tích",
    avgRating: 4.3,
    totalListens: 0
  },
  {
    title: "Trung Thu Và Chị Hằng",
    author: "Truyện cổ dân gian Việt Nam",
    coverUrl: "https://res.cloudinary.com/dyjin1gu3/image/upload/v1766306195/Truyen_co_tich_Trung_Thu_va_Chi_Hang_ynx3sd.webp",
    description: "Truyền thuyết về Chị Hằng Nga và nguồn gốc Tết Trung Thu.",
    category: "Cổ tích",
    avgRating: 4.8,
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