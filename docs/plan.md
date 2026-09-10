# Plan — remaining work

Procedure details live in the linked docs, including the [native patching
workflow](native-patching.md).

## Device QA

- [ ] Verify the post-splash transition into login/onboarding and monitor at
      least 30 seconds for delayed termination.
- [ ] Verify promotional-notification suppression while preserving message,
      call, friend-request, and birthday notifications using the [QA
      checklist](qa-checklist.md).
- [ ] Complete the backup/reinstall/login/restore cycle; verify media/message
      associations and record provenance.
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.

## Pending product decisions

- [ ] Choose one bounded UI cleanup target and independently verify its symbols,
      semantics, callers, consumers, mutation, and risks.
- [ ] Decide whether opt-in analytics DAO suppression is wanted while preserving
      unrelated database operations and genuine notifications.

## Data-transfer research

- [ ] Inspect native backup, restore, and phone-transfer entry points and verify
      candidates in smali.
- [ ] Trace databases, attachment references, snapshots, encryption/key
      lifecycle, and reinstall/device/account constraints.
- [ ] Demonstrate a disposable-data round trip with attachment associations,
      interruption recovery, and wrong-account/version rejection before claiming
      restore support; distinguish media-only from full-chat results.

## Release

- [ ] After required QA passes, ship the stable release following the [release
      process](release.md).
