#!/bin/bash
# repatch.sh — re-patch an APK/APKM with this repo's patch set and sign it.
# Usage: scripts/repatch.sh <app.apk|apkm|xapk|apks> [output.apk]
# Requires: java, morphe-cli jar, signing keystore.
# Template adapted from chiggi_morphe_patches/scripts/repatch_sonyliv.sh.
#
# Overridable via environment variables:
#   APP_NAME       -> launcher name (enables "Change app name" if present)
#   PACKAGE_NAME   -> new package id (enables "Change package name" if present)
#   MPP            -> patch bundle (default: newest local build, else latest release)
#   KEYSTORE       -> signing keystore (default: ./Morphe.keystore)
#   MORPHE_CLI     -> morphe-cli jar (default: ~/.local/bin/morphe-cli.jar)
#   GITHUB_REPO    -> owner/repo used when downloading the latest release bundle
set -euo pipefail

INPUT="${1:?Usage: scripts/repatch.sh <apk-file> [output.apk]}"
OUT="${2:-${INPUT%.*}_patched.apk}"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLI="${MORPHE_CLI:-$HOME/.local/bin/morphe-cli.jar}"
KEYSTORE="${KEYSTORE:-$PROJECT_DIR/Morphe.keystore}"
GITHUB_REPO="${GITHUB_REPO:-}"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

die() { echo "❌ $*" >&2; exit 1; }

command -v java >/dev/null 2>&1 || die "java not found"
[[ -f "$INPUT" ]] || die "input not found: $INPUT"
[[ -f "$CLI" ]] || die "morphe-cli jar not found at: $CLI (set MORPHE_CLI)"
[[ -f "$KEYSTORE" ]] || die "keystore not found at: $KEYSTORE (set KEYSTORE)"

# ---------- Locate the .mpp patch bundle ----------
# Prefer a locally built bundle; otherwise download the latest GitHub release asset.
if [[ -z "${MPP:-}" ]]; then
  MPP="$(for f in "$PROJECT_DIR"/patches/build/libs/patches-*.mpp; do
    [[ -e "$f" ]] || continue
    case "$f" in *sources*|*javadoc*) continue;; *) printf '%s\n' "$f";; esac
  done | head -1 || true)"
fi
if [[ -z "$MPP" ]]; then
  [[ -n "$GITHUB_REPO" ]] || die "no local .mpp found; set MPP= or GITHUB_REPO=owner/repo"
  echo "No local .mpp found. Downloading the latest release bundle..."
  MPP="$TMP/patches.mpp"
  url="$(curl -fsSL "https://api.github.com/repos/$GITHUB_REPO/releases/latest" \
    | python3 -c "import sys,json;print(next(a['browser_download_url'] for a in json.load(sys.stdin)['assets'] if a['name'].endswith('.mpp')))")"
  curl -fsSL -o "$MPP" "$url"
fi
echo "Using patch bundle: $MPP"

# ---------- Build the options file (rename patches only if env vars set) ----------
OPTS="$TMP/options.json"
java -jar "$CLI" options-create -p "$MPP" -o "$OPTS" -t "$TMP/oc" >/dev/null 2>&1
APP_NAME="${APP_NAME:-}" PACKAGE_NAME="${PACKAGE_NAME:-}" python3 - "$OPTS" <<'PY'
import json, os, sys
path = sys.argv[1]
data = json.load(open(path))
patches = data[0]["patches"]
app_name, pkg_name = os.environ["APP_NAME"], os.environ["PACKAGE_NAME"]
if app_name and "Change app name" in patches:
    patches["Change app name"]["enabled"] = True
    patches["Change app name"].setdefault("options", {})["appName"] = app_name
if pkg_name and "Change package name" in patches:
    patches["Change package name"]["enabled"] = True
    patches["Change package name"].setdefault("options", {})["packageName"] = pkg_name
json.dump(data, open(path, "w"), indent=1)
PY

# ---------- Patch + sign ----------
echo "Patching '$INPUT' -> '$OUT'"
java -jar "$CLI" patch -p "$MPP" --options-file "$OPTS" --keystore "$KEYSTORE" \
  -o "$OUT" --purge -t "$TMP/patch" "$INPUT"

echo
echo "✅ Patched APK: $OUT"
echo "Install:  adb install -r \"$OUT\""
