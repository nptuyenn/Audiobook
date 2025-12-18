import mongoose from "mongoose";
const userSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  password: String,
  refreshToken: String,
  isVerified: { type: Boolean, default: false },
  resetToken: String,
  resetTokenExpires: Date,
  otp: String,                    
  otpExpires: Date,
  googleId: String,               
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.models.User || mongoose.model("User", userSchema);