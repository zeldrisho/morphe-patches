# Remaining repository maintenance

Cross-app engineering backlog. Zalo feature investigations remain in the
[feature roadmap](plan.md); release policy remains in [release.md](release.md).
These items are repository safeguards, not app-feature claims. Keep app-specific
patches and the independent Threads/Zalo extensions; prefer focused tests over a
framework rewrite.

## P1: Release and bytecode regression coverage

### Release preflight tests — medium

Targets: `scripts/prepare_release.py` and `scripts/tests/test_release.py`.

- Add temporary-repository tests for valid subsequent releases, missing/unreachable
  tags, and generator/extraction/commit failures.
- Assert every rejected preflight and failed staging attempt leaves owned and
  unrelated files intact; current coverage includes malformed, duplicate,
  out-of-order, and first-release staging cases.

### Bytecode-helper contracts — medium

Target: `patches/src/main/kotlin/com/zeldrisho/patches/shared/bytecode/MethodExtensions.kt`.

- Add focused tests for zero-local methods, parameter aliases, wide values,
  register encoding boundaries, and try/catch removal, exercising real consumers.
  The whole-body versus instruction-preserving contract is now documented and
  the unsafe register growth was removed from the provider-check injection.

## P2: Qualification and metadata

### Synthetic tests versus APK qualification — medium

Targets: `patches/src/test/`, Gradle wiring, and `docs/validation.md`.

- Inventory synthetic transformation, negative-match, and ambiguity coverage;
  fill gaps rather than duplicating tests.
- Extend the explicit APK qualification task with any newly pinned target
  metadata; it now validates Zalo's package, version code, arm64 split/native
  library, and unsupported-ABI rejection, reporting checks separately.
- Keep proprietary APKs out of Git/public CI and device/control validation as a
  separate release requirement.

### Embedded extension contracts — medium

Targets: `patches/build.gradle.kts` and bundle verification tests.

- Make bundle verification validate exact embedded DEX method descriptors and
  required public/static flags, rather than only DEX symbols. Source-level ABI
  reflection tests now cover the Threads and Zalo extension entry points.
- Add negative fixtures for missing classes, wrong signatures/flags, corrupt DEX,
  and cross-app contamination.

### Fresh structural patch metadata — medium

Targets: the isolated generation task and `PatchesListShapeTest.kt`.

- Cover exact target metadata, risky package-renaming defaults, all declared
  targets, added/removed patches, stale input, malformed metadata, and
  release-metadata agreement. Structural tests now validate app association,
  target presence, option shape, uniqueness within an app, and rename defaults.

## P2: Documentation and maintainability

### Documentation checks — small

- Add more tests for Markdown heading slugs, relative links, and malformed links.
- Audit examples for register-aware injection and links to canonical procedures.

### Selective structural cleanup — small/medium

- Keep package-renaming logic app-specific unless equivalent semantics are proven.
- On the next substantive change, move `zalo/ZaloMicroGSupportPatch.kt` into
  `zalo/microg/` and separate matching/contracts from manifest and bytecode
  transformations. Preserve patch identity, artifacts, and descriptors.
- Keep cross-app maintenance here and Zalo feature work in `plan.md`.

### Source provenance and vendored rules — small

- Keep attribution, upstream revision, local deviations, and update policy near
  borrowed implementations.
- Keep executable filtering rules fixed at build time; updates require review and
  a bundle release, never runtime fetching.

## P2: Qualification boundaries

### Original APK signing identity — medium

- Validate the input certificate for signature-sensitive patching and test
  already-repatched inputs, missing/unexpected certificates, and repeated runs.
- Distinguish original certificate DER, SHA-1 digest, and output signing identity.
  Keep proprietary inputs private and diagnostics bounded. This is input
  qualification, not a provider/OAuth fix.

## Deferred until a concrete trigger

- **Patch-time resource mapping:** resolve resource names to IDs only when a UI
  patch needs resource-literal fingerprinting; then test missing resources,
  lifecycle, and repeated sessions.
- **Compact README/catalog split:** defer `PATCHES.md` and a compact README index
  until the two-app catalog becomes unwieldy.

## Boundaries

Do not import global entitlement spoofing, HTTP-header mutation as an OAuth fix,
broad MicroG rewrites, unrelated app ports, or generic shared runtime
infrastructure without a concrete consumer and independent evidence.

## Acceptance

Implement remaining items in focused branches with targeted regression tests and
run the canonical [verification procedure](development.md#verify). Documentation
changes need local link/text checks; they do not establish APK or device
compatibility. Do not add maintenance-only entries to the user-visible changelog
or hand-edit release-owned generated metadata.
