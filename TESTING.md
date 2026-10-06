# Verification record

Build date: 2026-10-05. Runtime: Android 14 / API 34, official AOSP x86_64 emulator image r04. Physical phone testing remains necessary for manufacturer-specific alarm behavior.

## Passed

- Java/resource compilation, DEX generation and APK alignment.
- APK signature verification: v2 and v3, one signer. Release APK is not debuggable.
- Package metadata: `com.kibria.dynamicalarm`, version 1.0, minSdk 26, targetSdk 34, no native ABI restrictions.
- Ten JVM scheduling checks: future time, time already reached, Monday/Sunday masks, weekday skipping, year rollover, spring/fall clock changes and a nonexistent daylight-saving time.
- Seventeen Android instrumentation assertions: app launch, alarm persistence with Unicode and multiline content, exact trigger registered in AlarmManager, scheduled receiver firing, foreground ringing state, one-time disable, five-minute snooze persistence and Android scheduling, repeating next occurrence, overlapping alarms, dismissal isolation, sound-source selection, final dismissal and expired-alarm recovery.
- Interactive editor: entered an alarm title, selected Morning weather, entered Dhaka and saved. The home card showed the correct details and Android's alarm service showed the exact requested trigger time.
- Visually inspected the home and editor screens at 1080 × 1920 / 420 dpi. No clipped controls or overlapping text; the editor scrolls to remaining settings.
- Weather API response verified for the fields used by the app.

## Reproduce

```powershell
.\build.ps1 -Sdk 'C:\path\to\Android\Sdk'
.\test-android.ps1 -Sdk 'C:\path\to\Android\Sdk'
```

The instrumentation script uses only `emulator-5554`, installs the app and a separately signed test package, grants alarm/notification access and clears the disposable emulator's app alarms. Do not adapt it to a real phone that contains alarms you wish to keep.

## Check on your phone

Use the built-in 15-second test with the phone locked, then test snooze, screen-off and battery-saving conditions. Check system alarm volume and Do Not Disturb. The emulator runs with host audio muted, so it verifies alarm/service behavior rather than physical speaker output or vibration.

Also check your preferred dialer, SMS/email app, document viewer and navigation app. Intent destinations vary by installed apps. Sending messages and making real calls were intentionally not part of testing. Secure-lock PIN/biometric handoff, prolonged Doze, actual reboot recovery, vendor battery managers and Android 15/16 have not been device-tested.

On first boot the emulator showed Android system/System UI responsiveness dialogs. Their logged timestamps preceded app interaction; no Dynamic Alarm crash was logged. Those startup dialogs were dismissed before visual checks.

Final interactive check: the saved 08:17 weather alarm woke the sleeping emulator and displayed RingActivity. Its action dismissed the foreground alarm, opened the city chooser, and loaded the Dhaka forecast with an umbrella suggestion. The ring and weather screens were visually inspected.


Version 1.1 packaging verification: resources and DEX stored uncompressed, ZIP CRC and alignment valid; v1/v2/v3 signatures verified (minimum verification API 21). Same signing key and package; versionCode increased to 2. Streamed update installation, clean installation, and cold launch passed on the Android 14 emulator. S24 installation failure cause remains unconfirmed without device logs. Universal APK size: 82,479 bytes. No model-specific native libraries.


## Version 1.2 verification

- Minimum SDK 29 (Android 10), target SDK 34, versionCode 3; same package and signing certificate as the working 1.1 installation.
- Build and ten timing tests pass. ZIP alignment and v1/v2/v3 signing checks pass.
- 34 Android 14 runtime assertions passed, including legacy alarm migration, quote persistence and rendering, home delete confirmation and cancellation, attachment lists/MIME/read flags, and valid destination coordinates.
- Separate map runtime check passed: bundled Leaflet, live map tiles, live Photon search for Dhaka, selecting a result, and enabling native destination confirmation. Map screenshot was visually inspected.
- Separate attachment runtime check passed: the email-only chooser found a test email app, and that app (a different Android UID) read two real content-URI attachments through temporary read grants. No email was sent.
- App source, resources and map assets contain no removed manufacturer branding. Requested footer and feedback address are included.
- Device-specific email applications and Android 10 hardware have not been individually tested. Tests ran on Android 14; the minimum install requirement is Android 10.

Additional test runners:
`test-android.ps1 -Runner MapSmokeTest`
`test-android.ps1 -Runner AttachmentSmokeTest`
