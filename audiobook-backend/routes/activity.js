import express from "express";
import { protect } from "../middleware/auth.js";
import UserActivity from "../models/UserActivity.js";
import Book from "../models/Book.js";

const router = express.Router();

// POST /activity/progress
router.post("/progress", protect, async (req, res, next) => {
  try {
    const { bookId, chapter, progressTime } = req.body;
    if (!bookId || chapter === undefined || progressTime === undefined) {
      return res.status(400).json({ message: "Thiếu input" });
    }

    await UserActivity.updateOne(
      { userId: req.user.id, bookId },
      { chapter, progressTime, updatedAt: new Date() },
      { upsert: true }
    );

    res.json({ message: "Cập nhật tiến trình thành công" });
  } catch (err) {
    next(err);
  }
});

// GET/POST /activity/favorites
router.route("/favorites")
  .get(protect, async (req, res, next) => {
    try {
      const favorites = await UserActivity.find({
        userId: req.user.id,
        isFavorite: true
      }).populate("bookId", "title coverUrl author");

      const result = favorites
      .filter(f => f.bookId) 
      .map(f => ({
        bookId: f.bookId._id,
        title: f.bookId.title,
        coverUrl: f.bookId.coverUrl,
        author: f.bookId.author
      }));

res.json(result);
    } catch (err) {
      next(err);
    }
  })
  .post(protect, async (req, res, next) => {
    try {
      const { bookId, isFavorite } = req.body;
      const book = await Book.findById(bookId);
      if (!book) return res.status(404).json({ message: "Sách không tồn tại" });

      await UserActivity.updateOne(
        { userId: req.user.id, bookId },
        { isFavorite, updatedAt: new Date() },
        { upsert: true }
      );

      res.json({ message: "Cập nhật yêu thích thành công" });
    } catch (err) {
      next(err);
    }
  });

// POST /activity/review
router.post("/review", protect, async (req, res, next) => {
  try {
    const { bookId, rating, review } = req.body;
    if (!bookId || !rating || rating < 1 || rating > 5) {
      return res.status(400).json({ message: "Rating phải từ 1-5" });
    }

    await UserActivity.updateOne(
      { userId: req.user.id, bookId },
      { rating, review, updatedAt: new Date() },
      { upsert: true }
    );

    res.json({ message: "Gửi đánh giá thành công" });
  } catch (err) {
    next(err);
  }
});

export default router;