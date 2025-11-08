import express from "express";
const router = express.Router();

router.get("/", (req, res) => {
  res.send("✨ AI Content route working");
});

export default router;
