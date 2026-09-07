# QA checklist (per release / APK bump)

Canonical repeatable device procedure. Other docs link here; they do not restate it.
Manual E2E — run on a **throwaway account** (re-signed build + VPN/proxy on a
real account = ban risk, see [lessons learned](lessons-learned.md#what-is-and-isnt-patchable)).
Needs `adb` plus the APK pinned in `shared/Constants.kt` (currently Threads
`434.0.0.41.74` / `TESTED_VERSION_CODE 510406926` unless re-fingerprinting —
`Constants.kt` is the source of truth, not this checklist).

## Build

```bash
./gradlew :patches:test :extensions:extension:testDebugUnitTest buildAndroid --no-daemon
shellcheck scripts/*.sh
python3 -m unittest discover -s scripts/tests -v  # offline helper regression tests
```

The `.mpp` lands in `patches/build/libs/patches-*.mpp`.

## Re-patch + install

```bash
MPP="patches/build/libs/patches-<version>.mpp" \
  bash scripts/repatch.sh /path/to/threads.apkm /tmp/threads_patched.apk
# Only update an existing install if its signing certificate matches.
adb install -r /tmp/threads_patched.apk
```

Record the input APK version/code and hash, bundle path/hash, enabled patches,
package ID, device/Android version, and signing certificate fingerprint (never
passwords). Multiple local bundles can exist; do not assume the helper selected
the newest one — pass `MPP=` explicitly. Offline helper tests verify
orchestration, not real APK signing or device behavior. Keep screenshots, UI
dumps, and logs outside Git; temporary files are not durable evidence. Retain
sanitized notes in the release/PR record.

- [ ] `aapt dump badging` shows no `AD_ID` permission
- [ ] Fresh login works with the default package and default-on patches.
      An existing session is a separate smoke test, not fresh-login evidence.
      Preserve it: ask before logout, clearing data, or uninstalling. Use a
      clean test device/profile where possible; the user enters credentials.
- [ ] Renamed package (`PACKAGE_NAME=...`) installs **alongside stock-signed**
      Threads, no `INSTALL_FAILED_DUPLICATE_PERMISSION`. Verify the stock
      copy's signing certificate against the original APK; a patched
      default-package copy does not satisfy this check. Do not replace an
      existing differently signed copy without approval.
- [ ] Both stock and renamed copies launch; test renamed-package login
      separately because OAuth/App Links can depend on package and signing.
- [ ] Launcher shows `APP_NAME` override

Session and coexistence rules: existing-session behavior, fresh login, and
renamed-package login are separate checks. Coexistence with a patched app does
not establish coexistence with stock-signed upstream.

## Feed ad removal (issue #5 regression)

Relabel-vs-remove signature: "labels disappeared but content still there" =
filter bypassed, predicate neutralized instead. Verify **removal**:

```bash
QA_DIR="$(mktemp -d /tmp/morphe-qa.XXXXXX)"
adb logcat -b crash -d > "$QA_DIR/crash-before.txt"
# Scroll the main feed and visually inspect changing content, then capture it.
adb shell uiautomator dump /sdcard/morphe-qa-ui.xml
adb pull /sdcard/morphe-qa-ui.xml "$QA_DIR/ui.xml"
adb shell rm /sdcard/morphe-qa-ui.xml
adb exec-out screencap -p > "$QA_DIR/feed.png"
adb logcat -b crash -d > "$QA_DIR/crash-after.txt"
# Label matches are only supplemental evidence; absence does not prove removal.
grep -oiE '\b(Ad|Sponsored)\b' "$QA_DIR/ui.xml" || true
```

- [ ] Sponsored units absent from main feed (gap-free, no blank cards),
      confirmed visually across refreshed/scrolled content, not just labels
- [ ] Video posts still open and play normally. Threads has no separate
      clips/reels surface; this is a playback regression check, not video-ad QA.
- [ ] Compare before/after crash buffers for new app crashes. The production
      `FeedAdFilter` emits no logs; a silent tag does not prove execution or
      removal. Use controlled comparison or verified temporary instrumentation
      for removal evidence. A clean smoke test is not comprehensive coverage.
- [ ] Test surfaces the app actually exposes; do not import another app's feature
      terminology or invent ad surfaces. Confirm video playback with changing
      frames/progress, separately from ad-removal evidence.

Record each result as PASS / FAIL / BLOCKED with its evidence.
Do not merge the stable release while required checks remain blocked.

## Version bump (new Threads release)

- [ ] Fingerprint `FeedMergeMethod` still resolves to exactly 1 method — 0 or >1
      means R8 drift; re-hunt per the [reverse engineering workflow](reverse-engineering.md#hunt-targets)
- [ ] Re-run [Build](#build) through [Feed ad removal](#feed-ad-removal-issue-5-regression)
      on the new version before updating `Constants.kt`
