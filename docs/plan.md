# Plan — remaining work

Completed implementation and build details are recorded in the repository history;
durable diagnostic guidance lives in [Lessons learned](lessons-learned.md).

## Zalo Drive authorization

- [ ] Resolve MicroG OAuth authorization for the Morphe-signed Zalo build. The
      picker and `app.revanced` token-service path work, but MicroG currently
      returns `UNREGISTERED_ON_API_CONSOLE` because the installed APK's signing
      certificate is not registered for the OAuth client.
- [ ] Repeat the Drive restore E2E on SM-S936B after OAuth configuration is
      corrected. Confirm account selection, token acquisition, and photo
      restore without the Add Account webview; retain only bounded, sanitized
      logs and never record credentials or OAuth tokens.

## Device QA and release

- [ ] Complete the [QA checklist](qa-checklist.md) for cold start, lifecycle,
      crypto/DAO and integrity behavior, required ad surfaces, and preservation
      of message, call, friend-request, and birthday notifications.
- [ ] Run SDK-verified re-patching, or document a verifier waiver only after
      device QA and reproducible toolchain evidence.
- [ ] Stage and ship a versioned release only after authentication, restore,
      and device QA pass, following the [release process](release.md).
