#!/bin/bash
# backup-zalo-data.sh — pull Zalo's external media folder before uninstalling.
# Usage: scripts/backup-zalo-data.sh [backup_parent_dir]
# Requires: adb on PATH, exactly one connected device (no root needed).
#
# Creates <parent>/backup_zalo_YYYYMMDD_HHMMSS/com.zing.zalo via
# `adb pull /sdcard/Android/data/com.zing.zalo`. Timestamped so repeated
# test cycles never overwrite prior backups. Covers the external media
# folder only — message text needs Zalo's own in-app backup (see reminder
# printed on success).
set -euo pipefail

PKG="com.zing.zalo"
SRC="/sdcard/Android/data/${PKG}"
PARENT="${1:-.}"

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

main() {
    require_one_device

    if ! adb shell test -d "$SRC" >/dev/null 2>&1; then
        echo "❌ Source path missing on device: ${SRC}" >&2
        echo "   Nothing to back up — is Zalo installed with external data?" >&2
        exit 1
    fi

    local stamp dir files size
    stamp="$(date +%Y%m%d_%H%M%S)"
    dir="${PARENT}/backup_zalo_${stamp}"
    mkdir -p "$dir"
    echo "⬇️  Pulling ${SRC} → ${dir}/"
    adb pull "$SRC" "${dir}/"

    files="$(find "$dir" -type f | wc -l | tr -d ' ')"
    size="$(du -sh "$dir" | cut -f1)"
    if [ "$files" -eq 0 ]; then
        echo "❌ Pull produced zero files — refusing to call this a backup." >&2
        exit 1
    fi
    echo "✅ Backup complete: ${dir} (${files} files, ${size})"
    echo
    echo "⚠️  MANUAL STEP STILL REQUIRED: this backup covers the external media"
    echo "   folder only — message text is NOT included. Before uninstalling, open"
    echo "   Zalo and run Settings > Sao lưu & đồng bộ tin nhắn (backup & sync"
    echo "   messages) so chats can be restored from Zalo's own backup."
}

main "$@"
