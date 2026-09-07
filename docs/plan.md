# Plan — remaining work

## Finish release QA
- [ ] Verify fresh login on the default-package, default-on patch combo;
      preserve the existing session unless logout is explicitly approved.
- [ ] Visually verify sponsored units are removed, with no blank cards or gaps,
      and clips/reels remain unaffected. Label absence alone is insufficient.
- [ ] Test the opt-in renamed package alongside **stock-signed** Threads;
      coexistence with the patched default-package copy is already tested.
      Follow `docs/qa-checklist.md` §§2–3.

## Ship pending changes
- [x] Manually review and commit `scripts/repatch.sh` CLI/signing fixes,
      offline regression tests, and documentation changes on `dev`
      (CodeRabbit skipped as requested).
- [ ] After QA, merge (no squash) `dev` → `main` for the automated stable
      release (`docs/release.md`).

## Verification status
- PASS: Gradle patch/extension unit tests and `buildAndroid`; six offline
  helper regression tests; ShellCheck, Bash syntax, and diff whitespace checks.
- PENDING: real APK re-patch/signing and the device checks above. Offline
  helper tests validate argument handling, not actual signing or app behavior.
- BLOCKED: stable merge until required device QA passes. In this pass, no
  sessions were cleared or apps replaced; device access was limited to listing
  connected devices.

## Follow-ups
- [ ] Optional: extend Hide ads to clips/reels sponsored surfaces only after
      APK recon, fingerprint verification, and device testing.
- [ ] Per app update: re-verify fingerprints and device behavior before
      updating the compatibility pin (`docs/qa-checklist.md` §4).
