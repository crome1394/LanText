# Privacy

LanText has no account, no analytics, no crash reporter, and no advertising.

## What stays on the phone

Messages, contacts, pairing records, the TLS key, and session hashes live in
the Android SMS provider and LanText's private app storage. Backup is disabled
(`allowBackup="false"`).

## What crosses the network

While web access is on **and** you are connected to a Wi-Fi name you allowed:

- The phone serves HTTPS on its **Wi-Fi IPv4 address only** (port 8743).
- A paired browser on that same LAN can read conversations, send SMS/MMS,
  search contacts, create contacts, and add a phone number to an existing
  contact.
- Outgoing SMS and MMS go through your carrier, the same way they would from
  any messenger on the phone.
- GIF search uses Openverse (Creative Commons) from the phone, then the
  chosen GIF is sent as MMS. Voice notes are recorded in the browser and
  sent as AMR over MMS. Neither uses a LanText cloud.

Nothing is uploaded to a LanText server. There is no LanText server.

## Location

LanText requests Nearby devices (Android 13+) and location so Android will
reveal the current SSID, including while the listener runs in the background.
Location is still required for the live Wi-Fi name on current Pixels, even
with Nearby devices. Coordinates are never read, stored, or sent. Nearby
devices is not a scan for headphones, speakers, or other phones.

## Certificates and pairing

The HTTPS certificate is generated on the phone. Pairing requires the PIN shown
in the app **and** an Approve tap on the phone. Session cookies are stored as
hashes, not as reusable secrets on disk in the clear.

## Third parties

F-Droid, GitHub, or whoever hosts the APK may see that you downloaded it. The
app itself does not phone home.
