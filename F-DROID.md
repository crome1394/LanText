# F-Droid

LanText is meant to be built and shipped by F-Droid. It will not go on Google
Play: Play rejects SMS permissions unless the app is the default SMS handler,
and becoming that handler is the opposite of this project.

## Listing files (in this repo)

```
fastlane/metadata/android/en-US/
  title.txt
  short_description.txt          # max 80 characters
  full_description.txt           # max 4000 characters, basic HTML
  changelogs/1.txt               # versionCode 1, max 500 characters
  images/icon.png                # 512×512
  images/featureGraphic.png      # 1024×500
  images/phoneScreenshots/
  images/tenInchScreenshots/
```

Rebuild the graphics from `tools/store-art/` with `tools/store-art/render.sh`
if you change branding.

## Inclusion notes for reviewers

| | |
|---|---|
| Application id | `app.lantext` |
| License | Apache-2.0 |
| Categories | Phone & SMS, Internet |
| minSdk / targetSdk | 26 / 36 |
| Build | Gradle, JDK 17, `subdir: app` |
| Anti-Features | None intended |
| Tracking / ads / crash reporting | None |
| Non-free assets or GMS | None |

**Location permission.** `ACCESS_FINE_LOCATION` and (on Android 13+)
`NEARBY_WIFI_DEVICES` are requested so the OS will reveal the current SSID.
The allowlist comparison happens on device. Coordinates are not read. This
should **not** be tagged as a tracking Anti-Feature; please mention the
rationale in the metadata description if users ask.

**No default SMS role.** The app uses public Telephony / SmsManager APIs so
Fossify, Google Messages, or any other default app stays in charge.

**Vendored MMS code.** `com.google.android.mms.pdu_alt` and
`com.klinker.android.send_message.MmsFileProvider` are Apache-2.0 sources
copied from AOSP / klinker / Fossify mmslib so MMS can be composed without
a Maven package that pulls extra logging. See [NOTICE](NOTICE).

## `fdroiddata` template

Copy [docs/fdroiddata.yml](docs/fdroiddata.yml) into an
[fdroiddata](https://gitlab.com/fdroid/fdroiddata) merge request after this
repository has a public clone URL. Replace the `TODO` fields. Builds should
track git tags named `v<versionName>` (first tag: `v0.1.0`).

## Binary / reproducible builds

F-Droid should compile from source. Do not submit a prebuilt APK as the
canonical build. `assembleRelease` uses R8; keep ProGuard rules in
`app/proguard-rules.pro` if a future version needs extra keep rules for the
PDU composer or NanoHTTPD.
