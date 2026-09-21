# Remaining repository maintenance

Cross-app engineering backlog. Zalo feature investigations remain in the
[feature roadmap](plan.md); release policy remains in [release.md](release.md).
These items are repository safeguards, not app-feature claims. Keep app-specific
patches and the independent Threads/Zalo extensions; prefer focused tests over a
framework rewrite.

## P1: Device journeys and component boundaries

### Repeatable device-validation journeys — medium

Targets: `docs/validation.md` and a small set of versioned journey specifications.

- Execute the documented launch, background/resume, missing-provider cancellation,
  account selection/Drive refresh, notification, and Threads feed journeys on the
  pinned APK and a control build; the procedure is documented but no device result
  is recorded yet.
- Record PASS, FAIL, or BLOCKED per check and distinguish unexecuted steps after
  failure. A successful interaction alone does not establish expected behavior.
- Prefer UI identifiers/accessibility labels over fixed coordinates; add a small
  external UI Automator harness only if repeated manual runs justify it.
- Use throwaway accounts and explicitly selected devices. Keep APKs, screenshots,
  UI dumps, and account data outside Git. Preserve existing data unless reset is
  explicitly authorized.
- Accept when another maintainer can reproduce the checks on the pinned APK and
  inspect the evidence independently; automation does not replace remote/restore
  verification.

### Patch-induced component and intent boundaries — medium

Targets: manifest/package rewrites, extension entry points, and APK qualification.

- Compare original and patched exported components, permissions, provider
  authorities, URI grants, and package visibility; explain every security-relevant
  delta, especially package-renaming and MicroG changes.
- Test cold and warm intent delivery where modified components handle both, and
  malformed extras, unexpected destinations, and unauthorized access where relevant.
- Require narrowly scoped URI access for future export/history/recording features.
  Preserve intended authentication and sharing; do not globally harden unrelated
  stock components or treat package visibility as caller authentication.
- Accept when changes introduce no unexplained exposure or authority/permission
  collision. This is a safeguard, not a claim of an existing vulnerability.

## P2: Qualification and metadata

### Synthetic tests versus APK qualification — medium

Targets: `patches/src/test/`, Gradle wiring, and `docs/validation.md`.

- Inventory synthetic transformation, negative-match, and ambiguity coverage;
  fill gaps rather than duplicating tests.
- Run the explicit APK qualification task against the pinned proprietary input,
  including the base/split package, version, certificate, arm64 native library,
  and unsupported-ABI checks. Add isolated mismatch fixtures for split metadata
  and certificates without committing APKs.
- Keep proprietary APKs out of Git/public CI and device/control validation as a
  separate release requirement.

### Embedded extension contracts — medium

Targets: `patches/build.gradle.kts` and bundle verification tests.

- Make bundle verification validate exact embedded DEX method descriptors and
  required public/static flags, rather than only raw symbol/type presence. The
  current source-level ABI reflection tests are not sufficient for this.
- Add negative fixtures for missing classes, wrong signatures/flags, corrupt DEX,
  and cross-app contamination.

### Fresh structural patch metadata — medium

Targets: the isolated generation task and `PatchesListShapeTest.kt`.

- Cover exact target metadata, risky package-renaming defaults, all declared
  targets, added/removed patches, stale input, malformed metadata, and
  release-metadata agreement. Structural tests now validate app association,
  target presence, option shape, uniqueness within an app, and rename defaults.

## P2: Runtime tests and performance

### Android-runtime extension coverage — medium

Targets: `extensions/zalo/src/test/` and `extensions/threads/src/test/`.

- Add focused platform-backed tests, using Robolectric where compatible with the
  Morphe build, without adding a dependency-injection framework or generic UI stack.
- Existing JVM tests cover provider classification and invalid refresh inputs; add
  missing/disabled provider, package-manager failure, dialog cancellation,
  unavailable-browser, and finishing/destroyed-activity cases.
- Add deterministic delayed-refresh tests for missing reflective methods,
  invocation failures, repeated selection, stale lifecycle views, and coalescing.
- Assert safe fallback behavior without unnecessary host-app blocking. Keep real
  provider/OAuth behavior and host integration separately device-qualified.

### Controlled performance baselines — medium

Targets: local device-validation procedures and ignored analysis artifacts.

- Compare stock, minimally re-signed control, and selected-patch builds on the same
  device. Start with cold launch and Threads scrolling; add native backup scheduling
  when implemented.
- Measure startup, frame timing, memory, and relevant background/network activity.
  Establish repeatability and variance before setting regression thresholds.
- Check profiling capabilities first; do not silently make release APKs debuggable.
  Keep traces and heap dumps private, with bounded capture and retention.
- Accept when measurements distinguish patch overhead from signing, environment,
  and backend effects rather than merely producing a trace.

### Build-plugin compatibility inventory — small/medium

Targets: `settings.gradle.kts`, Gradle wiring, and `docs/toolchain.md`.

- Record the effective AGP, Gradle, Kotlin, D8/R8, SDK, and Java versions and
  the combinations supported by `app.morphe.patches`; the repository currently
  records Java/Gradle setup but not the resolved AGP/R8 versions. Do not infer AGP
  from the wrapper.
- Upgrade only when needed and supported by Morphe. Check public DSL/variant APIs,
  extension packaging, and Android versus JVM Kotlin configuration separately.
- Require canonical verification, both extension test suites, embedded DEX
  contracts, original-APKM repatching, and device smoke tests; Gradle help/dry-run
  success alone is not upgrade qualification.

## P2: Documentation and maintainability

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

- **Shrinker safety and size analysis:** first establish whether extension
  packaging invokes R8. If enabled, protect injected entry-point names, exact
  descriptors/flags, and reflection contracts; extend the embedded DEX checks
  above and measure savings before narrowing keep rules. Do not assume an app
  module/analyzer task exists or shrink the proprietary host APK.
- **Patch-time resource mapping:** resolve resource names to IDs only when a UI
  patch needs resource-literal fingerprinting; then test missing resources,
  lifecycle, and repeated sessions.
- **Compact README/catalog split:** defer `PATCHES.md` and a compact README index
  until the two-app catalog becomes unwieldy.

## Android skills references

These are investigation references, not instructions to install tools or migrate
frameworks automatically. Verify commands against the installed version and record
an upstream revision when adopting code or procedures:

- [Android CLI and device journeys](https://github.com/android/skills/blob/main/devtools/android-cli/SKILL.md)
- [Testing setup](https://github.com/android/skills/blob/main/testing/testing-setup/SKILL.md)
- [Intent security](https://github.com/android/skills/blob/main/security/android-intent-security/SKILL.md)
- [Android profiler](https://github.com/android/skills/blob/main/profilers/android-profiler/SKILL.md)
- [R8 analyzer](https://github.com/android/skills/blob/main/performance/r8-analyzer/SKILL.md)
- [AGP 9 upgrade](https://github.com/android/skills/blob/main/build-system/agp/agp-9-upgrade/SKILL.md)

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
