import express from 'express';
import rateLimit from 'express-rate-limit';
import Joi from 'joi';
import { validate } from '../middleware/validate.js';
import { protect } from '../middleware/auth.js';
import { createStory, createStoryFromSpeech, saveStory, chat } from '../controllers/aiController.js';

const storySchema = Joi.object({ topic: Joi.string().min(2).required(), title: Joi.string().optional() });
const speechSchema = Joi.object({ audioBase64: Joi.string().required(), audioMime: Joi.string().optional(), title: Joi.string().optional() });
const saveSchema = Joi.object({ title: Joi.string().optional(), text: Joi.string().required(), audioUrl: Joi.string().required() });
const chatSchema = Joi.object({ message: Joi.string().min(1).required(), history: Joi.array().optional() });

const router = express.Router();

const aiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10, // limit to 10 requests per window per IP
  message: { message: 'Too many requests, please slow down.' }
});

// Generate story and return audio (base64) + text
router.post('/story', protect, aiLimiter, validate(storySchema), createStory);
// Generate story from voice (speech-to-text -> story)
router.post('/story/voice', protect, aiLimiter, validate(speechSchema), createStoryFromSpeech);

// Save story (upload audio and save metadata)
router.post('/story/save', protect, aiLimiter, validate(saveSchema), saveStory);

// Chatbot
router.post('/chat', protect, aiLimiter, validate(chatSchema), chat);

export default router;
