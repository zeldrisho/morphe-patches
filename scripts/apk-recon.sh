#!/bin/bash
# apk-recon.sh — identify an APK and emit a recon report.
# Usage: scripts/apk-recon.sh <file.apk|apkm|xapk> [output.md]
# Requires: aapt, unzip, rg (optional), apkid via uvx (optional).
set -e

APK="${1:?Usage: scripts/apk-recon.sh <apk-file> [output.md]}"
OUT="${2:-recon.md}"

if [ ! -f "$APK" ]; then
  echo "❌ Not found: $APK" >&2
  exit 1
fi

WORK_WAS_TEMP=0
TARGET="$APK"
EXT="${APK##*.}"
TMPDIR=""
if [[ "$EXT" == "apkm" || "$EXT" == "xapk" || "$EXT" == "apks" ]]; then
  TMPDIR="$(mktemp -d)"
  trap 'rm -rf "$TMPDIR"' EXIT
  unzip -o -q "$APK" "base.apk" -d "$TMPDIR" || {
    echo "❌ Could not extract base.apk from $APK" >&2
    exit 1
  }
  TARGET="$TMPDIR/base.apk"
  WORK_WAS_TEMP=1
fi

BADGING="$(aapt dump badging "$TARGET" 2>/dev/null | head -20)"
PKG="$(echo "$BADGING" | grep -oP "package: name='\K[^']+" | head -1)"
VER="$(echo "$BADGING" | grep -oP "versionName='\K[^']+" | head -1)"
VERCODE="$(echo "$BADGING" | grep -oP "versionCode='\K[^']+" | head -1)"
MINSDK="$(echo "$BADGING" | grep -oP "sdkVersion:'\K[^']+" | head -1)"
TARGETSDK="$(echo "$BADGING" | grep -oP "targetSdkVersion:'\K[^']+" | head -1)"
LABEL="$(echo "$BADGING" | grep -oP "application-label:'\K[^']+" | head -1)"
LAUNCH="$(echo "$BADGING" | grep -oP "launchable-activity: name='\K[^']+" | head -1)"

SPLIT="APK"
if [[ "$EXT" == "apkm" ]]; then SPLIT="APKM"
elif [[ "$EXT" == "xapk" ]]; then SPLIT="XAPK"
elif [[ "$EXT" == "apks" ]]; then SPLIT="APKS"; fi
if aapt dump xmltree "$TARGET" AndroidManifest.xml 2>/dev/null | rg -qi 'split|requiredSplit'; then
  SPLIT="$SPLIT (split manifest detected)"
fi

DEXES="$(unzip -l "$APK" | rg '\.dex' | awk '{print $4}' | tr '\n' ' ')"
DEXCOUNT="$(unzip -l "$APK" | rg -c '\.dex')"
LIBS="$(unzip -l "$APK" | rg -o 'lib/[a-z0-9_-]+' | sort -u | tr '\n' ' ')"

FRAMEWORK="native"
if unzip -l "$APK" | rg -q 'index.android.bundle'; then
  FRAMEWORK="React Native"
elif unzip -l "$APK" | rg -q 'libflutter|libapp\.so'; then
  FRAMEWORK="Flutter"
fi

APKID="unknown (apkid not available)"
if command -v uvx >/dev/null 2>&1; then
  APKID="$(uvx apkid "$APK" 2>/dev/null || echo 'apkid failed')"
fi

SIZE="$(du -h "$APK" | cut -f1)"

cat > "$OUT" <<EOF
# Recon — ${LABEL:-unknown}

## Identity
- App Name: ${LABEL:-unknown}
- Package: ${PKG:-unknown}
- Version: ${VER:-unknown}
- VersionCode: ${VERCODE:-unknown}
- MinSdk: ${MINSDK:-unknown}
- TargetSdk: ${TARGETSDK:-unknown}

## APK Info
- File: $APK ($SIZE)
- APK Type: $SPLIT
- DEX count: $DEXCOUNT ($DEXES)

## Protections (apkid)
\`\`\`
$APKID
\`\`\`

## Architecture
- Framework: $FRAMEWORK
- Native libs: $LIBS
- Main activity: ${LAUNCH:-unknown}
EOF

echo "✅ Recon written to $OUT"
if [ "$WORK_WAS_TEMP" = "1" ]; then
  rm -rf "$TMPDIR"
  trap - EXIT
fi
