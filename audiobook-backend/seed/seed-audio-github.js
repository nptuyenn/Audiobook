// VỊ TRÍ: audiobook-backend/seed/seed-audio-github.js
import mongoose from "mongoose";
import Book from "../models/Book.js";
import Audio from "../models/Audio.js";
import dotenv from "dotenv";

dotenv.config();

const audioData = [
  {
    title: "Trung Thu Và Chị Hằng",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306270/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c1_fvcdzh.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306271/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c2_ubxsu8.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306272/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c3_k9h25y.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306273/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c4_qwzyrz.mp3" },
      { chapter: 5, title: "Chương 5", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306275/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c5_cojvlo.mp3" },
      { chapter: 6, title: "Chương 6", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306276/Truyen_co_tich_Trung_Thu_va_Chi_Hang_c6_wpqfbl.mp3" }
    ]
  },
  {
    title: "Ba Chàng Dũng Sĩ",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306223/Truyen_co_tich__ba_chang_dung_si_c1_ktfmqu.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306224/Truyen_co_tich__ba_chang_dung_si_c2_s6h2df.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306225/Truyen_co_tich__ba_chang_dung_si_c3_zqjria.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306227/Truyen_co_tich__ba_chang_dung_si_c4_mmi4zq.mp3" },
      { chapter: 5, title: "Chương 5", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306228/Truyen_co_tich__ba_chang_dung_si_c5_ke1r9k.mp3" }
    ]
  },
  {
    title: "Nàng Tiên Thứ Chín",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306229/Truyen_co_tich__nang_tien_thu_chin_c1_mn0tky.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306233/Truyen_co_tich__nang_tien_thu_chin_c2_wbnxcb.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306232/Truyen_co_tich__nang_tien_thu_chin_c3_ql8met.mp3" }
    ]
  },
  {
    title: "Sự Tích Con Tằm",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306234/Truyen_co_tich__su_tich_con_tam_c1_n4yexh.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306235/Truyen_co_tich__su_tich_con_tam_c2_ctixfw.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306236/Truyen_co_tich__su_tich_con_tam_c3_dlfkpt.mp3" }
    ]
  },
  {
    title: "Sự Tích Hồ Ba Bể",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306237/Truyen_co_tich__su_tich_ho_ba_be_c1_yedwex.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306239/Truyen_co_tich__su_tich_ho_ba_be_c2_ebkwiq.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306240/Truyen_co_tich__su_tich_ho_ba_be_c3_rmolcm.mp3" }
    ]
  },
  {
    title: "Sơn Tinh Thủy Tinh",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306241/Truyen_co_tich_son_tinh_thuy_tinh_c1_ly6agz.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306243/Truyen_co_tich_son_tinh_thuy_tinh_c2_efkedl.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306244/Truyen_co_tich_son_tinh_thuy_tinh_c3_li5zuf.mp3" }
    ]
  },
  {
    title: "Hoàng Tử Bé",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306278/Truyen_giao_duc_Hoang_tu_be_c1_vd5ljy.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306279/Truyen_giao_duc_Hoang_tu_be_c2_q2gi2n.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306281/Truyen_giao_duc_Hoang_tu_be_c3_mbjgyr.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306282/Truyen_giao_duc_Hoang_tu_be_c4_kjmoke.mp3" }
    ]
  },
  {
    title: "Làm Bạn Với Bầu Trời",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306284/Truyen_giao_duc_Lam_ban_voi_bau_troi_c1_lsgr14.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306284/Truyen_giao_duc_Lam_ban_voi_bau_troi_c2_vv9pkz.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306286/Truyen_giao_duc_Lam_ban_voi_bau_troi_c3_g0em6v.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306287/Truyen_giao_duc_Lam_ban_voi_bau_troi_c4_tfq4nu.mp3" }
    ]
  },
  {
    title: "Thần Thoại Hy Lạp",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306289/Truyen_giao_duc_Than_thoai_Hy_Lap_c1_mjv9jc.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306290/Truyen_giao_duc_Than_thoai_Hy_Lap_c2_pbiokv.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306291/Truyen_giao_duc_Than_thoai_Hy_Lap_c3_mbysnu.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306293/Truyen_giao_duc_Than_thoai_Hy_Lap_c4_azctb3.mp3" }
    ]
  },
  {
    title: "Bác Thỏ Cây Và Các Con",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306295/Truyen_ngu_ngon_Bac_tho_cay_va_cac_con_c1_opb6pn.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306295/Truyen_ngu_ngon_Bac_tho_cay_va_cac_con_c2_conzig.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306297/Truyen_ngu_ngon_Bac_tho_cay_va_cac_con_c3_wavbtc.mp3" }
    ]
  },
  {
    title: "Cáo Và Dê",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306298/Truyen_ngu_ngon_Cao_va_de_c1_jbvn0v.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306300/Truyen_ngu_ngon_Cao_va_de_c2_ntwu0t.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306301/Truyen_ngu_ngon_Cao_va_de_c3_fuwqok.mp3" }
    ]
  },
  {
    title: "Dơi Và Hai Con Chồn",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306303/Truyen_ngu_ngon_Doi_va_hai_con_chon_c1_aw4lme.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306304/Truyen_ngu_ngon_Doi_va_hai_con_chon_c2_ghlu94.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306306/Truyen_ngu_ngon_Doi_va_hai_con_chon_c3_f4d4ba.mp3" }
    ]
  },
  {
    title: "Ếch Và Chuột",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306308/Truyen_ngu_ngon_Ech_va_chuot_c1_zphi2d.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306309/Truyen_ngu_ngon_Ech_va_chuot_c2_pemr13.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306310/Truyen_ngu_ngon_Ech_va_chuot_c3_ygjvyh.mp3" }
    ]
  },
  {
    title: "Rùa Và Thỏ",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306311/Truyen_ngu_ngon_Rua_va_tho_c1_va5fdp.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306312/Truyen_ngu_ngon_Rua_va_tho_c2_onfyab.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306314/Truyen_ngu_ngon_Rua_va_tho_c3_tgauhw.mp3" }
    ]
  },
  {
    title: "Triều Đình Của Sư Tử",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306315/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_c1_xpaeqx.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306317/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_c2_zpzxlk.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306319/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_c3_a8nldc.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306319/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_c4_huwrkb.mp3" },
      { chapter: 5, title: "Chương 5", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306321/Truyen_ngu_ngon_Trieu_dinh_cua_su_tu_c5_yzrpwo.mp3" }
    ]
  },
  {
    title: "Alice Ở Xứ Sở Thần Tiên",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306322/Truyen_nuoc_ngoai_Alice_in_wonderlane_c1_lr6ppw.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306323/Truyen_nuoc_ngoai_Alice_in_wonderlane_c2_sqir8e.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306325/Truyen_nuoc_ngoai_Alice_in_wonderlane_c3_fiu7fq.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306326/Truyen_nuoc_ngoai_Alice_in_wonderlane_c4_ew9imw.mp3" }
    ]
  },
  {
    title: "Julie, Con Của Bầy Sói",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306327/Truyen_nuoc_ngoai_Julie_con_cua_bay_soi_c1_paqsdf.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306329/Truyen_nuoc_ngoai_Julie_con_cua_bay_soi_c2_dpxtoy.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306330/Truyen_nuoc_ngoai_Julie_con_cua_bay_soi_c3_o3ofci.mp3" }
    ]
  },
  {
    title: "Lũ Trẻ Nhà Penderwicks",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306332/Truyen_nuoc_ngoai_Lu_tre_nha_Penderwicks_c1_tnq1q9.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306333/Truyen_nuoc_ngoai_Lu_tre_nha_Penderwicks_c2_vywujt.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306335/Truyen_nuoc_ngoai_Lu_tre_nha_Penderwicks_c3_gpoatk.mp3" }
    ]
  },
  {
    title: "Robinson Crusoe",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306336/Truyen_nuoc_ngoai_Ro-bin-xon_Co-ru-xo_c1_j4ebk2.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306337/Truyen_nuoc_ngoai_Ro-bin-xon_Co-ru-xo_c2_jc4mdp.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306339/Truyen_nuoc_ngoai_Ro-bin-xon_Co-ru-xo_c3_ol5jhf.mp3" }
    ]
  },
  {
    title: "80 Ngày Vòng Quanh Thế Giới",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306340/Truyen_phieu_luu_80_ngay_vong_quanh_the_gioi_c1_tfd8gq.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306342/Truyen_phieu_luu_80_ngay_vong_quanh_the_gioi_c2_l38r6f.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306345/Truyen_phieu_luu_80_ngay_vong_quanh_the_gioi_c3_uprcxk.mp3" },
      { chapter: 4, title: "Chương 4", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306345/Truyen_phieu_luu_80_ngay_vong_quanh_the_gioi_c4_obqlcr.mp3" }
    ]
  },
  {
    title: "Cuộc Phiêu Lưu Của Pinocchio",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306346/Truyen_phieu_luu_Cuoc_Phieu_Luu_Cua_Pinocchio_c1_xzwecf.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306349/Truyen_phieu_luu_Cuoc_Phieu_Luu_Cua_Pinocchio_c2_wjcxlz.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306349/Truyen_phieu_luu_Cuoc_Phieu_Luu_Cua_Pinocchio_c3_ghhbq8.mp3" }
    ]
  },
  {
    title: "Những Cuộc Phiêu Lưu Lí Kỳ Của Mumi Bố",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306358/Truyen_phieu_luu_Nhung_cuoc_phieu_luu_li_ki_cua_Mumi_Bo_c1_utuyry.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306357/Truyen_phieu_luu_Nhung_cuoc_phieu_luu_li_ki_cua_Mumi_Bo_c2_iwqoql.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306357/Truyen_phieu_luu_Nhung_cuoc_phieu_luu_li_ki_cua_Mumi_Bo_c3_zyjyml.mp3" }
    ]
  },
  {
    title: "Dế Mèn Phiêu Lưu Ký",
    chapters: [
      { chapter: 1, title: "Chương 1", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306350/Truyen_phieu_luu_de_men_phieu_luu_ky_c1_b9pgnm.mp3" },
      { chapter: 2, title: "Chương 2", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306353/Truyen_phieu_luu_de_men_phieu_luu_ky_c2_n2vl9v.mp3" },
      { chapter: 3, title: "Chương 3", url: "https://res.cloudinary.com/dyjin1gu3/video/upload/v1766306353/Truyen_phieu_luu_de_men_phieu_luu_ky_c3_ws0ep5.mp3" }
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