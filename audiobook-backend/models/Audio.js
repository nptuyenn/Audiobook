import mongoose from "mongoose";

const audioSchema = new mongoose.Schema({
  bookId: { type: mongoose.Schema.Types.ObjectId, ref: "Book" },
  chapter: Number,
  title: String,
  audioUrl: String
});

export default mongoose.model("Audio", audioSchema);
