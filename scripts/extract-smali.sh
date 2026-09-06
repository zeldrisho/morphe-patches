#!/bin/bash
# extract-smali.sh — disassemble all DEX files to smali (split-APK aware).
# Usage: scripts/extract-smali.sh <apk-file> <output-dir>
# Requires: unzip, baksmali, rg.
set -e

APK="${1:?Usage: scripts/extract-smali.sh <apk-file> <output-dir>}"
OUT="${2:?Usage: scripts/extract-smali.sh <apk-file> <output-dir>}"

if [ ! -f "$APK" ]; then
  echo "❌ Not found: $APK" >&2
  exit 1
fi

mkdir -p "$OUT"
TMPDIR="$(mktemp -d)"
trap 'rm -rf "$TMPDIR"' EXIT

EXT="${APK##*.}"
DEX_SOURCE="$APK"
if [[ "$EXT" == "apkm" || "$EXT" == "xapk" || "$EXT" == "apks" ]]; then
  unzip -o -q "$APK" "base.apk" -d "$TMPDIR" || {
    echo "❌ Could not extract base.apk from $APK" >&2
    exit 1
  }
  DEX_SOURCE="$TMPDIR/base.apk"
fi

COUNT=0
for dex in $(unzip -l "$DEX_SOURCE" | rg '\.dex' | awk '{print $4}'); do
  name="$(basename "$dex" .dex)"
  unzip -o -q "$DEX_SOURCE" "$dex" -d "$TMPDIR"
  baksmali d "$TMPDIR/$dex" -o "$OUT/$name"
  COUNT=$((COUNT + 1))
done

if [ "$COUNT" = "0" ]; then
  echo "❌ No DEX files found in $APK" >&2
  exit 1
fi

echo "✅ Disassembled $COUNT DEX file(s) to $OUT/"
ls "$OUT/"
