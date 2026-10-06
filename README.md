# Dynamic Alarm

A native Android implementation of A.S.M. Kibria's Dynamic Alarm concept: an alarm that opens the next action when you confirm it.

## Install on your phone

1. Copy `output/Dynamic-Alarm-1.2.apk` to your Android phone using USB, Drive, or another file transfer method.
2. Open the APK in Files/My Files. If prompted, allow **Install unknown apps** for that file-opening app, then install. You can revoke that installation permission afterward.
3. Open **Dynamic Alarm → Setup & test alarm**. Allow **Alarms & reminders**, **Notifications**, and **Full-screen alerts**. Android 14 and newer may require each separately.
4. Check system alarm volume and allow alarms in Do Not Disturb. Review your phone's battery settings if alarms are being restricted.
5. Choose **Schedule a test in 15 seconds**, lock your screen, and check the sound, notification, snooze and dismiss controls. Test on your own phone before depending on it.

The APK targets Android 14 (API 34) and can install on Android 10 or later. It contains no CPU-specific native code, so the same APK supports ARM and x86 devices. Newer Android releases and phone manufacturers can change alarm presentation and battery behavior.

## Included

- One-time dates and weekday repeats; create, edit, enable, disable and delete alarms.
- Nine modes: ordinary alarm, call, SMS, email, document/image, directions, installed app, website, morning weather.
- Contact picker for phone/email details, with manual entry as a fallback.
- Persistent access to selected documents through Android's system file picker.
- System alarm tones, per-alarm tone level, vibration, five-minute snooze, dismiss, and a ten-minute ringing timeout with missed-alarm notification.
- Android AlarmManager exact alarm-clock scheduling; schedule recovery after reboot, app update, time-zone changes and reopening the app.
- Weather for a city you enter, with a city-match chooser, temperature, precipitation probability and umbrella/hot/cold suggestions.
- Multiple alarms can have separate notifications and controls. The oldest active alarm supplies the sound; dismissing it switches to the next active alarm.

## Behavior and limitations

Calls open the dialer; SMS and email open a composer. You confirm the call or send in that app. The alarm never sends messages automatically. Secure lock screens must be unlocked before an action opens.

Documents need an installed viewer and a file that is still accessible. Directions open Google Maps directions through an HTTPS link. Weather needs internet; alarms themselves do not. Weather errors do not prevent an alarm from ringing.

There are no accounts, ads, analytics or remote alarm storage. Alarm details are stored in the app's private local preferences. Uninstalling clears them. Document access is limited to files you select. Weather sends the entered city to Open-Meteo when you request a forecast; external actions are then handled by their destination apps.

Alarms cannot ring while the phone is powered off. Force stopping the app prevents alarms until it is reopened. Permissions, zero system alarm volume, Do Not Disturb and vendor battery restrictions can affect operation. A phone in use may show a heads-up notification instead of a full-screen alarm. Past one-time alarms found on schedule recovery are marked inactive instead of unexpectedly ringing late. Repeating alarms retain local clock time; a daylight-saving gap shifts a nonexistent time forward, and an overlap uses its first occurrence.

Your alarm, with a purpose, by Kibria. Suggestions and questions: asmksn@gmail.com.

## Build from source

Requires JDK 21, Android SDK platform 35 and build-tools 35.0.0. No Gradle is required. Leaflet 1.9.4 is bundled for the map; its license is included in assets.

```powershell
.\build.ps1 -Sdk 'C:\path\to\Android\Sdk'
```

The default SDK path is `..\.tools\android-sdk` in this workspace. The script compiles Java and resources, creates DEX, aligns the APK, signs it, verifies its signature, and runs the scheduling tests. Output is `output/Dynamic-Alarm-1.2.apk`.

The personal-build signing key is in `signing/personal-build.jks` with alias `dynamic-alarm` and local development password `android`. Keep this key private and backed up if you want subsequent APKs to update this installation. It is excluded from the source ZIP and is not suitable as a production publishing key. The app itself is not marked debuggable.

## References

- Original design: `../Dynamic Alarm.pdf`.
- [Android alarm scheduling](https://developer.android.com/develop/background-work/services/alarms).
- [Android 14 full-screen intent behavior](https://developer.android.com/about/versions/14/behavior-changes-14#secure-fsi).
- [Open-Meteo weather API](https://open-meteo.com/en/docs) and [geocoding API](https://open-meteo.com/en/docs/geocoding-api). Weather data: Open-Meteo, CC BY 4.0. Place data: GeoNames.

See `TESTING.md` for the verification record and remaining device checks.



## Version 1.2 changes

- Delete an alarm directly from its home card, with confirmation. Deletion cancels its schedule and snooze.
- Search a place/address in the map picker or tap a map point, then choose **Use this destination**. Internet is needed for map tiles and search. A manually entered address remains available as a fallback.
- **Just ring** supports preset morning, Fajr and other quotes, plus any custom text. Quotes appear on the alarm screen and are optional.
- Email alarms can hold up to 10 selected files. The alarm opens an email-capable app with recipient, subject, body and attachments; you review and send there. Email app attachment limits apply.
- Updated footer and feedback composer addressed to asmksn@gmail.com.
- Minimum Android version is now Android 10 (API 29).

Install version 1.2 over the existing installation: it uses the same package and signing key and keeps saved alarms. Do not uninstall first.

Map searches are sent to the [Photon public service](https://github.com/komoot/photon), with explicit search requests, modest rate limiting and cached results. Availability is not guaranteed. Maps use [OpenStreetMap tiles](https://operations.osmfoundation.org/policies/tiles/), ordinary HTTP caching and visible attribution; there is no bulk tile download or offline map mode. Map UI: Leaflet, BSD-2-Clause (license in assets/leaflet-LICENSE.txt). Search/place data: © OpenStreetMap contributors, ODbL. No GPS permission is requested. The app does not send feedback emails automatically.
