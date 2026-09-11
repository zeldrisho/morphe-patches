# Plan — remaining work

Procedure details live in the linked docs, including the [native patching
workflow](native-patching.md).

## Device QA and restore

- [ ] Investigate the Google Drive restore authorization failure on SM-S936B;
      capture bounded client/GMS logs and confirm whether the failure is token
      issuance, account selection, or Drive API access. Do not record
      credentials, OAuth tokens, or private key material.
- [ ] Verify the authenticated session for crashes, crypto/DAO exceptions,
      integrity warnings, and the required feed/story/community ad behavior;
      preserve message, call, friend-request, and birthday notifications using
      the [QA checklist](qa-checklist.md).
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.

## Release

- [ ] Stage and ship a versioned release only after authentication and required
      device QA pass, following the [release process](release.md).
