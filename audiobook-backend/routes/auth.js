// VỊ TRÍ: audiobook-backend/routes/auth.js
import express from "express";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import User from "../models/User.js";
import { protect } from "../middleware/auth.js";
import sendEmail from "../utils/sendEmail.js";
import generateOTP from "../utils/generateOTP.js";
import { OAuth2Client } from "google-auth-library";

const router = express.Router();
const googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// === 1. ĐĂNG KÝ ===
router.post("/signup", async (req, res) => {
  const { name, email, password } = req.body;
  const userExists = await User.findOne({ email });
  if (userExists) return res.status(400).json({ message: "Email đã tồn tại" });

  const user = await User.create({
    name,
    email,
    password: await bcrypt.hash(password, 10)
  });

  const accessToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
  const refreshToken = jwt.sign({ id: user._id }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: "7d" });
  user.refreshToken = refreshToken;
  await user.save();

  // Gửi OTP xác minh
  const otp = generateOTP();
  user.otp = otp;
  user.otpExpires = Date.now() + 10 * 60 * 1000;
  await user.save();

  await sendEmail(email, "Xác minh email Audiobook", `<h3>Mã OTP: <b>${otp}</b></h3><p>Hết hạn sau 10 phút.</p>`);

  res.json({ userId: user._id, accessToken, refreshToken, message: "Vui lòng xác minh email" });
});

// === 2. XÁC MINH OTP ===
router.post("/verify-otp", async (req, res) => {
  const { email, otp } = req.body;
  const user = await User.findOne({ email, otp, otpExpires: { $gt: Date.now() } });
  if (!user) return res.status(400).json({ message: "OTP sai hoặc hết hạn" });

  user.isVerified = true;
  user.otp = null;
  user.otpExpires = null;
  await user.save();

  res.json({ message: "Xác minh thành công" });
});

// === 3. QUÊN MẬT KHẨU ===
router.post("/forgot-password", async (req, res) => {
  const { email } = req.body;
  const user = await User.findOne({ email });
  if (!user) return res.status(404).json({ message: "Email không tồn tại" });

  const token = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
  user.resetToken = token;
  user.resetTokenExpires = Date.now() + 15 * 60 * 1000;
  await user.save();

  const resetUrl = `http://localhost:5000/reset-password?token=${token}`;
  const html = `<p>Click <a href="${resetUrl}">vào đây</a> để đặt lại mật khẩu (hết hạn 15 phút).</p>`;

  await sendEmail(email, "Đặt lại mật khẩu", html);
  res.json({ message: "Link đặt lại đã gửi" });
});

// === 4. ĐẶT LẠI MẬT KHẨU ===
router.post("/reset-password", async (req, res) => {
  const { token, newPassword } = req.body;
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    const user = await User.findOne({
      _id: decoded.id,
      resetToken: token,
      resetTokenExpires: { $gt: Date.now() }
    });
    if (!user) return res.status(400).json({ message: "Token không hợp lệ" });

    user.password = await bcrypt.hash(newPassword, 10);
    user.resetToken = null;
    user.resetTokenExpires = null;
    await user.save();

    res.json({ message: "Đặt lại mật khẩu thành công" });
  } catch (err) {
    res.status(400).json({ message: "Token hết hạn" });
  }
});

// === 5. GOOGLE LOGIN ===
router.post("/google", async (req, res) => {
  const { token } = req.body;
  try {
    const ticket = await googleClient.verifyIdToken({
      idToken: token,
      audience: process.env.GOOGLE_CLIENT_ID
    });
    const payload = ticket.getPayload();
    const { sub, email, name } = payload;

    let user = await User.findOne({ googleId: sub });
    if (!user) {
      user = await User.create({
        googleId: sub,
        email,
        name,
        isVerified: true
      });
    }

    const accessToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
    const refreshToken = jwt.sign({ id: user._id }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: "7d" });
    user.refreshToken = refreshToken;
    await user.save();

    res.json({ userId: user._id, accessToken, refreshToken });
  } catch (err) {
    res.status(400).json({ message: "Google token không hợp lệ" });
  }
});

// === CÁC API KHÁC (signin, refresh, logout) GIỮ NGUYÊN ===

export default router;