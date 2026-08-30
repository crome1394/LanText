# LanText

**Type SMS on a real keyboard. Paste a picture. Nothing leaves your Wi-Fi.**

[![License](https://img.shields.io/badge/license-Apache%202.0-1F8A70.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8%2B-156B57.svg)](#build)
[![F-Droid](https://img.shields.io/badge/F--Droid-planned-156B57.svg)](F-DROID.md)
[![version](https://img.shields.io/badge/version-0.2.1-14201a.svg)](CHANGELOG.md)
[![source](https://img.shields.io/badge/github-crome1394%2FLanText-1F8A70.svg)](https://github.com/crome1394/LanText)

![LanText — SMS from your computer, never leaves your Wi-Fi](fastlane/metadata/android/en-US/images/featureGraphic.png)

LanText is a **companion** for the SMS app you already trust — Fossify Messages,
Google Messages, Samsung Messages, or anything else that uses the system SMS
store. It does **not** become the default messenger.

Install it on the phone, allow your home Wi-Fi, open the HTTPS address in any
browser. You get a two-pane inbox: conversations on the left, the thread on the
right. Texts go out through your carrier. Pictures go out as MMS. The default
app on the phone stays in sync.

There is no account. There is no cloud. Walk off the allowed network and the
server stops.

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/tenInchScreenshots/1.png" alt="LanText web inbox on a computer" width="900">
</p>

## Why this exists

Phones are a miserable place to type. The usual “fixes” are worse:

| | Cloud bridges | Replace the default SMS app | **LanText** |
|---|---|---|---|
| Your texts leave the house | Yes | No | **No** |
| Keep Fossify / Google / Samsung as default | Sometimes | **No** | **Yes** |
| Real keyboard, any OS, any browser | Yes | No | **Yes** |
| Paste a picture, it actually sends | If you pay them | Yes | **Yes** |
| Account / vendor lock-in | Yes | New app owns SMS | **None** |

MightyText, Pulse, AirDroid, and friends are convenient because they copy your
inbox to someone else's servers. LanText copies it to **your laptop**, over
**your** Wi-Fi, with a PIN plus a tap on the phone before any computer is
trusted.

## What it feels like

<p align="center">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" alt="LanText on the phone: URL, PIN, listen port" width="240">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" alt="Permission popup explaining SMS, contacts, Nearby devices, and location" width="240">
  <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" alt="Pairing in the browser" width="240">
</p>

- Two-pane inbox, search, contacts, unread dots
- **Enter** sends, **Shift+Enter** is a new line
- Paste, drop, or attach a picture — it goes out as MMS through the carrier
- Links in messages are clickable
- Desktop notifications when a text arrives
- Save an unknown number as a contact, or add it to someone you already know
- Appearance: Auto, Light, or Dark, plus palettes
- Change the listen port (default 8743) if something else on your LAN uses it
- A home-screen widget flips web access on and off without opening the app

## How it works

1. Install LanText on Android 8 or newer.
2. Grant SMS, contacts, notifications, and Nearby devices (Android 12 and
   older also need location — **only** so the OS will reveal the Wi-Fi name,
   not GPS).
3. Add your home Wi-Fi to the allowlist and turn web access on.
4. On a computer on that same network, open the HTTPS URL shown in the app
   (or scan the QR code).
5. Accept the self-signed certificate **after** checking the fingerprint on
   the phone.
6. Enter the PIN, then tap **Approve** on the phone.

The server binds only to the phone's Wi-Fi IPv4 address, on port **8743**
by default (you can change it in the app), and only while you are on an
allowed SSID. It is not exposed to the cellular network and it is not a
public website.

## Security

- **HTTPS only.** The certificate is generated on the phone.
- **Two-step pairing.** PIN from the app **and** an Approve prompt on the
  phone. A stolen PIN is not enough.
- **Hashed sessions.** Revoke a computer from the app and it is done.
- **SSID gate.** Leave home Wi-Fi and the listener stops. A foreground
  notification tells you when it is up.
- **No telemetry.** See [PRIVACY.md](PRIVACY.md).

Treat pairing like handing someone your unlocked phone. Only approve a
computer you are sitting in front of.

## Battery

The LAN server is a foreground service, and only while web access is enabled
**and** you are on an allowed network. LanText does not poll SMS; it watches
the system provider.

On Xiaomi, Huawei, Samsung, Oppo, and similar devices, set **Battery →
Unrestricted** for LanText. Otherwise the OEM may kill the listener the
moment the screen turns off.

## Limits of not being the default SMS app

This is a deliberate trade: Fossify (or whoever you chose) stays in charge of
the SMS database.

- **Mark-as-read** in the system store may be denied by Android. Delete
  conversations in the default SMS app on the phone.
- **Incoming MMS** is stored by the default messenger. LanText can show it
  once it is on the device.
- **Outgoing MMS** needs mobile data, even if you are on Wi-Fi. That is an
  Android / carrier rule, not LanText being clever.
- Google Play will not accept an app that holds SMS permissions unless it is
  the default SMS app. LanText is built for **F-Droid and sideload**.

## Keyboard shortcuts

Press **?** in the web UI for the full list. On a Mac, use ⌘ in place of Ctrl.

| Key | Action |
|---|---|
| `Enter` | Send |
| `Shift+Enter` | New line |
| `Ctrl+N` | New message |
| `Ctrl+K` or `/` | Search conversations |
| `J` / `K` or `↑` / `↓` | Next / previous conversation |
| `Esc` | Close a dialog or leave the composer |

## Build

```bash
export JAVA_HOME="$HOME/opt/jdk-17"   # JDK 17
export ANDROID_HOME="$HOME/Android/Sdk"
./gradlew :app:assembleDebug
./gradlew :app:installDebug           # debug id: app.lantext.debug
./gradlew :app:assembleRelease        # app.lantext — sign it yourself
```

- minSdk 26 (Android 8), targetSdk 36
- Current version: **0.2.1** (`versionCode` 3)
- Latest git tag: **v0.2.1**
- Application id: `app.lantext`
- License: [Apache 2.0](LICENSE)

Release APKs are **developer-signed** (unsigned Gradle output, then
`apksigner` from Build Tools 34). The keystore is not in this repository.
See [RELEASE.md](RELEASE.md).

## F-Droid

Not listed yet. Store metadata lives in
[`fastlane/metadata/android/en-US/`](fastlane/metadata/android/en-US/).
The planned packaging is developer-signed reproducible builds. Details and
the `fdroiddata` recipe: [F-DROID.md](F-DROID.md).

Tag releases as `v<versionName>` (semver) so F-Droid can follow tags.

## Contribute

Build notes, review expectations, and how to cut a release:
[CONTRIBUTING.md](CONTRIBUTING.md), [RELEASE.md](RELEASE.md).
Security reports: [SECURITY.md](SECURITY.md).

## License

[Apache License 2.0](LICENSE). Third-party notices, including the AOSP-derived
MMS PDU composer, are in [NOTICE](NOTICE).
