# AudioBook for Kids

AudioBook for Kids is a mobile audiobook application designed to make stories
more accessible, engaging, and interactive for children. In addition to
providing a library of narrated stories, the application uses AI to help
children and parents create personalized stories and listen to them as audio.

The repository contains:

- An Android client written in Java.
- A Node.js and Express backend.
- A MongoDB data layer for users, books, audio chapters, and listening
  activity.
- AI services for story generation, voice input, text-to-speech, and
  conversational chat.

## Features

### Audiobook library

- Browse children’s stories grouped by topics such as fairy tales, fables,
  education, adventure, and foreign stories.
- Search books by title or author.
- View book details, descriptions, ratings, covers, and chapter lists.
- Stream chapter audio from the backend.

### Listening experience

- Play, pause, seek, and resume audiobook chapters.
- Continue listening from the saved playback position.
- Keep a listening history and recently played list.
- Use a persistent mini-player while navigating between screens.
- Mark books as favorites and submit ratings or reviews.

### Accounts and authentication

- Sign up and verify an email address with an OTP.
- Sign in with email and password.
- Sign in with Google.
- Refresh access tokens and log out securely.
- Request a new password by email.

### AI-powered storytelling

- Generate a new children’s story from a text topic.
- Record a voice prompt and generate a story from the transcribed speech.
- Convert generated stories into playable audio.
- Save generated stories to the user’s library.
- Chat with the application’s AI assistant.

## Architecture

```text
Android app (Java)
        |
        | Retrofit / JSON API
        v
Node.js + Express backend
        |
        +--> MongoDB / MongoDB Atlas
        +--> Cloudinary (book covers and audio assets)
        +--> Gemini or Google AI (story generation)
        +--> Groq (AI chat)
        +--> Azure Speech Services (speech-to-text and text-to-speech)
        +--> SMTP email service (OTP and password recovery)
```

The Android client uses Retrofit for API communication, Gson for JSON
serialization, AndroidX and Material Components for the UI, Glide for image
loading, and Android media playback APIs for audio streaming.

## Repository structure

```text
.
├── app/
│   └── src/main/
│       ├── java/com/example/audiobook_for_kids/
│       │   ├── api/              # Retrofit client and API definitions
│       │   ├── auth/             # Session and token handling
│       │   ├── model/            # Request, response, and domain models
│       │   ├── repository/       # Data access and observable state
│       │   ├── service/          # Audio playback management
│       │   └── ...Activity.java  # Application screens
│       └── res/                  # Layouts, strings, images, and themes
├── audiobook-backend/
│   ├── controllers/              # Request handling and business logic
│   ├── models/                   # Mongoose schemas
│   ├── routes/                   # REST API routes
│   ├── services/                 # AI, speech, audio, and storage services
│   ├── seed/                     # Sample book and audio data
│   └── server.js                 # Express application entry point
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew.bat
```

## Requirements

- Android Studio with Android SDK 36.
- An Android device or emulator running Android 7.0 (API 24) or newer.
- Java 11 for the Android build.
- Node.js 18 or newer recommended for the backend.
- A MongoDB database, locally or through MongoDB Atlas.
- Credentials for the external services used by the enabled features.

## Running the backend

1. Open a terminal in the backend directory:

   ```powershell
   cd audiobook-backend
   ```

2. Install dependencies:

   ```powershell
   npm install
   ```

3. Create `audiobook-backend/.env`. At minimum, configure the database,
   authentication, and any AI or media services you plan to use:

   ```dotenv
   PORT=5000
   MONGO_URI=mongodb://localhost:27017/audiobook
   JWT_SECRET=replace-with-a-long-random-secret
   REFRESH_TOKEN_SECRET=replace-with-another-long-random-secret

   GOOGLE_API_KEY=your-google-ai-key
   GROQ_API_KEY=your-groq-key
   AZURE_SPEECH_KEY=your-azure-speech-key
   AZURE_SPEECH_REGION=your-azure-region
   AZURE_SPEECH_LANGUAGE=vi-VN
   AZURE_VOICE_NAME=vi-VN-HoaiMyNeural

   CLOUDINARY_CLOUD_NAME=your-cloud-name
   CLOUDINARY_API_KEY=your-cloudinary-key
   CLOUDINARY_API_SECRET=your-cloudinary-secret

   GOOGLE_CLIENT_ID=your-google-client-id
   EMAIL_HOST=smtp.example.com
   EMAIL_PORT=587
   EMAIL_USER=your-email
   EMAIL_PASS=your-email-password
   ```

   Keep secrets in `.env` and do not commit them to source control.

4. Start the server:

   ```powershell
   npm start
   ```

   For development with automatic restarts:

   ```powershell
   npm run dev
   ```

The backend listens on port `5000` by default and exposes route groups under
`/auth`, `/books`, `/audio`, `/activity`, `/ai-content`, and `/ai`.

## Running the Android app

1. Open the repository root in Android Studio.
2. Start the backend and make sure the Android client can reach it.
3. Configure the API base URL in
   `app/src/main/java/com/example/audiobook_for_kids/utils/Constants.java`:

   ```java
   public static final String BASE_URL = "http://10.0.2.2:5000/";
   ```

   Choose the URL that matches your setup:

   - **Android Emulator:** `http://10.0.2.2:5000/` reaches a backend running
     on the development machine.
   - **Physical device:** use the development machine’s LAN address, for
     example `http://192.168.1.10:5000/`, or use a secure tunnel.

   The URL must end with `/` because Retrofit resolves API paths relative to
   the configured base URL. If the project’s device detection remains enabled,
   update the corresponding `LOCAL_BASE_URL` or `NGROK_BASE_URL` constant
   instead of replacing `BASE_URL`.
4. Select an emulator or connected device.
5. Build and run the `app` configuration from Android Studio.

The project can also be assembled from PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

The application requests internet access and microphone access. Microphone
permission is required for voice-based story generation and voice interaction
with the AI assistant.

## API overview

| Route group | Purpose |
| --- | --- |
| `/auth` | Registration, OTP verification, login, Google login, refresh, logout, and password recovery |
| `/books` | Book listing, book details, and title/author search |
| `/audio` | Chapter metadata and audio URLs |
| `/activity` | Favorites, listening history, progress, reviews, and listening events |
| `/ai` | Story generation, voice-to-story generation, story saving, and AI chat |
| `/ai-content` | Access to stories generated and saved by a user |

AI endpoints require JWT authentication. The Android client sends the access
token through the `Authorization` header.

The AI request and response contract is documented in
[`audiobook-backend/docs/api_ai.md`](audiobook-backend/docs/api_ai.md).

## Seed data

The backend includes seed scripts for sample books and audio chapters in
`audiobook-backend/seed/`. Review the scripts before running them and ensure
that your `MONGO_URI` and media configuration are set correctly.

## Development notes

- Do not commit `.env` files, API keys, refresh tokens, or generated private
  audio files.
- Keep the Android API base URL aligned with the backend host and port.
- AI generation and speech processing can take longer than ordinary API calls;
  the Android client uses extended Retrofit/OkHttp timeouts for these
  operations.
- This project is currently configured as a version `1.0` Android application
  with a minimum SDK level of 24.
