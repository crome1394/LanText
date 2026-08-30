# F-Droid

LanText is meant to be built and shipped by F-Droid. It will not go on Google
Play: Play rejects SMS permissions unless the app is the default SMS handler,
and becoming that handler is the opposite of this project.

**No listing has been submitted yet.** Do not open an fdroiddata merge request
until a tagged GitHub APK has been tested on device.

Releases follow [Semantic Versioning](https://semver.org/) (`versionName`)
with a monotonically increasing Android `versionCode`. Git tags are
`v<versionName>` (example: `v0.2.3`).

The first inclusion is **exclusive upstream-signed reproducible** builds
(`Binaries` + `AllowedAPKSigningKeys`). Official docs encourage this for
**new** apps so users never uninstall/reinstall for a later key change:
[Submitting to F-Droid Quick Start Guide](https://f-droid.org/docs/Submitting_to_F-Droid_Quick_Start_Guide/)
(“Setup Reproducible Build”),
[Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds/),
[FAQ: Can APKs signed by my key be included?](https://f-droid.org/docs/FAQ_-_App_Developers/).

F-Droid rebuilds the listed commit from source. On match they publish the
GitHub APK with **our** signature. On mismatch that version is **skipped**
(they will not fall back to an F-Droid signing key). Details:
[docs/REPRODUCIBLE_BUILDS.md](docs/REPRODUCIBLE_BUILDS.md).

## Listing files (in this repo)

Store text and graphics live **only** here. Do not copy them into fdroiddata.

```
fastlane/metadata/android/en-US/
  title.txt
  short_description.txt          # max 80 characters
  full_description.txt           # max 4000 characters, basic HTML
  changelogs/<versionCode>.txt   # max 500 characters
  images/icon.png                # 512×512 — required (avoid F-Droid’s default icon)
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
| Build | Gradle, JDK 17, `subdir: app`, no `output:` |
| Signing | Exclusive upstream-signed RB (`Binaries` + `AllowedAPKSigningKeys`) |
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

## `fdroiddata` recipe

The YAML to copy is [docs/fdroiddata.yml](docs/fdroiddata.yml)
(`metadata/app.lantext.yml` on the fdroiddata side).

Hard rules (from F-Droid norms and the Point Forecast review):

1. `Builds.commit` is a **full git SHA**, never a tag or branch
   (`git rev-parse vX.Y.Z^{}`).
2. `subdir: app` (this repo’s Android module). No `output:`.
3. `gradle: [yes]`.
4. No summary, description, screenshots, or changelogs in the YAML.
   Fastlane in **this** repo is the store copy.
5. `versionName` / `versionCode` / `CurrentVersion` / `CurrentVersionCode`
   must match the listed commit and `app/build.gradle.kts`.
6. `Binaries:` is multiline. Asset name on GitHub is exactly
   `app-release-signed.apk`.
7. `AllowedAPKSigningKeys` matches
   [scripts/expected-release-cert.sha256](scripts/expected-release-cert.sha256).
8. One clean tagged release you are happy with. Do not bump versions
   mid-review unless a maintainer asks.

The draft currently lists **0.2.3** (`versionCode` 5), commit
`6b42261de4fea3e043d22db69b5b0e3165a29763`. If you tag a newer release
before the MR, update those fields first.

F-Droid clones **this GitHub repository**. You do not move the project to
GitLab; GitLab is only where the packaging recipe is reviewed.

## Merge request

When testing is done:

1. Confirm the GitHub release for that tag has `app-release-signed.apk`.
2. Confirm `scripts/expected-release-cert.sha256` matches
   `AllowedAPKSigningKeys`.
3. Fork [fdroiddata](https://gitlab.com/fdroid/fdroiddata), copy
   [docs/fdroiddata.yml](docs/fdroiddata.yml) to `metadata/app.lantext.yml`.
4. Title: `New app: LanText (app.lantext)`
5. Allow maintainer commits.
6. Do not open a second MR.

Suggested description:

```
LAN-only HTTPS companion for SMS/MMS on Android 8+. Apache-2.0.
Does not become the default SMS app.

Store metadata (title, descriptions, icon, screenshots, changelogs) is
Fastlane in the upstream repo.

First inclusion uses exclusive upstream-signed reproducible builds
(Binaries + AllowedAPKSigningKeys). Gradle assembleRelease is unsigned;
GitHub APKs are signed with Android Build Tools 34 apksigner.

Location and Nearby devices are only so Android will reveal the Wi-Fi
SSID for the allowlist. Not GPS; not intended as a tracking Anti-Feature.
```

If a maintainer asks to include an older skipped version, only say yes if
that APK should still be published; otherwise the listed tag supersedes it.

## Binary / reproducible builds

`assembleRelease` must produce an **unsigned** APK. Sign it with Android
Build Tools **34** `apksigner` (`scripts/sign-release.sh`). Do not use
Build Tools 35+ for the F-Droid-bound APK.

`assembleRelease` uses R8; keep ProGuard rules in `app/proguard-rules.pro`
if a future version needs extra keep rules for the PDU composer or NanoHTTPD.
If a rebuild fails to match, see
[F-Droid’s APK diff HOWTO](https://gitlab.com/fdroid/wiki/-/wikis/HOWTO:-diff-&-fix-APKs-for-Reproducible-Builds).
