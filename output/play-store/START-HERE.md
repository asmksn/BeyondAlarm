# Publish Dynamic Alarm on Google Play

Prepared 5 October 2026 for Kibria. This guide accompanies version 1.3, version code 4, package `com.kibria.dynamicalarm`. It supports Android 10+ and targets Android 16 (API 36).

## 1. Know which file to use

Upload **Dynamic-Alarm-1.3.aab** in Play Console. An AAB is an Android App Bundle: Google converts it into the right APK for each phone. You cannot tap an AAB to install it on your phone.

The separate **Dynamic-Alarm-Universal-1.3.apk** is for direct installation and updates your existing personal build. Do not upload the ZIP publishing kit, APK, source code, password or signing key as the Play release.

The AAB is signed with a separate upload key. Choose Google's generated app-signing key when setting up Play App Signing. Play-installed copies will then have a different signature from the old APK you installed manually. They cannot update that old copy directly. Before switching your phone to the Play version, record your alarm settings; uninstalling deletes those local alarms. Alternatively, use a second phone for Play testing. Keep using the supplied APK if you only want an update to your current installation.

## 2. Create your developer account

1. Visit https://play.google.com/console/signup using the Google account you intend to keep for this app.
2. Choose a personal account if publishing as yourself; use an organization account only if you have an eligible organization and its required documents.
3. Google charges a one-time US$25 registration fee. Complete Google's identity, contact and payment verification. Follow any device-verification instructions in the Play Console Android app.
4. Choose your public developer name carefully and review which verified details Google will display. Use your real information when verification asks for it.

Your Google password and payment details are not needed by this project. Enter them only in Google's own pages.

## 3. Create the app entry

In Play Console choose **Create app**. Suggested starting values:

| Setting | Value |
|---|---|
| App name | Dynamic Alarm |
| Default language | English |
| App or game | App |
| Free or paid | Free for this build |
| Category | Tools |
| Support email | asmksn@gmail.com |
| Ads | No |
| App access | All functionality is available without login |

Read and accept the declarations yourself. The package name becomes fixed after your first accepted upload. Google checks whether it is available; if it is already owned by someone else, a new package/build is needed.

This build uses free public map/search and weather services. Open-Meteo's free service is for non-commercial use; adding ads, subscriptions, payment or another commercial use requires revisiting the service plan. Photon and OpenStreetMap also impose acceptable-use limits. Review service capacity before a large launch.

## 4. Publish the privacy policy

The kit contains **privacy-policy.html**, matching the policy included in the app. It must be available at a public HTTPS web address before submission; a file on your PC is not a public URL.

An easy route is Google Sites: create a site, add a Privacy Policy page, paste the readable text from the supplied HTML file, publish the site with public access, and copy that page's URL. Alternatively, host the HTML on your own site or GitHub Pages. Open the published URL in a signed-out/private browser to confirm it is readable without login. Paste that URL into Play Console's Privacy policy field. Keep the app name and contact details consistent with your listing. The policy is not hosted by this project yet.

## 5. Fill in the store listing

Use the supplied **store-listing.txt**, **icon-512.png**, **feature-graphic-1024x500.png**, and actual phone screenshots. Review the wording before publishing. Google currently asks for a 512×512 icon, a 1024×500 feature graphic, and at least two phone screenshots; follow any extra device-format requirements shown by your Console.

Use screenshots from this app version with no test notifications or personal information. The app does not calculate Fajr prayer times: it lets you set the time yourself and select the Fajr quote. Do not advertise automatic prayer-time calculation or automatic email sending.

## 6. Complete App content

Find **Policy and programs / App content** or the equivalent Dashboard tasks; menu names can change.

- **App access:** no account or login is required. Reviewers must allow notifications, exact alarms and full-screen alarm access in Setup to test ringing. External actions need a suitable installed app.
- **Ads:** no ads in this build.
- **Content rating:** complete the IARC questionnaire honestly for this alarm/utility app. Google generates the rating; do not invent one.
- **Target audience:** select the ages you actually intend to serve. This app is not specifically designed as a children's app. Do not select younger children merely to increase reach.
- **Privacy policy:** enter the public HTTPS URL from step 4.
- **Data safety:** use the data-flow worksheet below. Do not select “no data collected” solely because alarms are saved locally.
- **Full-screen intent:** declare that setting and ringing user-created alarms is the app's core function. The permission opens the ringing screen over the lock screen when permitted.
- **Foreground service:** declare the media playback service, which plays the selected alarm sound while the alarm is ringing. It has a visible ongoing notification with snooze/dismiss actions and stops when dismissed or after the timeout. Upload a demonstration video if the form asks for one.
- Complete other applicable forms shown by Google, including health, government, financial or news declarations according to the app's actual functionality; it is an alarm utility.

Suggested permission explanation: “Dynamic Alarm lets users schedule alarms. At the selected time it plays an audible alarm using a foreground media playback service and shows an alarm notification/full-screen ringing screen. Users can snooze or dismiss the alarm from the screen or notification. The service runs only while an alarm is ringing and has a ten-minute timeout.”

For the video, record: create an alarm one minute ahead → grant required access → lock the screen → alarm rings → snooze/dismiss from the visible controls. Show actual behavior; do not simulate it. A video has not been recorded for you.

### Data safety worksheet — review before submitting

| Feature | Data flow | What to review in the form |
|---|---|---|
| Alarm settings, quotes, recipients, drafts, chosen app | Stored locally; no developer server or analytics | On-device-only data is not off-device collection |
| Map search | Search text goes to Photon; its operator receives IP/network metadata | Search history and any personal/location information entered; optional, app functionality |
| Map display | OpenStreetMap receives requested tile coordinates and IP/network metadata | Viewed map area and possible inferred approximate location; optional, app functionality |
| Weather | Open-Meteo receives city search, then selected city coordinates, plus IP/network metadata | Search history/location-related data; optional, app functionality |
| Email/SMS/navigation/file actions | User-requested handoff to the selected external app | Check Google's user-initiated transfer exception; no automatic sending |
| Feedback email | Your support mailbox receives the email and any content voluntarily sent | Email address, support message and attachments; customer support, with deletion by email request |

The app does not read GPS or collect advertising identifiers. A searched city/map area is not necessarily the user's current location. All built-in network endpoints use HTTPS. Independent services may retain logs, so do not claim all processing is ephemeral or promise deletion from their servers without evidence. Google's definitions and each provider's retention practices determine the final data-type/collection/sharing answers. This worksheet is an implementation audit, not a pre-approved declaration. Verify provider practices before the public submission. There is no account creation, so an account-deletion feature is not applicable to this build; local app data can be cleared and support-correspondence deletion requested by email.

## 7. Upload to internal testing first

1. Open **Test and release → Testing → Internal testing**.
2. Create a release and enroll in **Play App Signing**. Let Google generate the app-signing key; retain your separate upload key locally.
3. Upload **Dynamic-Alarm-1.3.aab**. Wait for Google to process it. Version name should be 1.3 and version code 4.
4. Paste the release notes from the listing file. Save and review the release. Resolve any errors shown by Google before rolling it out to internal testers.
5. Add your Google account to the tester list, copy the opt-in link, join the test and install from Play. Internal testing does not count as the required closed test.
6. Review the pre-launch report. On real phones test a locked-screen alarm, sound/vibration, snooze, reboot restoration, multiple alarms, quotes, map search and email attachments. Check Android 10 and Android 15/16 behavior as well as your own device. Battery restrictions and user permissions affect alarm behavior.

## 8. Closed testing and public release

For personal developer accounts created after 13 November 2023, Google currently requires **at least 12 testers opted in continuously for at least 14 days** in a closed test. Recruit genuine testers, share the opt-in link and have them use the app and report issues. Keep a record of their feedback and fixes. Internal testers alone do not satisfy this requirement.

After you meet the requirement, use **Apply for production** and answer Google's questions about your testing and readiness. Meeting the count/time requirement does not automatically grant approval. When production access is granted, create/promote a production release, select countries and submit for review. Google decides approval and timing. This package has not been submitted or approved yet.

## 9. Keep your update key safe

Private files are stored outside the publishing kit:

- `DynamicAlarm/signing/play-upload.p12` — upload keystore
- `DynamicAlarm/signing/play-upload-password.txt` — its password
- Key alias: `play-upload`

Back these up securely. Never publish them, commit them to a public repository, or upload them as store listing assets. They are needed to sign future uploads. Google can support upload-key reset if necessary, but keeping your backup is easier. Google holds the separate app-signing key under Play App Signing.

Every future Play upload needs a higher version code (next is at least 5). Reuse the same package name and upload key. Keep a backup of the app source too.

## Official references

- Account setup and fee: https://support.google.com/googleplay/android-developer/answer/6112435
- Testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465
- Target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Data safety definitions: https://support.google.com/googleplay/android-developer/answer/10787469
- Foreground services and full-screen intent: https://support.google.com/googleplay/android-developer/answer/13392821
- App signing: https://developer.android.com/studio/publish/app-signing
- Open-Meteo terms: https://open-meteo.com/en/terms
- Map tile policy: https://operations.osmfoundation.org/policies/tiles/
- Photon service: https://github.com/komoot/photon
