#!/usr/bin/env bash
# Render F-Droid / README store graphics from the HTML mocks in this directory.
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$DIR/../.." && pwd)"
OUT="$ROOT/fastlane/metadata/android/en-US/images"
CHROME="${CHROME:-google-chrome}"

shot() {
  local html="$1" width="$2" height="$3" dest="$4"
  mkdir -p "$(dirname "$dest")"
  "$CHROME" \
    --headless=new \
    --disable-gpu \
    --hide-scrollbars \
    --no-sandbox \
    --force-device-scale-factor=1 \
    --window-size="${width},${height}" \
    --default-background-color=00000000 \
    --screenshot="$dest" \
    "file://${html}"
  echo "wrote $dest"
}

shot "$DIR/icon.html"              512  512  "$OUT/icon.png"
shot "$DIR/feature.html"           1024 500  "$OUT/featureGraphic.png"
shot "$DIR/phone-app.html"         1080 1920 "$OUT/phoneScreenshots/1.png"
shot "$DIR/phone-permissions.html" 1080 1920 "$OUT/phoneScreenshots/2.png"
shot "$DIR/web-pair.html"          1080 1920 "$OUT/phoneScreenshots/3.png"
shot "$DIR/web-thread.html"        1080 1920 "$OUT/phoneScreenshots/4.png"
shot "$DIR/web-desktop.html"       1440 900  "$OUT/tenInchScreenshots/1.png"
