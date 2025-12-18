import mongoose from "mongoose";

const userActivitySchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
  bookId: { type: mongoose.Schema.Types.ObjectId, ref: "Book" },
  chapter: Number,
  progressTime: Number,
  isFavorite: Boolean,
  rating: Number,
  review: String,
  updatedAt: { type: Date, default: Date.now }
});

export default mongoose.models.UserActivity || mongoose.model("UserActivity", userActivitySchema);
