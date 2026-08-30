#!/bin/sh
# Create a LanText upload/release keystore OUTSIDE the git repo.
#
# Env (optional):
#   LT_KEYSTORE_OUT   — default: $HOME/.local/share/lantext/lantext-release.jks
#   LT_RELEASE_KEY_ALIAS — default: lantext
#   LT_KEYSTORE_PASSWORD / LT_KEY_PASSWORD — if unset, generated and printed once
#   LT_DNAME — default: CN=LanText, OU=Mobile, O=crome1394, C=US
#
# After creation, back up the keystore + passwords. Losing them breaks update continuity.
set -eu

OUT=${LT_KEYSTORE_OUT:-"$HOME/.local/share/lantext/lantext-release.jks"}
ALIAS=${LT_RELEASE_KEY_ALIAS:-lantext}
DNAME=${LT_DNAME:-CN=LanText, OU=Mobile, O=crome1394, C=US}
VALIDITY_DAYS=${LT_VALIDITY_DAYS:-10000}

gen_password() {
  if command -v openssl >/dev/null 2>&1; then
    openssl rand -base64 24 | tr -d '/+=' | head -c 32
    printf '\n'
  else
    head -c 48 /dev/urandom | base64 | tr -d '/+=\n' | head -c 32
    printf '\n'
  fi
}

if [ -e "$OUT" ]; then
  printf 'Keystore already exists: %s\n' "$OUT" >&2
  printf 'Refusing to overwrite. Move/backup the file first if you intend to replace it.\n' >&2
  exit 1
fi

mkdir -p "$(dirname "$OUT")"

if [ -z "${LT_KEYSTORE_PASSWORD:-}" ]; then
  LT_KEYSTORE_PASSWORD=$(gen_password | tr -d '\n')
  GENERATED_STORE=1
else
  GENERATED_STORE=0
fi
LT_KEY_PASSWORD=${LT_KEY_PASSWORD:-$LT_KEYSTORE_PASSWORD}
if [ "$GENERATED_STORE" -eq 1 ]; then
  GENERATED_KEY=1
else
  GENERATED_KEY=0
fi

keytool -genkeypair -v \
  -storetype PKCS12 \
  -keystore "$OUT" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$LT_KEYSTORE_PASSWORD" \
  -keypass "$LT_KEY_PASSWORD" \
  -dname "$DNAME"

printf '\n=== Keystore created ===\n'
printf 'Path:  %s\n' "$OUT"
printf 'Alias: %s\n' "$ALIAS"
if [ "$GENERATED_STORE" -eq 1 ] || [ "$GENERATED_KEY" -eq 1 ]; then
  printf '\n*** SAVE THESE PASSWORDS NOW (shown once) ***\n'
  printf 'LT_KEYSTORE_PASSWORD=%s\n' "$LT_KEYSTORE_PASSWORD"
  printf 'LT_KEY_PASSWORD=%s\n' "$LT_KEY_PASSWORD"
  printf '*** Put them in a password manager / encrypted backup ***\n'
fi

printf '\nShell exports for signing (current session):\n'
printf 'export LT_RELEASE_KEYSTORE=%s\n' "$OUT"
printf 'export LT_RELEASE_KEY_ALIAS=%s\n' "$ALIAS"
printf 'export LT_KEYSTORE_PASSWORD='\''…'\''   # from above / your vault\n'
printf 'export LT_KEY_PASSWORD='\''…'\''\n'

printf '\nNext:\n'
printf '  1. Back up the keystore file and passwords offline.\n'
printf '  2. Build unsigned: ./gradlew --no-daemon --no-parallel clean :app:assembleRelease\n'
printf '  3. Sign: sh scripts/sign-release.sh\n'
printf '  4. Pin cert SHA-256 in scripts/expected-release-cert.sha256\n'
printf '     (keytool -exportcert -alias %s -keystore %s | sha256sum)\n' "$ALIAS" "$OUT"
