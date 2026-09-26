# Remaining repository maintenance

Cross-app backlog only. Implemented procedures: [validation](validation.md) and
[provenance](provenance.md). Zalo candidates: [plan](plan.md). Publishing: [release](release.md).

## Outstanding private validation

These require devices/private artifacts; documentation and synthetic tests do not complete them.

| Priority | Work | Procedure |
| --- | --- | --- |
| P1 | Stock/control/patched Zalo and Threads journeys, including malformed intents and access boundaries | [Device scope](validation.md#device-validation-scope), [versioned sheets](validation.md#repeatable-device-journeys) |
| P2 | Cold-launch/Threads-scroll baselines; backup scheduling when implemented | [Performance baseline](validation.md#controlled-performance-baseline) |
| P2 | Pinned input qualification and repeated output-signing checks | [Build qualification](validation.md#build-validation), [signed output](validation.md#re-patch-and-install) |

No private APK results are recorded here. Keep stock and output signing identities
distinct; retain sanitized results in the release/PR record and raw artifacts outside Git.

## Coverage and test architecture still in progress

The synthetic-method fixture, resource/bytecode transformation tests, extracted
MicroG/media/telemetry helpers, and patch-list formatter tests are in place. The
patches module currently measures **65.4% line coverage** against a **65% floor**;
Threads and Zalo extension baselines are **91.9% / 91%** and **46.2% / 45.5%**.
The approximate 80% patches goal remains unmet. Integration/APK tests remain in
`src/test` and are opt-in through their existing environment checks; no custom
source set was introduced.

- **P1 — Complete synthetic fingerprint matcher coverage:** add positive and
  near-miss synthetic DEX tests for Zalo media, Business Box chat, and telemetry
  fingerprints. The current inventory test initializes fingerprint definitions
  and checks descriptor metadata; it does not validate every matcher against
  matching and mutated methods.
- **P1 — Cover patch orchestration paths:** exercise fallback, required-match
  failures, and replacement-count validation for MicroG, media, telemetry, and
  resource patch execution. Existing unit tests cover extracted helpers, not
  every Morphe execution-closure branch.
- **P2 — Continue toward the 80% patches floor:** add behavior-focused coverage
  for remaining production transformations and error paths; retain tooling in
  scope unless it is explicitly excluded with a documented rationale. Do not
  raise the floor beyond a durable measured baseline.
- **P2 — Grow Zalo extension coverage:** exercise provider/lifecycle failure
  paths and remaining runtime logic with focused Robolectric tests; retain plain
  JVM tests for logic that does not need Android framework behavior.
- **P3 — Keep Threads coverage healthy:** preserve meaningful ad-filter edge
  coverage while adding future runtime features; avoid tests that only defend a
  percentage target.

## Deferred until a concrete trigger

- **Shrinker safety/size:** establish whether extension packaging invokes R8.
  If enabled, protect injected names, descriptors/flags, and reflection contracts;
  extend embedded-Dex checks and measure savings before narrowing keep rules.
  Never shrink the proprietary host APK.
- **Resource mapping:** resolve names to IDs when a UI fingerprint needs it;
  test missing resources, lifecycle, and repeated sessions.
- **README/catalog split:** defer `PATCHES.md` until the two-app catalog is unwieldy.

## Boundaries

No global entitlement spoofing, HTTP-header OAuth fixes, broad MicroG rewrites,
unrelated app ports, or shared runtime infrastructure without a concrete consumer
and independent evidence.

## Acceptance

Use focused branches, targeted regression tests, and [canonical verification](development.md#verify).
Documentation changes do not prove compatibility. No maintenance-only changelog
entries or hand-edited release metadata; follow [release rules](release.md#rules).
