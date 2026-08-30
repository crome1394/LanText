# Reproducible upstream-signed builds (F-Droid)

LanText will use F-Droid’s **reproducible upstream-signed** flow, the same
pattern as [Point Forecast](https://github.com/crome1394/point-forecast)
and many modern F-Droid apps:

1. You publish a signed APK on GitHub: `app-release-signed.apk` on tag `vX.Y.Z`.
2. F-Droid rebuilds that tag from source.
3. If the rebuild matches (except the signature), F-Droid publishes **your** APK.
4. `AllowedAPKSigningKeys` in fdroiddata pins your certificate fingerprint.

This is **prepared in-repo** and **not submitted** until you decide to list
the app. Official docs:
[F-Droid Reproducible Builds](https://f-droid.org/en/docs/Reproducible_Builds/).

## Why

- Same signing identity on GitHub and F-Droid (in-place updates between sources).
- Less trust placed solely in F-Droid’s per-app signing key.
- Transparent proof that the published APK matches the tagged source.

## What is *not* required

Listing on F-Droid does **not** require this. The classic flow (F-Droid builds
and signs) remains valid. Upstream signing is the upgrade we intend to use
from the first listing, so users never have to uninstall/reinstall for a
later key change.

## Release contract

| Item | Value |
|---|---|
| Git tag | `v<versionName>` (semver, e.g. `v0.2.0`) |
| GitHub asset | `app-release-signed.apk` |
| `Binaries` | `https://github.com/crome1394/LanText/releases/download/v%v/app-release-signed.apk` |
| Signer tool | Android **Build Tools 34** `apksigner` (not 35+) |
| Gradle output | **Unsigned** `app-release-unsigned.apk` (do not Gradle-sign release) |
| Application id | `app.lantext` |

## Local tooling

| Script | Purpose |
|---|---|
| `scripts/create-release-keystore.sh` | Create keystore **outside** the repo |
| `scripts/verify-deterministic-build.sh` | Two clean builds; unsigned APK hashes must match |
| `scripts/sign-release.sh` | Sign with BT 34 `apksigner` |
| `scripts/verify-release-signing.sh` | Pin/check cert SHA-256 |
| `scripts/expected-release-cert.sha256` | Committed digest for `AllowedAPKSigningKeys` |

See [RELEASE.md](../RELEASE.md) for the cut checklist.

## Secrets

Never commit:

- The `.jks` / `.keystore` file
- Passwords / `keystore.properties`

`.gitignore` already excludes common keystore paths and `dist/`. Back up the
keystore and passwords in a password manager; losing them permanently breaks
update continuity for `app.lantext`.
