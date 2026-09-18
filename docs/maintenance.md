# Remaining repository maintenance

Cross-app engineering backlog. Zalo feature investigations remain in the
[feature roadmap](plan.md); release policy remains in [release.md](release.md).
These items are repository safeguards, not app-feature claims. Keep app-specific
patches and the independent Threads/Zalo extensions; prefer focused tests over a
framework rewrite.

## P1: Release and bytecode regression coverage

### Release preflight tests — medium

Targets: `scripts/prepare_release.py` and `scripts/tests/test_release.py`.

- Add temporary-repository tests for malformed and out-of-order headings,
  duplicate entries, valid first/subsequent releases, missing/unreachable tags,
  and generator/extraction/commit failures.
- Assert every rejected preflight and failed staging attempt leaves owned and
  unrelated files intact.

### Bytecode-helper contracts — medium

Target: `patches/src/main/kotlin/com/zeldrisho/patches/shared/bytecode/MethodExtensions.kt`.

- Separate whole-body replacement from instruction-preserving injection contracts.
  Increasing register count does not rewrite existing encoded parameter operands.
- Add focused tests for zero-local methods, parameter aliases, wide values,
  register encoding boundaries, and try/catch removal, exercising real consumers.

## P2: Qualification and metadata

### Synthetic tests versus APK qualification — medium

Targets: `patches/src/test/`, Gradle wiring, and `docs/validation.md`.

- Inventory synthetic transformation, negative-match, and ambiguity coverage;
  fill gaps rather than duplicating tests.
- Make the explicit APK qualification task validate the pinned version code and
  required ABI/library, and report passed, failed, and skipped checks separately.
- Keep proprietary APKs out of Git/public CI and device/control validation as a
  separate release requirement.

### Embedded extension contracts — medium

Targets: `patches/build.gradle.kts` and bundle verification tests.

- Validate exact injected method descriptors and required public/static flags,
  rather than only DEX symbols.
- Add negative fixtures for missing classes, wrong signatures/flags, corrupt DEX,
  and cross-app contamination.

### Fresh structural patch metadata — medium

Targets: the isolated generation task and `PatchesListShapeTest.kt`.

- Assert each patch’s app association, exact targets, defaults, options, and
  uniqueness within its app; identical names across apps may be valid.
- Cover risky package-renaming defaults, all declared targets, added/removed
  patches, stale input, malformed metadata, and release-metadata agreement.

## P2: Documentation and maintainability

### Documentation checks — small

- Replace remaining blanket `catch(Throwable)` guidance with scoped host-boundary
  handling and bounded diagnostics.
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

### Architecture availability — small

- Make APK qualification test unsupported architecture, missing native library,
  wrong version code, and rejection before mutation while preserving exact native
  preconditions. Do not add generic resolver composition without evidence.

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
