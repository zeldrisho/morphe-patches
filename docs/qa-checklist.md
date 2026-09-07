# QA checklist (per release / APK bump)

Manual E2E — run on a **throwaway account** (re-signed build + VPN/proxy on a
real account = ban risk, see `docs/lessons-learned.md`). Needs `adb` + the
pinned APK (`434.0.0.41.74` / versionCode `510406926` unless re-fingerprinting).

## 1. Build

```bash
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew :patches:test :extensions:extension:testDebugUnitTest
./gradlew buildAndroid          # .mpp -> patches/build/libs/patches-*.mpp
shellcheck scripts/*.sh
```

## 2. Re-patch + install

```bash
scripts/repatch.sh <threads.apkm> /tmp/threads_patched.apk
adb install -r /tmp/threads_patched.apk
```

- [ ] `aapt dump badging` shows no `AD_ID` permission
- [ ] Renamed package (`PACKAGE_NAME=...`) installs **alongside** stock, no
      `INSTALL_FAILED_DUPLICATE_PERMISSION`
- [ ] Launcher shows `APP_NAME` override

## 3. Feed ad removal (issue #5 regression)

Relabel-vs-remove signature: "labels disappeared but content still there" =
filter bypassed, predicate neutralized instead. Verify **removal**:

```bash
# scroll the main feed, then dump the visible screen as text
adb shell uiautomator dump /sdcard/ui.xml
adb pull /sdcard/ui.xml && grep -ci -E 'Ad|Sponsored' ui.xml
```

- [ ] Sponsored units absent from main feed (gap-free, no blank cards)
- [ ] Clips/reels untouched (feed-scoped by design)
- [ ] `adb logcat -s FeedAdFilter` shows no crash on scroll; worst case on a
      new app version is ads returning, never a broken feed

## 4. Version bump (new Threads release)

- [ ] Fingerprint `FeedMergeMethod` (A0F, `BarcelonaFeedCache`, 8 params + `this`)
      still resolves to exactly 1 method — 0 or >1 means R8 drift, re-hunt per
      `docs/reverse-engineering.md`
- [ ] Re-run steps 1–3 on the new version before updating `Constants.kt`
