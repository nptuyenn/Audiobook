// server.js
import express from "express";
import mongoose from "mongoose";
import dotenv from "dotenv";
import cors from "cors";

// Routes
import authRoutes from "./routes/auth.js";
import bookRoutes from "./routes/books.js";
import audioRoutes from "./routes/audio.js";
import activityRoutes from "./routes/activity.js";
import aiContentRoutes from "./routes/aiContent.js";

dotenv.config();

const app = express();

// Middleware
app.use(cors({
  origin: "*", // Dev: "*", sau đổi thành frontend URL
  credentials: true
}));
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ extended: true }));

// --- Routes (PHẢI TRƯỚC 404) ---
app.use("/auth", authRoutes);
app.use("/books", bookRoutes);
app.use("/audio", audioRoutes);
app.use("/activity", activityRoutes);
app.use("/ai-content", aiContentRoutes);

// 404 Handler - CHỈ BẮT KHI KHÔNG KHỚP ROUTE NÀO
app.all("*", (req, res) => {
  res.status(404).json({ message: "API không tồn tại" });
});

// Global Error Handler
app.use((err, req, res, next) => {
  console.error("Error:", err);
  const status = err.status || 500;
  const message = err.message || "Lỗi máy chủ nội bộ";

  res.status(status).json({
    message,
    ...(process.env.NODE_ENV === "development" && { stack: err.stack })
  });
});

// --- MongoDB Atlas Connection ---
const connectDB = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("MongoDB Atlas connected");
  } catch (err) {
    console.error("MongoDB connection failed:", err.message);
    process.exit(1);
  }
};

// Start Server
const PORT = process.env.PORT || 5000;

const startServer = async () => {
  await connectDB();
  app.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}`);
  });
};

startServer();