# Releasing LanText

How to cut a public release (GitHub now; F-Droid later with the same
developer-signed reproducible flow).

Background: [docs/REPRODUCIBLE_BUILDS.md](docs/REPRODUCIBLE_BUILDS.md).
Do **not** open an fdroiddata merge request until you have tested a signed
build and are ready to list the app.

## Prerequisites

- Clean working tree on `main`
- JDK 17+, Android SDK with **Build Tools 34.0.0** (`apksigner`)
- Release keystore **outside** the repo (`scripts/create-release-keystore.sh`)
- Env vars for signing (never commit these):

```bash
export LT_RELEASE_KEYSTORE="$HOME/.local/share/lantext/lantext-release.jks"
export LT_RELEASE_KEY_ALIAS=lantext
export LT_KEYSTORE_PASSWORD='…'
export LT_KEY_PASSWORD='…'
```

## Version numbers (semver)

1. Choose the next `versionName` (`MAJOR.MINOR.PATCH`) and bump `versionCode` by 1
   in `app/build.gradle.kts`.
2. Move `[Unreleased]` notes in [CHANGELOG.md](CHANGELOG.md) under
   `## [X.Y.Z] — YYYY-MM-DD`.
3. Leave a fresh empty `## [Unreleased]` section at the top.
4. Add `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
   (max 500 characters).

## Build (unsigned, deterministic)

Prefer a **fresh clone** of the exact commit/tag you will publish (matches F-Droid).

```bash
./gradlew --no-daemon --no-parallel --no-configuration-cache clean :app:assembleRelease
```

Unsigned output:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

Same-machine determinism smoke test:

```bash
sh scripts/verify-deterministic-build.sh
```

## Sign (Build Tools 34 only)

```bash
mkdir -p dist
sh scripts/sign-release.sh \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  dist/app-release-signed.apk
```

The script verifies the certificate against `scripts/expected-release-cert.sha256`.

**Do not** sign with Build Tools 35+ `apksigner` for F-Droid upstream-signed releases.

## Install smoke test

```bash
adb install -r dist/app-release-signed.apk
# If INSTALL_FAILED_UPDATE_INCOMPATIBLE: uninstall the debug build first
# (`app.lantext.debug` can sit next to release; `app.lantext` cannot).
```

## Git tag and GitHub release

Attach the asset with this **exact** name (F-Droid `Binaries` contract):

```text
app-release-signed.apk
```

```bash
git status   # should be clean
git tag -a vX.Y.Z -m "LanText X.Y.Z"
git push origin main
git push origin vX.Y.Z

gh release create vX.Y.Z dist/app-release-signed.apk \
  --title "LanText X.Y.Z" \
  --notes-file -   # paste ## [X.Y.Z] from CHANGELOG
```

Push the tag only when the signed asset is ready to attach.

## F-Droid (later)

First inclusion is exclusive upstream-signed RB. Copy
[docs/fdroiddata.yml](docs/fdroiddata.yml) to an fdroiddata MR as
`metadata/app.lantext.yml`.

- `Builds.commit` must be the **full SHA** of the tag (`git rev-parse vX.Y.Z^{}`), never the tag name
- If this tag is newer than the draft, update `versionName`, `versionCode`, `commit`, `CurrentVersion`, and `CurrentVersionCode`
- `UpdateCheckMode: Tags` picks up later `vX.Y.Z` tags
- F-Droid rebuilds from the SHA and compares to GitHub `app-release-signed.apk`
- On match, they publish **your** signature
- On mismatch, that version is skipped (no F-Droid-signed fallback)

See [F-DROID.md](F-DROID.md). Do not open the MR until testing on a signed
APK is done. Do not bump versions mid-review unless a maintainer asks.

## Checklist

- [ ] Version name/code bumped
- [ ] CHANGELOG `[Unreleased]` → dated version
- [ ] Fastlane `changelogs/<versionCode>.txt`
- [ ] `verify-deterministic-build.sh` passes
- [ ] `sign-release.sh` → `dist/app-release-signed.apk`
- [ ] `verify-release-signing.sh` passes
- [ ] Device smoke test (pair, SMS, MMS, leave Wi-Fi, widget)
- [ ] Tag `vX.Y.Z` pushed
- [ ] GitHub Release with asset **`app-release-signed.apk`**
- [ ] (First listing only) fdroiddata MR: copy `docs/fdroiddata.yml` → `metadata/app.lantext.yml`, full SHA, Fastlane-only store copy
