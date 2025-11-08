import mongoose from "mongoose";

const bookSchema = new mongoose.Schema({
  title: String,
  author: String,
  coverUrl: String,
  description: String,
  category: String,
  avgRating: Number,
  totalListens: Number,
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.model("Book", bookSchema);
