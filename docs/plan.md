# Plan — remaining work

Procedure details live in the linked docs, including the [native patching
workflow](native-patching.md).

## Authentication and device QA

- [ ] Capture a fresh failed-login trace on SM-S936B with logcat started before
      pressing Login; identify the auth endpoint and server/client response for
      error 101. The current trace has no app-side 101 or HTTP payload.
- [ ] Decide whether the re-signed build can support authentication after
      confirming the Java signature payload path; do not claim login support from
      startup success alone.
- [ ] With a successful logged-in session, verify feed/story/community ad
      suppression and preserve message, call, friend-request, and birthday
      notifications using the [QA checklist](qa-checklist.md). Requires a
      disposable/test account and controlled fixtures.
- [ ] Monitor the logged-in session for crashes, crypto/DAO exceptions, and
      integrity warnings.
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.

## Release

- [ ] Stage and ship a versioned release only after authentication and required
      device QA pass, following the [release process](release.md).
