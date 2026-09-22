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

### Synthetic tests versus APK qualification — mostly complete

Targets: `patches/src/test/`, Gradle wiring, `scripts/apk_qualification.py`,
`docs/validation.md`.

Completed:

- Patch-list and embedded-extension metadata have structural and negative-fixture
  coverage.
- `qualifyZaloApk` checks every adjacent split's package, version, certificate,
  arm64 native library, unsupported ABIs, and pinned stock certificate.
- Synthetic fixtures cover wrong package/version, missing or unexpected
  certificates, split metadata mismatch, and split certificate mismatch.

Remaining:

- Add transformation and ambiguity fixtures only when a concrete patch exposes a
  gap. Proprietary APKs remain excluded from Git/public CI.

### Embedded extension contracts — complete

Targets: `patches/build.gradle.kts` and bundle verification tests.

- Bundle verification validates exact embedded DEX method descriptors and
  required public/static flags.
- Negative archive fixtures reject missing and cross-app artifacts; descriptor,
  flag, class, and corrupt DEX failures remain enforced by the verifier.

### Fresh structural patch metadata — complete

Targets: the isolated generation task, `PatchListValidatorTest.kt`, and
`PatchesListShapeTest.kt`.

- Fixtures now cover malformed metadata, duplicate/changed patch entries,
  stale or blank version input, and generated metadata agreement with the
  project release version.
- Exact target metadata, risky package-renaming defaults, all declared targets,
  app association, option shape, and uniqueness within an app remain covered.
  Generation selects the exact versioned bundle and rejects ambiguous artifacts.

## P2: Runtime tests and performance

### Android-runtime extension coverage — remaining

Targets: `extensions/zalo/src/test/` and `extensions/threads/src/test/`.

- Provider enabled/disabled/missing/null-application cases and invalid refresh
  input coverage are implemented; Threads feed filtering has broad shape and
  cache coverage.
- Delayed refresh reflection failures, null/stale targets, and thrown target
  exceptions are now covered by unit tests.
- Remaining Zalo cases: package-manager failure, dialog cancellation, unavailable
  browser, finishing/destroyed activities, and an observable repeated-selection
  coalescing test. The latter require platform-backed seams or device validation.
- Add focused platform-backed tests without introducing a DI framework or generic
  UI stack. Keep real provider/OAuth behavior separately device-qualified.

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

### Build-plugin compatibility inventory — complete for current toolchain

Targets: `settings.gradle.kts`, Gradle wiring, and `docs/toolchain.md`.

- Documented Gradle 9.7.1, AGP 9.1.0, Kotlin 2.4.10, Java 21, SDK 36, and the
  unresolved standalone D8/R8 status.
- No upgrade is proposed. Future upgrades still require canonical verification,
  both extension test suites, embedded DEX contracts, original-APKM repatching,
  and device smoke tests.

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

### Original APK signing identity — mostly complete

- `qualifyZaloApk` validates the pinned stock certificate and every adjacent split;
  synthetic fixtures cover missing, unexpected, mismatched, and repacked inputs.
- Remaining: exercise repeated qualification and output-signing checks in the
  release procedure with private artifacts. Keep original certificate DER/digests
  distinct from output signing identity. This is input qualification, not an
  OAuth fix.

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
