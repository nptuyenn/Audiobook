import express from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import User from "../models/User.js";
import { validate } from "../middleware/validate.js";
import { protect } from "../middleware/auth.js";
import Joi from "joi";

const router = express.Router();

// Schemas
const signupSchema = Joi.object({
  email: Joi.string().email().required(),
  password: Joi.string().min(6).required(),
  name: Joi.string().min(2).required(),
});

const signinSchema = Joi.object({
  email: Joi.string().email().required(),
  password: Joi.string().required(),
});

// Generate tokens
const generateTokens = (userId) => {
  const accessToken = jwt.sign({ id: userId }, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN,
  });
  const refreshToken = jwt.sign({ id: userId }, process.env.REFRESH_TOKEN_SECRET, {
    expiresIn: process.env.REFRESH_TOKEN_EXPIRES_IN,
  });
  return { accessToken, refreshToken };
};

// POST /auth/signup
router.post("/signup", validate(signupSchema), async (req, res, next) => {
  try {
    const { email, password, name } = req.body;
    const exists = await User.findOne({ email });
    if (exists) return res.status(409).json({ message: "Email đã tồn tại" });

    const hashed = await bcrypt.hash(password, 10);
    const user = await User.create({ email, password: hashed, name });

    const { accessToken, refreshToken } = generateTokens(user._id);
    res.status(201).json({ userId: user._id, accessToken, refreshToken });
  } catch (err) {
    next(err);
  }
});

// POST /auth/signin
router.post("/signin", validate(signinSchema), async (req, res, next) => {
  try {
    const { email, password } = req.body;
    const user = await User.findOne({ email });
    if (!user || !(await bcrypt.compare(password, user.password))) {
      return res.status(401).json({ message: "Sai email hoặc mật khẩu" });
    }

    const { accessToken, refreshToken } = generateTokens(user._id);
    res.json({ userId: user._id, accessToken, refreshToken });
  } catch (err) {
    next(err);
  }
});

// POST /auth/signout (client xóa token)
router.post("/signout", protect, (req, res) => {
  res.json({ message: "Đăng xuất thành công" });
});

// POST /auth/refresh
router.post("/refresh", (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) return res.status(401).json({ message: "Không có refresh token" });

  try {
    const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);
    const accessToken = jwt.sign({ id: decoded.id }, process.env.JWT_SECRET, {
      expiresIn: process.env.JWT_EXPIRES_IN,
    });
    res.json({ accessToken });
  } catch (err) {
    res.status(403).json({ message: "Refresh token hết hạn" });
  }
});

export default router;