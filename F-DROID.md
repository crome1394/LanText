# F-Droid

LanText is meant to be built and shipped by F-Droid. It will not go on Google
Play: Play rejects SMS permissions unless the app is the default SMS handler,
and becoming that handler is the opposite of this project.

**No listing has been submitted yet.** This file and `docs/fdroiddata.yml` are
the recipe to copy when testing of a **developer-signed** APK is done.

Releases follow [Semantic Versioning](https://semver.org/) (`versionName`)
with a monotonically increasing Android `versionCode`. Git tags are
`v<versionName>` (example: `v0.2.0`).

The intended packaging mode is **reproducible upstream-signed** builds
(`Binaries` + `AllowedAPKSigningKeys`), the same as
[Point Forecast](https://github.com/crome1394/point-forecast). F-Droid rebuilds
the tagged source and, on match, publishes the GitHub APK with **our**
signature. See [docs/REPRODUCIBLE_BUILDS.md](docs/REPRODUCIBLE_BUILDS.md).

## Listing files (in this repo)

```
fastlane/metadata/android/en-US/
  title.txt
  short_description.txt          # max 80 characters
  full_description.txt           # max 4000 characters, basic HTML
  changelogs/<versionCode>.txt   # max 500 characters
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
| Signing | Developer-signed, reproducible (`Binaries` + `AllowedAPKSigningKeys`) |
| Anti-Features | None intended |
| Tracking / ads / crash reporting | None |
| Non-free assets or GMS | None |

**Location and Nearby devices.** `ACCESS_FINE_LOCATION` and (on Android 13+)
`NEARBY_WIFI_DEVICES` are requested so the OS will reveal the current SSID.
LanText does not scan for headphones or other phones, and it does not read
GPS. The allowlist comparison happens on device. This should **not** be tagged
as a tracking Anti-Feature; the store description states the rationale.

**No default SMS role.** The app uses public Telephony / SmsManager APIs so
Fossify, Google Messages, or any other default app stays in charge.

**Vendored MMS code.** `com.google.android.mms.pdu_alt` and
`com.klinker.android.send_message.MmsFileProvider` are Apache-2.0 sources
copied from AOSP / klinker / Fossify mmslib so MMS can be composed without
a Maven package that pulls extra logging. See [NOTICE](NOTICE).

## `fdroiddata` template

When you are ready to list the app:

1. Cut a signed GitHub release (`app-release-signed.apk` on tag `v<versionName>`).
2. Confirm `scripts/expected-release-cert.sha256` matches
   `AllowedAPKSigningKeys` in [docs/fdroiddata.yml](docs/fdroiddata.yml).
3. Copy that YAML to an
   [fdroiddata](https://gitlab.com/fdroid/fdroiddata) merge request as
   `metadata/app.lantext.yml`.
4. Point `Builds.commit` at the tag you just published.

F-Droid clones **this GitHub repository**. You do not move the project to
GitLab; GitLab is only where the packaging recipe is reviewed.

## Binary / reproducible builds

`assembleRelease` must produce an **unsigned** APK. Sign it with Android
Build Tools **34** `apksigner` (`scripts/sign-release.sh`). Do not use
Build Tools 35+ for the F-Droid-bound APK.

`assembleRelease` uses R8; keep ProGuard rules in `app/proguard-rules.pro`
if a future version needs extra keep rules for the PDU composer or NanoHTTPD.
If a rebuild fails to match, see
[F-Droid’s APK diff HOWTO](https://gitlab.com/fdroid/wiki/-/wikis/HOWTO:-diff-&-fix-APKs-for-Reproducible-Builds).
