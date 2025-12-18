import mongoose from "mongoose";

const aiContentSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
  title: String,
  storyText: String,
  audioUrl: String,
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.models.AIContent || mongoose.model("AIContent", aiContentSchema);
