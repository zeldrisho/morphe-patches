# Plan — remaining work

Only outstanding actions. Procedure details live in the linked docs.

## Zalo 26.08.01 startup regression

- [ ] Trace the exception that reaches Zalo's uncaught-exception handler during
      cold start of the Morphe-patched APK. The process exits with
      `System.exit(0)` before the first screen; the unmodified APK remains
      alive. Verify the failing path in smali and identify whether the cause is
      patch bytecode, merged-APKM packaging, re-signing/integrity validation,
      or restored external data.
- [ ] Produce a minimal patched control that launches, then re-enable the
      Zalo patches one at a time. Do not bypass the startup kill branch without
      identifying the underlying exception and preserving normal error handling.
- [ ] Rebuild, install, and cold-start the fixed APK before restoring data or
      claiming any feature result. Record input/bundle/options provenance and
      certificate details in the QA/release record.

## Device QA (Zalo 26.08.01, all four patches)

- [ ] After the startup regression is fixed, run the notification preservation
      test: Timeline/Stories and Zalo Video are suppressed while message, call,
      friend-request, and birthday notifications still arrive — follow the
      [QA checklist](qa-checklist.md).
- [ ] Exercise the non-destructive reinstall cycle on-device using the updated
      backup/restore helpers, with Zalo's in-app message backup and the agreed
      install → restore → login order. Confirm media and message associations,
      and record provenance with the result.
- [ ] Run the required SDK-verified re-patch, or document the verifier waiver
      only after device QA and reproducible toolchain evidence.

## Awaiting go-ahead (do not implement unasked)

- [ ] Choose one bounded UI cleanup target for 260801903 (inbox boxes,
      zcloud banner, Me rows, or bottom tabs); prefer a smaller surface before
      navigation-state changes.
- [ ] If approved, compare the reference project's 260801903 base-APK hash
      with our extracted original base APK, then independently re-verify its
      candidate symbols. Record owner/signature, semantic anchor, callers and
      consumers, proposed mutation, and regression risks in local analysis —
      [external-reference workflow](reverse-engineering.md#learning-from-other-patch-projects).
- [ ] Decide whether to add analytics DAO suppression (opt-in, off by default);
      if approved, verify specific write paths and preserve unrelated database
      operations rather than disabling the database wholesale.
- [ ] Decide whether ACTIVITY_UPDATES / USER_INTERACTIONS channels stay out of
      the promo-notifications patch; do not broaden filtering without explicit
      scope approval and preservation tests.

## Local data-transfer patch (research only; implementation not approved)

- [ ] Inspect Zalo 26.08.01 / 260801903 for native backup, restore, and
      phone-transfer entry points; verify candidates in smali and determine
      whether an existing flow can be exposed without replacing its machinery —
      [data-migration investigation](reverse-engineering.md#investigating-data-migration-patches).
- [ ] Trace message databases, attachment references, snapshot handling, and
      encryption/key lifecycle; establish reinstall/device/account constraints
      before proposing full chat restoration.
- [ ] Choose a bounded patch scope from the evidence: prefer a usable native
      transfer flow, with external-media export/import as the fallback. Obtain
      explicit approval before implementation or introducing in-app UI under the
      current [scope rules](maintenance.md#standing-rules).
- [ ] Before claiming restore support, demonstrate a round trip on disposable
      data, including attachment associations, interruption recovery, and
      wrong-account/version rejection; separate media-only from full-chat
      results.

## Release

- [ ] After required QA passes, ship the stable release following the
      [release process](release.md).
