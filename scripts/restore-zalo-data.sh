#!/bin/bash
# restore-zalo-data.sh — push a Zalo external-data backup back after reinstall.
# Usage: scripts/restore-zalo-data.sh [backup_dir]
# Requires: adb on PATH, exactly one connected device (no root needed).
#
# PRECONDITION: install the patched APK before restoring. This helper supports
# restoring before first login when that is explicitly desired; Zalo may still
# wipe or reject the folder during first-run migration. With no argument, uses
# the most recent backup_zalo_* directory in the current working directory.
# Compresses the backup locally and extracts it as one ADB stream, avoiding
# per-file transfer overhead, then verifies the result on-device.
set -euo pipefail

PKG="com.zing.zalo"
DST="/sdcard/Android/data"

usage() {
    echo "Usage: scripts/restore-zalo-data.sh [backup_dir]" >&2
    echo >&2
    echo "Precondition: install the patched APK and LOG IN before restoring;" >&2
    echo "a pre-login restore may be wiped or rejected by Zalo on first run." >&2
}

require_one_device() {
    if ! command -v adb >/dev/null 2>&1; then
        echo "❌ adb not found on PATH (install Android platform-tools)." >&2
        exit 1
    fi
    local count
    count="$(adb devices | awk 'NR>1 && $2 == "device"' | wc -l | tr -d ' ')"
    if [ "$count" -ne 1 ]; then
        echo "❌ Expected exactly one connected device, found ${count}:" >&2
        adb devices >&2
        exit 1
    fi
}

# Echo the backup dir: explicit arg, else newest backup_zalo_* in cwd.
resolve_backup() {
    if [ $# -ge 1 ]; then
        echo "$1"
        return
    fi
    local latest="" d
    for d in backup_zalo_*; do
        [ -d "$d" ] || continue
        if [ -z "$latest" ] || [ "$d" \> "$latest" ]; then
            latest="$d"
        fi
    done
    if [ -z "$latest" ]; then
        echo "❌ No backup_zalo_* directory in ${PWD} — pass one explicitly." >&2
        usage
        exit 1
    fi
    echo "$latest"
}

main() {
    local backup src_dir local_files remote target
    backup="$(resolve_backup "$@")"
    if [ ! -d "$backup" ]; then
        echo "❌ Backup directory not found: ${backup}" >&2
        usage
        exit 1
    fi
    src_dir="${backup}/${PKG}"
    if [ ! -d "$src_dir" ]; then
        echo "❌ Expected ${src_dir} inside the backup (from backup-zalo-data.sh)." >&2
        exit 1
    fi

    require_one_device

    echo "⬆️  Compressing and streaming ${src_dir} → ${DST}/"
    # tar/gzip are available in Android toybox. Keep the archive in a pipe so
    # no second multi-gigabyte temporary copy is created on either side.
    tar -czf - -C "$backup" "$PKG" |
        adb shell "cd '$DST' && tar -xzf -"

    target="${DST}/${PKG}"
    if ! adb shell test -d "$target" >/dev/null 2>&1; then
        echo "❌ Push finished but ${target} is missing on device." >&2
        exit 1
    fi
    local_files="$(find "$src_dir" -type f | wc -l | tr -d ' ')"
    remote="$(adb shell ls -R "$target" | wc -l | tr -d ' ')"
    if [ "$local_files" -gt 0 ] && [ "$remote" -eq 0 ]; then
        echo "❌ Target exists but lists zero entries (local files: ${local_files})." >&2
        exit 1
    fi
    echo "✅ Restore complete: ${target} (local files: ${local_files}, on-device listing lines: ${remote})"
}

main "$@"
