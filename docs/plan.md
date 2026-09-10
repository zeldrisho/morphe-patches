# Plan — remaining work

Only outstanding actions. Procedure details live in the linked docs.

## Device QA (Zalo 26.08.01, all four patches)

- [ ] Install the patched APKM on the test phone and confirm promo pushes
      (Timeline/Stories, Zalo Video) are suppressed while message, call,
      friend-request, and birthday notifications still arrive —
      [QA checklist](qa-checklist.md). Record input/bundle/options provenance
      with the result.
- [ ] Exercise the non-destructive reinstall cycle on-device
      (`scripts/backup-zalo-data.sh` → uninstall stock → install patched →
      log in → `scripts/restore-zalo-data.sh`); media present afterwards,
      no first-run wipe. Scripts + offline tests are in place; the on-device
      half is still unverified (no device attached).
- [ ] Record the APKMirror download page URL for the Zalo 26.08.01 input
      (hash and variant already on file; URL still missing).

## Awaiting go-ahead (do not implement unasked)

- [ ] UI cleanup scope decision + obfuscated symbol re-hunt for 260801903
      (bottom tabs, inbox boxes, zcloud banner, Me rows).
- [ ] Analytics DAO suppression (opt-in, off by default) and whether
      ACTIVITY_UPDATES / USER_INTERACTIONS channels stay out of the
      promo-notifications patch.

## Release

- [ ] After required QA passes, ship the stable release following the
      [release process](release.md).
