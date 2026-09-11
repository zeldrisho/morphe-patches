# Plan — remaining work

## Authentication and restore

- [ ] Resolve OAuth authorization for the re-signed Zalo build through an
      authorized Google configuration, or use the stock VNG-signed build.
- [ ] Repeat the Drive restore E2E on SM-S936B after authorization is corrected;
      retain only bounded, sanitized logs and never record credentials or tokens.

## Device QA and release

- [ ] Complete the [QA checklist](qa-checklist.md) for startup, lifecycle,
      crypto/DAO, integrity, ad surfaces, and notification preservation.
- [ ] Run SDK-verified re-patching, or document a verifier waiver after device
      QA and reproducible toolchain evidence.
- [ ] Stage and ship a versioned release only after authentication, restore, and
      device QA pass, following the [release process](release.md).
