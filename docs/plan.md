# Plan — Threads patch fix (issue #5)

Working home: this repo, namespace `com.zeldrisho.threads`. The feed ad-removal
rework is implemented and device-verified. **Remaining work only:**

## Ship the fix
- [ ] Decide release path: this repo (`zeldrisho/morphe-patches`) or the issue's
      repo (`durgesh0505/chiggi_morphe_patches`, still has the old broken patch).
      If chiggi: port `HideAdsPatch.kt` + `FeedAdFilter.java` + extension wiring there.
- [ ] Confirm the extension `.mpe` is reproducible at release time
      (build copies it from `extensions/extension/build/morphe/extensions/extension.mpe`
      to repo-relative `extensions/extension.mpe`; `extendWith("extensions/extension.mpe")`
      resolves relative to the patch working dir) — verify in CI, not just local.
- [ ] Commit with `fix:`/`feat:` message per repo conventions; cut release via the
      repo's semantic-release flow (never by hand). Regenerate `patches-list.json`.
- [ ] Full-combo device pass (all default-on patches together): Hide ads + Remove
      AD_ID + Change app name + Change package name → confirm login & feed still OK.

## Version pinning
- [ ] `Constants.kt` targets `com.instagram.barcelona` `434.0.0.41.74`
      (tested on APKMirror build versionCode **510406926**; the original pin noted
      510406907 from APKPure — same version name, different code).
- [ ] Fingerprint surface is pinned: `BarcelonaFeedCache.A0F` (classes.dex) +
      `Media.DED()`. Re-verify smali per future app update; worst case ads return
      (filter no-ops), feed never breaks.

## Housekeeping
- [ ] `analysis/` (large decompile tree) → `.gitignore` or relocate outside repo.
- [ ] Remove/keep `extensions/extension.mpe` build-copy decision (currently untracked).
- [ ] Optional follow-up: extend Hide ads to clips/reels sponsored surfaces
      (currently feed-scoped only; issue #5 is feed-only).
