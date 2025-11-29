# Smart Contact Updater - Android App

Android application for detecting titles from phone calls and updating contacts.

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Minimum)
- Target SDK 34

### Step 1: Create New Project

1. Open Android Studio
2. File → New → New Project
3. Select **"Empty Activity"**
4. Configure:
   - **Name:** SmartContactUpdater
   - **Package name:** com.example.smartcontactupdater
   - **Language:** Kotlin
   - **Minimum SDK:** API 24

### Step 2: Copy Files

Copy all files from this directory to your project:
```
app/src/main/
├── AndroidManifest.xml          → Replace existing
├── java/com/example/smartcontactupdater/
│   ├── MainActivity.kt
│   ├── service/
│   │   └── CallDetectionService.kt
│   ├── receiver/
│   │   ├── CallReceiver.kt
│   │   └── UpdateContactReceiver.kt
│   ├── detector/
│   │   └── TitleDetector.kt
│   ├── api/
│   │   └── ApiClient.kt
│   └── utils/
│       ├── ContactManager.kt
│       ├── PreferenceManager.kt
│       └── LogManager.kt
└── res/
    └── layout/
        └── activity_main.xml
```

### Step 3: Update build.gradle

Copy the dependencies from `build.gradle` to your `app/build.gradle` file.

### Step 4: Sync and Build

1. Click **"Sync Now"** when prompted
2. Build → Make Project
3. Wait for Gradle sync to complete

### Step 5: Run

1. Connect Android device or start emulator
2. Run → Run 'app'
3. Grant all required permissions
4. Configure server URL in settings

## 📱 Configuration

### In-App Settings

1. Open app
2. Tap **"Settings"**
3. Enter your backend server URL:
   - Local testing: `http://10.0.2.2:3000` (emulator)
   - Real device: `http://YOUR_COMPUTER_IP:3000`
4. Default API key is already set: `mySecretKey123456`

## 🔑 Required Permissions

The app requires these permissions:
- ✅ RECORD_AUDIO - Record call audio
- ✅ READ_CONTACTS - Read contact names
- ✅ WRITE_CONTACTS - Update contact names
- ✅ READ_PHONE_STATE - Detect phone calls
- ✅ FOREGROUND_SERVICE - Run in background
- ✅ INTERNET - Connect to backend
- ✅ POST_NOTIFICATIONS - Show update notifications

All permissions are requested at runtime.

## 🎯 How to Use

1. **Enable Service:**
   - Open app
   - Toggle **"Enable Smart Updates"** ON
   - Grant all permissions

2. **Make/Receive Call:**
   - The app will automatically record first 10 seconds
   - Audio is sent to backend for transcription

3. **Title Detection:**
   - If a title is detected (Dr, Eng, Prof, etc.)
   - You'll see a notification

4. **Update Contact:**
   - Tap notification to update the contact
   - Contact name will be updated with the detected title

## 🧪 Testing

### Test Without Real Calls

Add this test button to MainActivity:
```kotlin
// In onCreate, after setupListeners()
Button(this).apply {
    text = "🧪 Test Detection"
    setOnClickListener {
        val detector = TitleDetector()
        val result = detector.detectTitle("hello doctor smith")
        Toast.makeText(this@MainActivity, "Detected: $result", Toast.LENGTH_LONG).show()
    }
}.also { 
    // Add to your layout
}
```

### Check Logs
```bash
adb logcat | grep SmartContact
```

## 🐛 Troubleshooting

### App Crashes on Start
- Check all files are in correct directories
- Verify package names match
- Check build.gradle dependencies

### Service Not Starting
- Verify all permissions granted
- Check service enabled in settings
- Look at logcat for errors

### Recording Not Working
- Some devices restrict call recording
- Check RECORD_AUDIO permission
- Try on different device/emulator

### Cannot Connect to Backend
- Check server URL is correct
- Backend must be running
- Use correct IP for real devices
- Test backend separately with curl

### No Title Detected
- Check backend logs
- Verify audio was uploaded
- Test with clear pronunciation
- Check supported titles list

## 📋 Supported Titles

- Doctor / Dr
- Engineer / Eng
- Professor / Prof
- Mr / Mister
- Mrs / Misses
- Miss / Ms

## 🔒 Privacy

- Audio is recorded only during calls
- Recordings are temporary (deleted after processing)
- No data stored on device or server
- All processing happens in real-time

## 📚 Project Structure
```
android/
├── MainActivity.kt              # Main UI
├── service/
│   └── CallDetectionService.kt # Background service
├── receiver/
│   ├── CallReceiver.kt         # Phone state listener
│   └── UpdateContactReceiver.kt # Contact update handler
├── detector/
│   └── TitleDetector.kt        # Keyword detection
├── api/
│   └── ApiClient.kt            # Backend API calls
└── utils/
    ├── ContactManager.kt       # Contact operations
    ├── PreferenceManager.kt    # App settings
    └── LogManager.kt           # Activity logging
```

## 🤝 Contributing

Found a bug or want to add features? Please open an issue or submit a PR!

## 📄 License

MIT License - See main project README
