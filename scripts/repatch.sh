#!/bin/bash
# repatch.sh — re-patch an APK/APKM with this repo's patch set and sign it.
# Usage: scripts/repatch.sh <app.apk|apkm|xapk|apks> [output.apk]
# Requires: java, python3, Morphe (morphe-desktop-*-all.jar), signing keystore (curl for downloads).
# No environment setup needed: the script discovers the Morphe JAR and a
# signing keystore from their standard locations (see below).
# Template adapted from chiggi_morphe_patches/scripts/repatch_sonyliv.sh.
#
# Overridable via environment variables:
#   APP_NAME       -> launcher name (enables "Change app name" if present)
#   PACKAGE_NAME   -> new package id (enables "Change package name" if present)
#   MPP            -> patch bundle (default: most recently modified local build, else latest release)
#   KEYSTORE       -> signing keystore (default: imported/shared keystore in
#                    the standard data dirs, else ./Morphe.keystore)
#   KEYSTORE_ALIAS   -> key entry alias (default: Morphe; keytool-created stores
#                       often lowercase it to `morphe` — override in that case)
#   KEYSTORE_PASSWORD       -> keystore password (default: Morphe; set to empty
#                       for the legacy repo Morphe.keystore: KEYSTORE_PASSWORD="")
#   KEYSTORE_ENTRY_PASSWORD -> key entry password (default: CLI default; flag omitted)
#   MORPHE_CLI     -> optional Morphe JAR override (default: automatic discovery:
#                    newest morphe-desktop-*-all.jar in ~/.local/share/morphe-desktop/
#                    or ~/.local/share/morphe/, then `morphe` on PATH,
#                    then ~/.local/bin/morphe.jar)
#   VERIFY_SDK     -> opt-in DEX/APK verification: 1 uses SDK discovery
#                    ($ANDROID_HOME -> $ANDROID_SDK_ROOT -> OS default),
#                    any other value is passed as --verify-with-sdk=<path>.
#                    Empty/0/false disables verification (default).
#   GITHUB_REPO    -> owner/repo used when downloading the latest release bundle
set -euo pipefail

INPUT="${1:?Usage: scripts/repatch.sh <apk-file> [output.apk]}"
OUT="${2:-${INPUT%.*}_patched.apk}"
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# ---------- Locate the Morphe JAR (no env vars required) ----------
# Priority: $MORPHE_CLI override, newest upstream artifact in the standard
# share dirs, `morphe` on PATH, then ~/.local/bin/morphe.jar.
JAR="${MORPHE_CLI:-}"
MORPHE_BIN=""
if [[ -z "$JAR" ]]; then
    for dir in "$HOME/.local/share/morphe-desktop" "$HOME/.local/share/morphe"; do
        for f in "$dir"/morphe-desktop-*-all.jar; do
            [[ -f "$f" ]] || continue
            if [[ -z "$JAR" || "$f" -nt "$JAR" ]]; then
                JAR="$f"
            fi
        done
    done
fi
if [[ -z "$JAR" ]]; then
    if cmd="$(command -v morphe 2>/dev/null)"; then
        MORPHE_BIN="$cmd"
    elif [[ -f "$HOME/.local/bin/morphe.jar" ]]; then
        JAR="$HOME/.local/bin/morphe.jar"
    fi
fi

# Run Morphe: wrapper executable when discovered on PATH, else `java -jar`.
morphe() {
    if [[ -n "$MORPHE_BIN" ]]; then
        "$MORPHE_BIN" "$@"
    else
        java -jar "$JAR" "$@"
    fi
}

# ---------- Locate the signing keystore (no env vars required) ----------
# Priority: $KEYSTORE override, imported/shared keystores in the standard
# data dirs, then the repo's ./Morphe.keystore (empty store password).
if [[ -z "${KEYSTORE:-}" ]]; then
    for k in \
        "$HOME/.local/share/morphe-desktop/morphe-data/imported.keystore" \
        "$HOME/.local/share/morphe-desktop/morphe-data/morphe.keystore" \
        "$HOME/.local/share/morphe/morphe-data/imported.keystore" \
        "$HOME/.local/share/morphe/morphe-data/morphe.keystore" \
        "$HOME/morphe/morphe-data/imported.keystore" \
        "$HOME/morphe/morphe-data/morphe.keystore" \
        "$HOME/morphe/imported.keystore" \
        "$HOME/morphe/morphe.keystore"; do
        if [[ -f "$k" ]]; then
            KEYSTORE="$k"
            break
        fi
    done
fi
if [[ -z "${KEYSTORE:-}" && -f "$PROJECT_DIR/Morphe.keystore" ]]; then
    KEYSTORE="$PROJECT_DIR/Morphe.keystore"
fi
GITHUB_REPO="${GITHUB_REPO:-zeldrisho/morphe-patches}"

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

# Print an error message to stderr and exit with code 1.
die() {
    echo "❌ $*" >&2
    exit 1
}

if [[ -z "$MORPHE_BIN" ]]; then
    command -v java >/dev/null 2>&1 || die "java not found"
fi
command -v python3 >/dev/null 2>&1 || die "python3 not found"
[[ -f "$INPUT" ]] || die "input not found: $INPUT"
if [[ -z "$MORPHE_BIN" && ! -f "${JAR:-}" ]]; then
    die "Morphe JAR not found. Download morphe-desktop-*-all.jar to ~/.local/share/morphe-desktop/ or place 'morphe' in PATH."
fi
[[ -f "${KEYSTORE:-}" ]] || die "keystore not found (set KEYSTORE= or import one into morphe-data/)"

# ---------- Locate the .mpp patch bundle ----------
# Prefer a locally built bundle; otherwise download the latest GitHub release asset.
if [[ -z "${MPP:-}" ]]; then
    MPP=""
    for f in "$PROJECT_DIR"/patches/build/libs/patches-*.mpp; do
        [[ -f "$f" ]] || continue
        case "${f##*/}" in *sources* | *javadoc*) continue ;; esac
        if [[ -z "$MPP" || "$f" -nt "$MPP" ]]; then
            MPP="$f"
        fi
    done
fi
if [[ -z "$MPP" ]]; then
    [[ -n "$GITHUB_REPO" ]] || die "no local .mpp found; set MPP= or GITHUB_REPO=owner/repo"
    echo "No local .mpp found. Downloading the latest release bundle..."
    MPP="$TMP/patches.mpp"
    url="$(curl -fsSL "https://api.github.com/repos/$GITHUB_REPO/releases/latest" |
        python3 -c "import sys,json;print(next(a['browser_download_url'] for a in json.load(sys.stdin)['assets'] if a['name'].endswith('.mpp')))")"
    curl -fsSL -o "$MPP" "$url"
fi
[[ -f "$MPP" ]] || die "patch bundle not found: $MPP"
echo "Using patch bundle: $MPP"

# ---------- Build the options file (rename patches only if env vars set) ----------
OPTS="$TMP/options.json"
morphe options-create -p "$MPP" -o "$OPTS" >/dev/null
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
# NB: CLI options need the `=` form, not space-separated (see lessons-learned).
KEYSTORE_ARGS=(--keystore="$KEYSTORE" --keystore-entry-alias="${KEYSTORE_ALIAS:-Morphe}")
KEYSTORE_PASSWORD="${KEYSTORE_PASSWORD-Morphe}"
[[ -n "$KEYSTORE_PASSWORD" ]] && KEYSTORE_ARGS+=(--keystore-password="$KEYSTORE_PASSWORD")
[[ -n "${KEYSTORE_ENTRY_PASSWORD:-}" ]] && KEYSTORE_ARGS+=(--keystore-entry-password="$KEYSTORE_ENTRY_PASSWORD")
VERIFY_ARGS=()
case "${VERIFY_SDK:-}" in
    "" | 0 | false | no) ;;
    1 | true | yes) VERIFY_ARGS+=(--verify-with-sdk) ;;
    *) VERIFY_ARGS+=(--verify-with-sdk="$VERIFY_SDK") ;;
esac
echo "Patching '$INPUT' -> '$OUT'"
morphe patch -p "$MPP" --options-file "$OPTS" "${KEYSTORE_ARGS[@]}" "${VERIFY_ARGS[@]}" \
    -o "$OUT" -t "$TMP/patch" "$INPUT"

echo
echo "✅ Patched APK: $OUT"
echo "Install:  adb install -r \"$OUT\""
