# Plan — remaining work

Only outstanding actions. Procedure details live in the linked docs.

## Zalo 26.08.01 startup regression

- [ ] Re-hunt the complete native startup integrity path in
      `lib/arm64-v8a/libnative_utils.so`. A narrowly patched
      `apk_tampered → System.exit(0)` branch did not launch, so additional
      validation/configuration failure paths remain.
- [ ] Identify a safe, narrowly scoped mutation that preserves native
      configuration initialization and normal error handling. Do not suppress
      the shared exit helper or skip `InitializeConfig` wholesale.
- [ ] Implement it as a version- and ABI-pinned `rawResourcePatch`, with exact
      input-byte guards and `default = false` until validated.
- [ ] Build and install a minimal patched control, then re-enable the four
      Zalo feature patches cumulatively, one at a time. Cold-start each APK
      before any data or feature QA.
- [ ] Record input/bundle/options hashes, APK certificates, device/Android
      version, native evidence, and startup logs in the QA/release record.

## Device QA (Zalo 26.08.01, all four patches)

- [ ] After startup passes, run notification preservation QA: Timeline/Stories
      and Zalo Video are suppressed while message, call, friend-request, and
      birthday notifications still arrive; follow the [QA checklist](qa-checklist.md).
- [ ] Exercise the non-destructive reinstall cycle using the backup/restore
      helpers, Zalo's in-app message backup, and install → restore → login
      order. Confirm media and message associations and record provenance.
- [ ] Run the required SDK-verified re-patch, or document a verifier waiver
      only after device QA and reproducible toolchain evidence.

## Awaiting go-ahead (do not implement unasked)

- [ ] Choose one bounded UI cleanup target for 260801903 (inbox boxes,
      zcloud banner, Me rows, or bottom tabs); prefer a smaller surface before
      navigation-state changes.
- [ ] If approved, compare the reference project's 260801903 base-APK hash
      with our extracted original base APK, independently re-verify candidate
      symbols, and record owner/signature, semantic anchor, callers,
      consumers, mutation, and regression risks in local analysis.
- [ ] Decide whether to add analytics DAO suppression (opt-in, off by default)
      and whether ACTIVITY_UPDATES / USER_INTERACTIONS remain out of the
      promo-notifications patch. Preserve unrelated database operations and
      genuine notifications.

## Local data-transfer patch (research only; implementation not approved)

- [ ] Inspect native backup, restore, and phone-transfer entry points for
      Zalo 26.08.01 / 260801903; verify candidates in smali and determine
      whether an existing flow can be exposed without replacing its machinery.
- [ ] Trace message databases, attachment references, snapshot handling, and
      encryption/key lifecycle; establish reinstall/device/account constraints.
- [ ] Choose a bounded scope from the evidence: prefer a usable native
      transfer flow, with external-media export/import as fallback. Obtain
      explicit approval before implementation or adding UI.
- [ ] Before claiming restore support, demonstrate a disposable-data round
      trip with attachment associations, interruption recovery, and
      wrong-account/version rejection; distinguish media-only from full-chat
      results.

## Release

- [ ] After required QA passes, ship the stable release following the
      [release process](release.md).
