# Plan — remaining work

Procedure details live in the linked docs, including the [native patching
workflow](native-patching.md).

## Device QA and release

- [ ] Complete the blocked Zalo Drive restore E2E on SM-S936B: confirm the
      existing microG account is selected without launching `addAccount`, then
      verify Drive token acquisition and photo restore. Capture bounded,
      sanitized logs only; never record credentials or OAuth tokens.
- [ ] Verify the authenticated session for crashes, crypto/DAO exceptions,
      integrity warnings, required feed/story/community ad behavior, and
      preservation of message, call, friend-request, and birthday notifications
      using the [QA checklist](qa-checklist.md).
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.
- [ ] Stage and ship a versioned release only after authentication, restore,
      and required device QA pass, following the [release process](release.md).
