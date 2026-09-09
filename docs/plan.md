# Plan — remaining work

Only outstanding actions. Procedure details live in the linked docs.

## Finish release QA

- [x] Re-ran the device QA checklist for the supported 445 version on a
      single phone (SM_S936B, Android 16): fresh login, repeated
      refresh/pagination, video playback, crash comparison (no crash), no
      AD_ID permission. Sanitized evidence/provenance retained in the
      release record; SDK verification waived per lessons learned.
- [x] Renamed-package support remains documented as best-effort only: the 445
      renamed build crashed during first-feed loading, while the
      original-package build worked. Do not promise coexistence until it is
      independently verified with stock-signed Threads.

## Release

- [ ] After required QA passes, merge (no squash) into `main`, stage with
      `scripts/prepare-release.sh`, and push the version tag for the stable
      release ([release process](release.md)).
