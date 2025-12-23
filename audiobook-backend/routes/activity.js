import express from "express";
import { protect } from "../middleware/auth.js";
import UserActivity from "../models/UserActivity.js";
import Book from "../models/Book.js";
import Audio from "../models/Audio.js";

const router = express.Router();

// POST /activity/progress
router.post("/progress", protect, async (req, res, next) => {
  try {
    const { bookId, chapter, progressTime } = req.body;
    if (!bookId || chapter === undefined || progressTime === undefined) {
      return res.status(400).json({ message: "Thiếu input" });
    }

    const audioChapters = await Audio.find({ bookId }).sort({ chapter: -1 });
    const lastChapter = audioChapters[0];

    let isFinished = false;
    if (lastChapter && chapter === lastChapter.chapter) {
      const chapterDuration = lastChapter.durationSec;
      // Mark as finished if progress is within 95% of the last chapter's duration
      if (chapterDuration > 0 ) {
        isFinished = true;
      }
    }

    const updateData = { chapter, progressTime, updatedAt: new Date() };
    if (isFinished) {
      updateData.isFinished = true;
    }

    await UserActivity.updateOne(
      { userId: req.user.id, bookId },
      updateData,
      { upsert: true }
    );

    res.json({ message: "Cập nhật tiến trình thành công", isFinished });
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

    // Recalculate average rating for the book
    const activities = await UserActivity.find({ bookId, rating: { $exists: true, $ne: null } });
    const totalRating = activities.reduce((sum, act) => sum + act.rating, 0);
    const avgRating = activities.length > 0 ? totalRating / activities.length : 0;

    await Book.updateOne({ _id: bookId }, { avgRating });

    res.json({ message: "Gửi đánh giá thành công" });
  } catch (err) {
    next(err);
  }
});

// POST /activity/start-listening
router.post("/start-listening", protect, async (req, res, next) => {
  try {
    const { bookId } = req.body;
    if (!bookId) {
      return res.status(400).json({ message: "Thiếu bookId" });
    }

    await UserActivity.updateOne(
      { userId: req.user.id, bookId },
      // Chỉ cập nhật thời gian tương tác, không set isFinished
      // upsert: true sẽ tạo mới record nếu chưa có
      { $set: { updatedAt: new Date() } },
      { upsert: true }
    );

    res.status(200).json({ message: "Đã ghi nhận trạng thái đã nghe" });
  } catch (err) {
    next(err);
  }
});

// GET /activity/listened
router.get("/listened", protect, async (req, res, next) => {
  try {
    const listenedActivities = await UserActivity.find({
      userId: req.user.id,
      isFinished: true
    }).populate("bookId", "title coverUrl author");

    const result = listenedActivities
      .filter(activity => activity.bookId)
      .map(activity => ({
        bookId: activity.bookId._id,
        title: activity.bookId.title,
        coverUrl: activity.bookId.coverUrl,
        author: activity.bookId.author
      }));

    res.json(result);
  } catch (err) {
    next(err);
  }
});

// GET /activity/history - New endpoint for listening history
router.get("/history", protect, async (req, res, next) => {
  try {
    const history = await UserActivity.find({
      userId: req.user.id,
      // Sách đã được bắt đầu nghe (có progressTime)
      progressTime: { $gt: 0 } 
    })
    .sort({ updatedAt: -1 }) // Sắp xếp theo lần nghe gần nhất
    .populate("bookId", "title coverUrl author");

    const result = history
      .filter(activity => activity.bookId) // Lọc những activity có sách (bookId không null)
      .map(activity => ({
        bookId: activity.bookId._id,
        title: activity.bookId.title,
        coverUrl: activity.bookId.coverUrl,
        author: activity.bookId.author,
        progressTime: activity.progressTime,
        chapter: activity.chapter,
        updatedAt: activity.updatedAt,
        isFinished: activity.isFinished,
      }));

    res.json(result);
  } catch (err) {
    next(err);
  }
});

export default router;