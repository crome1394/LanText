# Security

LanText is an SMS gateway on your LAN. Treat a paired browser as having the
same reach as the SMS and contacts permissions on the phone.

## Report a vulnerability

Please **do not** open a public issue for anything that lets a stranger read
or send texts.

Open a **private** GitHub security advisory on
[crome1394/LanText](https://github.com/crome1394/LanText/security/advisories/new).
Do not attach message bodies. Include:

- Android version and app version (`versionName` / `versionCode`)
- Whether web access was on, and whether the client was already paired
- Steps to reproduce, and whether it works off the allowed SSID

## What the design already assumes

- The HTTPS certificate is self-signed. Users must compare the fingerprint
  shown in the app with the one in the browser. A first-visit intercept on
  the LAN could otherwise serve a lookalike page.
- Pairing is PIN **plus** an on-phone Approve. The PIN is a second factor for
  someone who already has LAN access, not a password for the internet.
- The server binds to the Wi-Fi IPv4 address only and refuses to run off the
  allowlisted SSID. That is the main remote-attack reduction. It is not a
  substitute for a hostile LAN (compromised router, guest isolation off, etc.).
- `ACCESS_FINE_LOCATION` and `NEARBY_WIFI_DEVICES` exist solely so Android
  will disclose the SSID. Nearby devices is not a scan for other hardware.
- `MmsFileProvider` is exported because the system MMS service must read the
  composed PDU. The provider serves files from the app cache by last path
  segment, not arbitrary paths.

## Things that are out of scope as “bugs”

- Android refusing mark-as-read or conversation delete because LanText is not
  the default SMS app. That is platform policy. Delete threads in the default
  messenger.
- The browser warning for the self-signed certificate. That is the feature.
- MMS failing when mobile data is off. Carriers and Android require it.
