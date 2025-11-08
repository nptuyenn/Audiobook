import express from "express";
import Audio from "../models/Audio.js";

const router = express.Router();

// GET /audio/book/:bookId
router.get("/book/:bookId", async (req, res, next) => {
  try {
    const audios = await Audio.find({ bookId: req.params.bookId }).sort({ chapter: 1 });
    if (!audios.length) return res.status(404).json({ message: "Không tìm thấy audio" });
    res.json(audios);
  } catch (err) {
    next(err);
  }
});

// GET /audio/:id
router.get("/:id", async (req, res, next) => {
  try {
    const audio = await Audio.findById(req.params.id);
    if (!audio) return res.status(404).json({ message: "Audio không tồn tại" });
    res.json({ audioUrl: audio.audioUrl });
  } catch (err) {
    next(err);
  }
});

export default router;