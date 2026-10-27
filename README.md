# Beyond Alarm

**Your alarm, with a purpose.**

A native Android implementation of A.S.M. Kibria's Beyond Alarm concept: an alarm that opens the next action when you confirm it.

---

### 📥 Direct Downloads

- **[Download Beyond Alarm APK (v2.0 Universal)](Beyond-Alarm-Universal-2.0.apk)**  
  *(Direct Link: [Download Raw APK](https://github.com/asmksn/BeyondAlarm/raw/main/Beyond-Alarm-Universal-2.0.apk))*
- **[Download Complete Visual Documentation (.docx)](Beyond-Alarm-Complete-Documentation.docx)**  
  *(Comprehensive Product Guide with Screenshots)*

---

## 📱 How to Install on Android

1. Download **[Beyond-Alarm-Universal-2.0.apk](https://github.com/asmksn/BeyondAlarm/raw/main/Beyond-Alarm-Universal-2.0.apk)** directly to your Android device (or copy via USB/Drive).
2. Tap the downloaded file in your browser downloads or **Files / My Files** app.
3. If prompted, enable **"Install unknown apps"** for your browser or file manager, then tap **Install**.
4. Open **Beyond Alarm** -> Tap **Setup & test alarm**. Grant the standard Android permissions:
   - **Alarms & reminders** (exact timing)
   - **Notifications** (heads-up alerts)
   - **Full-screen alerts** (over-lockscreen ringing screen)
5. Tap **Schedule a test in 15 seconds**, lock your phone, and experience the one-tap action launch firsthand!

*Compatibility: Supports Android 10 through Android 16 (API 29 to API 36). Universal build supporting ARM64, ARMv7, and x86_64.*

---

## ✨ Key Features

- **9 Visual Action Modes (Oval-Pill Selector)**:
  - 📞 **Call someone**: Pre-loads phone number into the dialer.
  - ✉️ **Send an email**: Pre-fills recipient, subject, body & attachments.
  - 📍 **Get directions**: Opens Google Maps navigation directly to your destination.
  - 💬 **Send a message**: Ready SMS text message draft.
  - 📱 **Open an app**: Immediately launches your chosen app (fitness, prayer, notes).
  - ☀️ **Morning weather**: Live local weather forecast and temperature for your city.
  - 📄 **Open a document**: Directly displays doctor prescription, checklist, or PDF.
  - 🌐 **Open a website**: Opens news portal or specific URL in your browser.
  - 🔔 **Just ring**: Standard acoustic alarm with custom volume (10% to 100%).
- **24-Hour Chronological Flow**: Alarms automatically arrange themselves from morning to night.
- **Compact UI**: Single-line side-by-side `[Edit alarm | Delete alarm]` controls to eliminate clutter.
- **Full-Screen Ringing Mode**: High-legibility 64pt clock, action banner, one-tap lockscreen unlock, 5-min snooze, and 10-minute battery auto-timeout.
- **Built-in Maps & Offline/Online Geocoder**: Search locations with Google Maps and Photon.
- **Quotes & Affirmations Library**: Built-in categories including authentic **Islamic** quotes (Quran & Hadiths), Morning motivation, A fresh start, Small steps, and Focus.
- **100% Private & Ad-Free**: No accounts, no sign-ins, no cloud tracking, and zero ads forever. Everything stays on your phone.
- **Under 1 MB Native Footprint**: Pure native Android Java without bloated cross-platform overhead.
- **Reboot Resilience**: Uses Android Exact AlarmManager with BOOT_COMPLETED recovery.

---

## 🛠️ Build from Source

Requires JDK 21, Android SDK platform 36, and build-tools 35.0.0. No Gradle required.

```powershell
.\build.ps1 -Sdk 'D:\KS\App idea\.tools\android-sdk'
```

Output is generated at `Beyond-Alarm-Universal-2.0.apk` and `output/Beyond-Alarm-Universal-2.0.apk`.

---

## 👤 Author & Feedback

Developed by **A.S.M. Kibria**  
- **GitHub**: [github.com/asmksn/BeyondAlarm](https://github.com/asmksn/BeyondAlarm)  
- **Inquiries & Suggestions**: `asmksn@gmail.com`
