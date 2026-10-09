# Alarm Next

Your alarm, with a purpose.

A native Android implementation of A.S.M. Kibria's Alarm Next concept: an alarm that opens the next action when you confirm it.

## Install on your phone

1. Copy `output/Alarm-Next-Universal-2.0.apk` to your Android phone using USB, Drive, or another file transfer method.
2. Open the APK in Files/My Files. If prompted, allow **Install unknown apps** for that file-opening app, then install. You can revoke that installation permission afterward.
3. Open **Alarm Next → Setup & test alarm**. Allow **Alarms & reminders**, **Notifications**, and **Full-screen alerts**. Android 14 and newer may require each separately.
4. Check system alarm volume and allow alarms in Do Not Disturb. Review your phone's battery settings if alarms are being restricted.
5. Choose **Schedule a test in 15 seconds**, lock your screen, and check the sound, notification, snooze and dismiss controls. Test on your own phone before depending on it.

The APK targets Android 16 (API 36) and can install on Android 10 or later. It contains no CPU-specific native code, so the same APK supports ARM and x86 devices.

## Features

- **Chronological Time-of-Day Sorting**: Alarms are ordered 24-hour time wise (e.g., 4:00 AM on top, 6:00 PM in the middle, 11:00 PM at the bottom).
- **Curated & Custom Quotes**: Built-in categories including authentic **Islamic** quotes (Quran & Hadiths), Morning motivation, A fresh start, Small steps, Time for yourself, Gratitude, and Focus with alternating highlighting cards. You can also write your own custom quotes.
- **Nine Action Modes**: Ordinary alarm, call, SMS, email, document/image, directions, installed app, website, morning weather.
- **Contact picker** for phone/email details, with manual entry fallback.
- **Persistent document access** through Android's system file picker.
- **System alarm tones**, per-alarm tone level, vibration, 5-minute snooze, dismiss, and 10-minute ringing timeout with missed-alarm notification.
- **Android AlarmManager** exact alarm scheduling; recovery after reboot, app update, time-zone changes, and reopening the app.
- **Morning weather** for your chosen city using Open-Meteo.
- **Interactive Map Search** with Photon and OpenStreetMap tiles.
- **No ads, accounts, or analytics**: Everything is stored locally on your device.

## Version 2.0 Changes

- Updated app name to **Alarm Next** with tagline *"Your alarm, with a purpose."*
- Brand new modern high-resolution launcher icon.
- Added **Islamic** quote category with 13 authentic Quranic verses and Hadiths.
- Added alternating 2-color shaded card highlighting for quote browsing.
- Alarms list ordered chronologically by set time of day.
- Single-line compact footer in Privacy & About dialog.

## Build from Source

Requires JDK 21, Android SDK platform 36 and build-tools 35.0.0. No Gradle is required.

```powershell
.\build.ps1 -Sdk 'D:\KS\App idea\.tools\android-sdk'
```

Output is `output/Alarm-Next-Universal-2.0.apk`.

## Author & Feedback

Developed by A.S.M. Kibria.  
Suggestions and questions: `asmksn@gmail.com`
