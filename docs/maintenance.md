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

The reusable synthetic-method fixture and initial Zalo/Threads transformation
checks are in place. They do not yet provide broad coverage of production patch
execution or extension lifecycle behavior. Current line coverage and enforced
floors are: patches **40.8% / 40%**, Threads **91.9% / 91%**, and Zalo
**46.2% / 45.5%**. The approximate 80% goal remains unmet for patches and Zalo.

- **P1 — Grow meaningful patch coverage:** prioritize bytecode/resource
  transformations, fallback/error paths, and mismatch handling across Zalo,
  Threads, and future app targets. Reuse
  `patches/src/test/kotlin/com/zeldrisho/patches/testing/`; do not test only
  fingerprint declarations or proprietary APK fixtures. Raise floors only after
  durable behavior coverage exists.
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
