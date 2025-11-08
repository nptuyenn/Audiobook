import express from "express";
import Book from "../models/Book.js";
import Audio from "../models/Audio.js";
import UserActivity from "../models/UserActivity.js";

const router = express.Router();

// GET /books
router.get("/", async (req, res, next) => {
  try {
    const books = await Book.find().select("title author coverUrl category avgRating totalListens");
    res.json(books);
  } catch (err) {
    next(err);
  }
});

// GET /books/:id
router.get("/:id", async (req, res, next) => {
  try {
    const book = await Book.findById(req.params.id);
    if (!book) return res.status(404).json({ message: "Không tìm thấy sách" });

    const audios = await Audio.find({ bookId: req.params.id }).sort({ chapter: 1 });
    const ratings = await UserActivity.find({ bookId: req.params.id, rating: { $gt: 0 } });
    const avgRating = ratings.length
      ? ratings.reduce((a, b) => a + b.rating, 0) / ratings.length
      : 0;

    res.json({
      ...book.toObject(),
      avgRating: Number(avgRating.toFixed(1)),
      audios
    });
  } catch (err) {
    next(err);
  }
});

// GET /books/search?q=...
router.get("/search", async (req, res, next) => {
  try {
    const { q } = req.query;
    if (!q) return res.status(400).json({ message: "Thiếu từ khóa tìm kiếm" });

    const books = await Book.find({
      $or: [
        { title: { $regex: q, $options: "i" } },
        { author: { $regex: q, $options: "i" } }
      ]
    });

    res.status(books.length > 0 ? 200 : 204).json(books);
  } catch (err) {
    next(err);
  }
});

export default router;