// audiobook-backend/routes/auth.js
import express from "express";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import User from "../models/User.js";
import sendEmail from "../utils/sendEmail.js";
import generateOTP from "../utils/generateOTP.js";
import { OAuth2Client } from "google-auth-library";

const router = express.Router();
const googleClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

// === 1. ĐĂNG KÝ ===
router.post("/signup", async (req, res) => {
  try {
    const { name, email, password } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Vui lòng gửi đầy đủ name, email, password" });
    }

    const userExists = await User.findOne({ email });
    if (userExists) return res.status(400).json({ message: "Email đã tồn tại" });

    const hashedPassword = await bcrypt.hash(password, 10);

    const user = await User.create({
      name,
      email,
      password: hashedPassword
    });

    const accessToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
    const refreshToken = jwt.sign({ id: user._id }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: "7d" });
    user.refreshToken = refreshToken;

    // Gửi OTP xác minh
    const otp = generateOTP();
    user.otp = otp;
    user.otpExpires = Date.now() + 10 * 60 * 1000;

    await user.save();
    await sendEmail(email, "Xác minh email Audiobook", `<h3>Mã OTP: <b>${otp}</b></h3><p>Hết hạn sau 10 phút.</p>`);

    res.status(201).json({ userId: user._id, accessToken, refreshToken, message: "Vui lòng xác minh email" });
  } catch (err) {
    console.error("Signup error:", err);
    res.status(500).json({ message: "Lỗi server khi tạo tài khoản" });
  }
});

// === 2. XÁC MINH OTP ===
router.post("/verify-otp", async (req, res) => {
  try {
    const { email, otp } = req.body;
    if (!email || !otp) return res.status(400).json({ message: "Email và OTP là bắt buộc" });

    const user = await User.findOne({ email, otp, otpExpires: { $gt: Date.now() } });
    if (!user) return res.status(400).json({ message: "OTP sai hoặc hết hạn" });

    user.isVerified = true;
    user.otp = null;
    user.otpExpires = null;
    await user.save();

    res.json({ message: "Xác minh thành công" });
  } catch (err) {
    console.error("Verify OTP error:", err);
    res.status(500).json({ message: "Lỗi server khi xác minh OTP" });
  }
});

// === 3. ĐĂNG NHẬP ===
router.post("/signin", async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ message: "Email và password là bắt buộc" });

    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ message: "Email không tồn tại" });

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(400).json({ message: "Mật khẩu sai" });
    if (!user.isVerified) return res.status(400).json({ message: "Vui lòng xác minh email trước" });

    const accessToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
    const refreshToken = jwt.sign({ id: user._id }, process.env.REFRESH_TOKEN_SECRET, { expiresIn: "7d" });
    user.refreshToken = refreshToken;
    await user.save();

    res.json({ userId: user._id, accessToken, refreshToken });
  } catch (err) {
    console.error("Signin error:", err);
    res.status(500).json({ message: "Lỗi server khi đăng nhập" });
  }
});

// === 4. QUÊN MẬT KHẨU ===
router.post("/forgot-password", async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) return res.status(400).json({ message: "Email là bắt buộc" });

    const user = await User.findOne({ email });
    if (!user) return res.status(404).json({ message: "Email không tồn tại" });

    const newPassword = Math.random().toString(36).slice(-8);
    user.password = await bcrypt.hash(newPassword, 10);
    await user.save();

    const html = `
      <h3>Mật khẩu mới của bạn</h3>
      <p>Email: <b>${email}</b></p>
      <p>Mật khẩu mới: <b>${newPassword}</b></p>
      <p>Vui lòng đăng nhập và đổi mật khẩu ngay!</p>
    `;
    await sendEmail(email, "Mật khẩu mới - Audiobook", html);

    res.json({ message: "Mật khẩu mới đã được gửi đến email" });
  } catch (err) {
    console.error("Forgot password error:", err);
    res.status(500).json({ message: "Lỗi server khi tạo mật khẩu mới" });
  }
});

// === 5. LÀM MỚI TOKEN ===
router.post("/refresh", async (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) return res.status(401).json({ message: "Không có refresh token" });

    const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);
    const user = await User.findById(decoded.id);
    if (!user || user.refreshToken !== refreshToken) {
      return res.status(401).json({ message: "Refresh token không hợp lệ" });
    }

    const accessToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, { expiresIn: "15m" });
    res.json({ accessToken });
  } catch (err) {
    console.error("Refresh token error:", err);
    res.status(401).json({ message: "Refresh token hết hạn hoặc không hợp lệ" });
  }
});

// === 6. ĐĂNG XUẤT ===
router.post("/logout", async (req, res) => {
  try {
    const { refreshToken } = req.body;
    if (!refreshToken) return res.status(400).json({ message: "Không có token" });

    const user = await User.findOne({ refreshToken });
    if (user) {
      user.refreshToken = null;
      await user.save();
    }

    res.json({ message: "Đăng xuất thành công" });
  } catch (err) {
    console.error("Logout error:", err);
    res.status(500).json({ message: "Lỗi server khi đăng xuất" });
  }
});

// === 7. GOOGLE LOGIN ===
router.post("/google", async (req, res) => {
  try {
    const { token } = req.body;
    if (!token) return res.status(400).json({ message: "Token là bắt buộc" });

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
    console.error("Google login error:", err);
    res.status(400).json({ message: "Google token không hợp lệ" });
  }
});

export default router;
