import mongoose from "mongoose";

const audioSchema = new mongoose.Schema({
  bookId: { type: mongoose.Schema.Types.ObjectId, ref: "Book" },
  chapter: Number,
  title: String,
  audioUrl: String,
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  durationSec: Number,
  sizeBytes: Number,
  voice: String,
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.models.Audio || mongoose.model("Audio", audioSchema);
