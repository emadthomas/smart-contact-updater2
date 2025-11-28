# Smart Contact Updater - Backend

Free speech-to-text backend using Hugging Face Whisper API.

## 🚀 Quick Start

1. **Install dependencies:**
```bash
npm install
```

2. **Install FFmpeg:**
   - **Windows:** Download from https://ffmpeg.org/download.html
   - **Mac:** `brew install ffmpeg`
   - **Linux:** `sudo apt install ffmpeg`

3. **Configure:**
```bash
cp .env.example .env
```

4. **Start server:**
```bash
npm start
```

5. **Test:**
```bash
curl http://localhost:3000/health
```

## ✨ Features

✅ 100% FREE using Hugging Face Whisper  
✅ No API key required (optional for higher limits)  
✅ Unlimited requests  
✅ Good quality transcription  

## 📡 API Endpoints

### Health Check
```bash
GET /health
```

### Transcribe Audio
```bash
POST /transcribe
Headers: x-api-key: mySecretKey123456
Body: multipart/form-data with 'file' field
```

## ⚙️ Configuration

Edit `.env`:
- `PORT=3000` - Server port
- `API_KEY=mySecretKey123456` - Secret key for Android app
- `USE_HUGGINGFACE=true` - Enable free transcription

## 🧪 Testing
```bash
# Download test audio
curl -o test.wav "https://www2.cs.uic.edu/~i101/SoundFiles/gettysburg.wav"

# Test transcription
curl -X POST \
  -H "x-api-key: mySecretKey123456" \
  -F "file=@test.wav" \
  http://localhost:3000/transcribe
```

## 🐛 Troubleshooting

**First request slow?**  
Model loads on first request (~20 seconds), then it's fast!

**FFmpeg not found?**  
Make sure FFmpeg is installed and in your PATH.

**Need higher rate limits?**  
Get a free Hugging Face token: https://huggingface.co/settings/tokens
