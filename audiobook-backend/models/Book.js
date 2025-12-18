import mongoose from "mongoose";

const bookSchema = new mongoose.Schema({
  title: { type: String, required: true },
  author: { type: String, required: true },
  coverUrl: { type: String, required: true },
  description: String,
  category: String,
  avgRating: { type: Number, default: 0 },        
  totalListens: { type: Number, default: 0 },     
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.models.Book || mongoose.model("Book", bookSchema);
