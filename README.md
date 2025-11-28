# 🎙️ Smart Contact List Updater

An Android application that automatically detects professional titles from phone calls and suggests updating contact names.

## ✨ Features

✅ Listens to first 10 seconds of phone calls  
✅ Detects titles: Dr, Eng, Prof, Mr, Mrs, Ms  
✅ Suggests contact name updates  
✅ 100% FREE backend using Hugging Face Whisper  
✅ Privacy-focused: Audio deleted after processing  

## 📁 Project Structure
```
smart-contact-updater/
├── backend/           # Node.js backend server (FREE API)
└── android/           # Android app (coming soon)
```

## 🚀 Quick Start

### Backend Setup (5 minutes)

1. **Clone the repository:**
```bash
git clone https://github.com/YOUR_USERNAME/smart-contact-updater.git
cd smart-contact-updater/backend
```

2. **Install dependencies:**
```bash
npm install
```

3. **Install FFmpeg:**
   - **Windows:** https://ffmpeg.org/download.html
   - **Mac:** `brew install ffmpeg`
   - **Linux:** `sudo apt install ffmpeg`

4. **Configure and start:**
```bash
cp .env.example .env
npm start
```

5. **Test:**
```bash
curl http://localhost:3000/health
```

✅ **Backend is running!**

### Android Setup

Android app code coming soon! For now, the backend is ready to use.

## 🔧 How It Works

1. 📞 User receives/makes a phone call
2. 🎤 App records first 10 seconds
3. ☁️ Audio sent to backend for transcription
4. 🔍 Backend detects titles using keyword matching
5. 📱 App shows notification to update contact
6. ✅ User taps to update contact name

## 💻 Technology Stack

**Backend:**
- Node.js + Express
- Hugging Face Whisper API (FREE!)
- FFmpeg for audio conversion

**Android (Coming Soon):**
- Kotlin
- MediaRecorder for call recording
- OkHttp for API calls
- ContactsContract for contact management

## 🆓 Free API

This project uses Hugging Face's free Whisper API:
- ✅ No credit card required
- ✅ Unlimited requests (with reasonable rate limits)
- ✅ Good quality transcription
- ✅ No quota tracking

## 🔒 Privacy

- Audio is only recorded during calls
- Recordings are temporary and deleted after processing
- No data is stored on servers
- All processing happens in real-time

## 📄 License

MIT License - Feel free to use and modify!

## 🤝 Contributing

Contributions welcome! Please open an issue or submit a PR.

## 📞 Support

Having issues? Check the [backend README](backend/README.md) for troubleshooting.

---

**Star ⭐ this repo if you find it useful!**
```

4. Replace `YOUR_USERNAME` with your actual GitHub username
5. Click **"Commit changes"**

✅ **Main README updated!**

---

## 🎉 You're Done!

Your repository is now live at:
```
https://github.com/YOUR_USERNAME/smart-contact-updater
