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

## Deferred until a concrete trigger

- **Transformation/ambiguity fixtures:** add when a patch exposes a qualification
  gap; proprietary APKs stay out of Git and public CI.
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
