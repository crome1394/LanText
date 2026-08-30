# Changelog

All notable changes to LanText are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and version numbers follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The F-Droid / Play-style “what's new” text for each Android `versionCode` lives in
[`fastlane/metadata/android/en-US/changelogs/`](fastlane/metadata/android/en-US/changelogs/).

## [Unreleased]

### Added

- Web appearance picker: Auto / Light / Dark, plus nine palettes (Fern, Ocean,
  Dusk, Ember, Slate, Sakura, Meadow, Nord, Contrast). Auto follows the system
  color scheme; each palette has a light and dark variant.
- Save an unknown number from a thread as a new contact, or add it to an
  existing contact (second phone). Requires contacts write permission.
- In-app “Why these permissions” section (About), including why location and
  Nearby devices are requested only so Android will reveal the Wi-Fi name.
- When a required permission is missing, a popup explains each one, with a
  Grant all button. The home-screen grant button is gone.

### Removed

- Conversation delete in the web UI. Android only lets the default SMS app
  delete threads, so the control could not work as a companion.

## [0.1.0] — 2026-08-30

First public release. Companion app for Android 8+ that serves a private
HTTPS inbox on Wi-Fi you allow, without becoming the default SMS app.

### Added

- LAN-only HTTPS server (NanoHTTPD + TLS) bound to the phone's Wi-Fi address on port 8743
- SSID allowlist: the server starts only on networks you pick, and stops when you leave
- Pairing with an 8-digit PIN plus an Approve / Deny prompt on the phone
- Device-generated certificate; fingerprint shown in the app for the browser warning
- Session tokens stored hashed; paired computers can be revoked
- Two-pane web UI: conversation list, thread, search, contacts, new message
- SMS send and receive through the system Telephony provider
- MMS pictures: attach, drag-and-drop, or paste; sent via `SmsManager.sendMultimediaMessage`
- Clickable `http(s)` links in message bodies
- Live inbox over WebSocket
- Browser desktop notifications for new texts
- Keyboard shortcuts (`Enter` to send, `Ctrl+N`, `Ctrl+K`, `J`/`K`, `?`)
- Home-screen widget to turn web access on or off
- Conversation delete from the web UI (Android may still block this unless LanText is the default SMS app)
- Foreground service only while web access is on and the phone is on an allowed SSID
- F-Droid Fastlane metadata and project documentation

[Unreleased]: #unreleased
[0.1.0]: #010--2026-08-30
