import express from "express";
import { protect } from "../middleware/auth.js";
import AIContent from "../models/AIcontent.js";

const router = express.Router();

// POST /ai-content/generate
router.post("/generate", protect, async (req, res, next) => {
  try {
    const { title, prompt } = req.body;
    if (!title || !prompt) return res.status(400).json({ message: "Thiếu input" });

    // Giả lập AI (sau này thay bằng API thật)
    const storyText = `AI Story: "${prompt}". Nội dung được tạo tự động cho "${title}"...`;
    const audioUrl = `https://fake-ai-audio.com/${Date.now()}.mp3`;

    const content = await AIContent.create({
      userId: req.user.id,
      title,
      storyText,
      audioUrl
    });

    res.json({ storyText: content.storyText, audioUrl: content.audioUrl });
  } catch (err) {
    next(err);
  }
});

// GET /ai-content/user/:id
router.get("/user/:id", protect, async (req, res, next) => {
  try {
    if (req.params.id !== req.user.id.toString()) {
      return res.status(403).json({ message: "Không có quyền" });
    }

    const contents = await AIContent.find({ userId: req.user.id })
      .sort({ createdAt: -1 })
      .select("title storyText audioUrl createdAt");

    res.json(contents);
  } catch (err) {
    next(err);
  }
});

export default router;