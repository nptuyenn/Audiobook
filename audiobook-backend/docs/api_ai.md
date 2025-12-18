# AI API - Contract

## POST /ai/story
Authentication: Bearer token (use `Authorization: Bearer <token>`)

Request JSON:
{
  "topic": "Tình bạn",
  "title": "Tình bạn của Gấu Nhỏ" // optional
}

Response JSON (200):
{
  "text": "...full story text...",
  "audioBase64": "<base64 mp3>",
  "audioMime": "audio/mpeg"
}

Notes:
- The client can immediately play the audio by decoding base64 into bytes.
- For large stories, consider requesting `POST /ai/story` and then calling `/ai/story/save` to persist the audio.


## POST /ai/story/save
Request JSON:
{
  "title": "...",
  "text": "...",
  "audioBase64": "..."
}

Response:
{ "message": "Saved", "aiContent": { ...saved document... } }


## POST /ai/chat
Request JSON:
{
  "message": "Hôm nay bé nên làm gì?",
  "history": [{ "role": "user", "text": "..." }, ...] // optional
}

Response JSON:
{ "text": "Xin chào, mình là Gấu Nhỏ..." }


## Notes for Android
- Use streaming playback from decoded bytes to avoid saving large files locally.
- To save permanently, call `/ai/story/save` with the returned `text` and `audioBase64`. The server uploads to Cloudinary and returns `aiContent` with `audioUrl`.
- All requests must be authenticated with JWT (same as other endpoints).
