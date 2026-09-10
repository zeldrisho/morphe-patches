# Plan — remaining work

Procedure details and durable decisions live in the linked docs.

## Zalo 26.08.01 startup

- [ ] Re-run the native integrity/configuration trace on the supplied rooted
      device. Cover all validation-failure edges, the Java/JNI termination
      caller, and native `openat`/`fopen`/`read`/`stat`/`mmap` paths.
- [ ] Resolve whether the filesystem checks independently validate the
      certificate or only load configuration / inspect the environment.
- [ ] Only if the trace is complete, design and implement the smallest
      version/ABI-pinned mutation that preserves initialization and normal
      error handling. Use exact input-byte guards and `default = false`; do
      not suppress shared exit helpers or skip `InitializeConfig`.
- [ ] Build and cold-start a minimal patched control, then enable the four Zalo
      feature patches cumulatively, one at a time.
- [ ] Record input, bundle, options, certificate, device, native evidence, and
      startup logs in the QA/release record.

See [maintenance](maintenance.md#current-zalo-work-guardrails) and the
[dynamic confirmation workflow](reverse-engineering.md#dynamic-confirmation-for-runtime-gates).

## Device QA

- [ ] After startup passes, verify promotional-notification suppression and
      preservation of message, call, friend-request, and birthday notifications
      using the [QA checklist](qa-checklist.md).
- [ ] Complete the backup/reinstall/login/restore cycle; verify media/message
      associations and record provenance.
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.

## Pending approval

- [ ] Choose one bounded UI cleanup target for 260801903 and independently
      verify its symbols, semantics, callers, consumers, mutation, and risks.
- [ ] Decide whether opt-in analytics DAO suppression is wanted while
      preserving unrelated database operations and genuine notifications.

## Local data-transfer research

- [ ] Inspect native backup, restore, and phone-transfer entry points for
      260801903 and verify candidates in smali.
- [ ] Trace databases, attachment references, snapshots, encryption/key
      lifecycle, and reinstall/device/account constraints.
- [ ] Obtain approval for a bounded scope before implementation or UI changes.
- [ ] Demonstrate a disposable-data round trip with attachment associations,
      interruption recovery, and wrong-account/version rejection before claiming
      restore support; distinguish media-only from full-chat results.

## Release

- [ ] After required QA passes, ship the stable release following the
      [release process](release.md).
