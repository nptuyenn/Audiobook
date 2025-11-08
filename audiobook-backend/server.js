import express from "express";
import mongoose from "mongoose";
import dotenv from "dotenv";
import cors from "cors";

import authRoutes from "./routes/auth.js";
import bookRoutes from "./routes/books.js";
import audioRoutes from "./routes/audio.js";
import activityRoutes from "./routes/activity.js";
import aiContentRoutes from "./routes/aiContent.js";

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// --- Routes ---
app.use("/auth", authRoutes);
app.use("/books", bookRoutes);
app.use("/audio", audioRoutes);
app.use("/activity", activityRoutes);
app.use("/ai-content", aiContentRoutes);

// --- MongoDB Connection ---
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log("MongoDB Atlas connected"))
  .catch(err => console.error("MongoDB connection error:", err));

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running at http://localhost:${PORT}`));
