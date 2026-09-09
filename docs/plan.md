# Plan — remaining work

Only outstanding actions. Procedure details live in the linked docs.

## Finish release QA

- [ ] Re-run the complete device QA checklist for the supported 445 version,
      including repeated refresh/pagination, video playback, crash comparison,
      and sanitized evidence/provenance in the release record.
- [x] Renamed-package support remains documented as best-effort only: the 445
      renamed build crashed during first-feed loading, while the
      original-package build worked. Do not promise coexistence until it is
      independently verified with stock-signed Threads.

## Release

- [ ] After required QA passes, merge (no squash) into `main`, stage with
      `scripts/prepare-release.sh`, and push the version tag for the stable
      release ([release process](release.md)).
