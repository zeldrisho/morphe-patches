# Plan — remaining work

## Provider-backed authentication and restore

- [ ] Resolve OAuth authorization for the re-signed client through an authorized
      provider configuration, or document the original-signing-identity
      requirement as unsupported.
- [ ] Repeat the provider-backed restore E2E on the target device after
      authorization is corrected; retain only bounded, sanitized logs and never
      record credentials or tokens.

## Device QA and release

- [ ] Complete the [QA checklist](qa-checklist.md) for startup, lifecycle,
      crypto/DAO, integrity, ad surfaces, and notification preservation.
- [ ] Run SDK-verified re-patching, or document a verifier waiver after device
      QA and reproducible toolchain evidence.
- [ ] Stage and ship a versioned release only after authentication, restore, and
      device QA pass, following the [release process](release.md).
