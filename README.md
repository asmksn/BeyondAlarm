# Beyond Alarm

Your alarm, with a purpose.

A native Android implementation of A.S.M. Kibria's Beyond Alarm concept: an alarm that opens the next action when you confirm it.

## Install on your phone

1. Copy `output/Beyond-Alarm-Universal-2.0.apk` to your Android phone using USB, Google Drive, or another file transfer method.
2. Open the APK in Files / My Files. If prompted, allow **Install unknown apps** for that app, then install.
3. Open **Beyond Alarm -> Setup & test alarm**. Allow **Alarms & reminders**, **Notifications**, and **Full-screen alerts**.
4. Check system alarm volume and allow alarms in Do Not Disturb. Review battery optimization settings if needed.
5. Choose **Schedule a test in 15 seconds**, lock your screen, and test sound, notification, snooze, and dismiss controls.

The APK targets Android 16 (API 36) and runs smoothly on Android 10 or later (API 29+). Universal APK supporting ARM and x86 devices.

## Features

- **Nine Action Modes with Vibrant Visual Shades & Icons**:
  - 🔔 Just ring
  - 📞 Call someone (opens phone dialer)
  - 💬 Send a message (SMS draft)
  - ✉️ Send an email (prepares email with attachments)
  - 📄 Open a document (PDF or file viewer)
  - 📍 Get directions (Google Maps route navigation)
  - 📱 Open an app (launches installed app)
  - 🌐 Open a website (opens URL in browser)
  - ☀️ Morning weather (local forecast)
- **Modern UI**: Oval-pill action selector, custom color-coded badges, and compact single-line controls.
- **Chronological Sorting**: Alarms ordered chronologically by set time of day.
- **Curated & Custom Quotes**: Built-in categories including authentic **Islamic** quotes (Quran & Hadiths), Morning motivation, A fresh start, Small steps, Time for yourself, Gratitude, and Focus. Custom quote support.
- **Persistent document access** through Android system file picker.
- **System alarm tones**, per-alarm volume, vibration, 5-minute snooze, dismiss, and missed-alarm notification.
- **Reliable Scheduling**: Android AlarmManager exact alarm scheduling with reboot recovery.
- **Private & Lightweight**: No ads, accounts, or analytics. Everything stays strictly on your device.

## Build from Source

Requires JDK 21, Android SDK platform 36, and build-tools 35.0.0. No Gradle required.

```powershell
.\build.ps1 -Sdk 'D:\KS\App idea\.tools\android-sdk'
```

Output is `output/Beyond-Alarm-Universal-2.0.apk`.

## Author & Feedback

Developed by A.S.M. Kibria.  
Suggestions and questions: `asmksn@gmail.com`
