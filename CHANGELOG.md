# Changelog

All notable changes to LanText are documented in this file.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and version numbers follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

The F-Droid / Play-style “what's new” text for each Android `versionCode` lives in
[`fastlane/metadata/android/en-US/changelogs/`](fastlane/metadata/android/en-US/changelogs/).

## [Unreleased]

## [0.2.5] — 2026-09-05

Sideload build for testers.

### Added

- Emoji picker on the web composer. Type a Meet-style shortcut such as
  `:lol` and press Tab or Enter to insert. Hover an emoji to see its
  command.
- Settings on the phone: GIF search and voice notes are experimental and
  can be turned off. The computer inbox hides those controls immediately.
- Pin a conversation from the thread header or by right-clicking it in
  the list. Pinned threads stay at the top.
- Copy Number, Details (everyone in the thread), and Download Thread (PDF
  with dates and times). The contact photo opens Details.
- Choosing a web appearance updates the home-screen widget colors.
- Desktop notification permission is requested when the inbox loads.
- Click a picture in a thread to expand it; click or Esc to close.
- GIF search (Openverse / Wikimedia, small enough for MMS) and browser
  voice notes sent as MMS. Paste or attach a `.gif` also works.

### Fixed

- After a full browser restart the inbox no longer stays blank while the
  phone is still coming up: the UI paints immediately, boot fetch times
  out, and a Refresh button reloads the inbox (Shift-click reloads the
  page). Pairing no longer does a full page reload if a cached page is
  missing the Refresh button, and a rejected PIN shows the reason.
- Conversation search now keeps threads that match a keyword in an older
  message (not only the latest snippet), and also searches MMS text.
- Persist the last allowed SSID and its Wi-Fi IPv4 so the listener comes
  back after a process restart when Android hides the network name in the
  background (location is while-in-use only).
- Resume the LAN listener after airplane mode, a MAC change, or a background
  Wi-Fi reconnect. Nearby devices was declared `neverForLocation`, so Android
  hid the SSID while LanText was in the background; the remembered network
  name was also cleared on a momentary disconnect, which left a reverse proxy
  such as `lantext.lan` returning HTTP 502.
- Web inbox: if the phone is unreachable, grey out and show a Reconnect
  button instead of a dead tab. A service worker keeps the page around so
  Brave/Chrome tab restore after the computer sleeps does not replace LanText
  with the browser’s HTTP 502 screen. The overlay waits 20 seconds so a brief
  WebSocket drop does not interrupt typing. The events socket no longer uses
  NanoHTTPD’s 5-second read timeout.
- Emoji typed in the computer inbox (including `:lol`) is sent as Unicode
  instead of four replacement characters.

### Changed

- Remembered SSID is reused only for the same Wi-Fi IPv4, so a hidden name
  cannot follow the phone onto another network. That pair is also stored so
  a process restart does not leave the listener stuck on “waiting for Wi-Fi”.
  Wi-Fi re-evaluation skips RSSI-only callbacks, and the known-SSID list is
  not rewritten on every sighting. Content-Security-Policy `connect-src` is
  `'self'` only.
- Home-screen widget Turn on / Turn off control uses the same pill shape
  as other buttons.

## [0.2.4] — 2026-08-30

Sideload build for testers.

### Fixed

- Keep the LAN listener reachable overnight with no computer connected:
  hold the Wi-Fi lock while web access is listening (not only while a
  browser is open), and keep the foreground service running on an allowed
  network so a reverse proxy such as `lantext.lan` does not return HTTP 502
  after the PC is powered off.

### Changed

- F-Droid draft recipe is exclusive upstream-signed reproducible builds
  for first inclusion (`docs/fdroiddata.yml`: full commit SHA, `subdir: app`,
  Fastlane-only store metadata).

## [0.2.3] — 2026-08-30

Sideload build for testers.

### Fixed

- Home-screen pairing URL, QR, and HTTPS bind use the phone's Wi-Fi
  station address (DHCP / `wlan0`). About still uses a generic example
  (`https://192.168.1.42:8743`); that was never meant to replace the
  live pairing URL.

## [0.2.2] — 2026-08-30

Sideload build for testers. Same developer-signed GitHub release as Point Forecast.

### Added

- About screen shows the app version (`versionName`).

### Fixed

- Saving or updating a contact from the web UI writes into the same
  Google (or other sync) account as existing contacts, so cloud-synced
  address books accept the change.

## [0.2.1] — 2026-08-30

### Changed

- About “For LAN operators” examples use `192.168.1.42` and `lantext.local`.
- Store screenshot 4 is a fictional two-pane inbox for demonstration.

## [0.2.0] — 2026-08-30

Test build for sideload. Same developer-signed flow as Point Forecast.

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
- Configurable listen port on the phone (default 8743, range 1024–65535).
  The HTTPS URL and server bind follow the saved port. The port editor is
  collapsed until you open it.
- Remove a saved Wi-Fi name from the list after turning it off.
- Refresh PIN and QR from the phone while web access is listening.
- About: collapsible permissions, a card for each permission, and a GitHub link.
- Home screen puts the QR and PIN first and is denser so a phone can show it without scrolling.
- About: “For LAN operators” notes (certificate, static IP, reverse proxies).
- Home screen fills the phone height; navigation labels stay on one line.
- Re-register the Wi-Fi callback after permissions are granted so the SSID
  is not stuck as hidden. Location is requested on all versions because
  Android still withholds the network name without it.
- System back / swipe-back on Networks, Computers, and About returns to home.

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
[0.2.5]: #025--2026-09-05
[0.2.4]: #024--2026-08-30
[0.2.3]: #023--2026-08-30
[0.2.2]: #022--2026-08-30
[0.2.1]: #021--2026-08-30
[0.2.0]: #020--2026-08-30
[0.1.0]: #010--2026-08-30
