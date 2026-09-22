# Remaining repository maintenance

Cross-app engineering backlog. Completed safeguards have been removed from this
plan; see [validation.md](validation.md), [provenance.md](provenance.md), and the
relevant tests for the implemented procedures. Zalo feature investigations remain
in [plan.md](plan.md), and release policy remains in [release.md](release.md).

## Not executable in this change

### Device journeys — P1

Device execution is intentionally not included in this change.

- Run the versioned journeys for [Zalo 26.08.01](journeys/zalo-26.08.01.md)
and [Threads 445.0.0.46.83](journeys/threads-445.0.0.46.83.md) on stock/control
and selected-patch builds.
- Record `PASS`, `FAIL`, or `BLOCKED` for every assertion and mark later steps
  explicitly unexecuted after a failure.
- Verify cold/warm launch and resume, missing-provider cancellation, account
  selection and Drive refresh, notifications, Threads filtering, malformed or
  unexpected intents, and unauthorized access where applicable.
- Use throwaway accounts and selected devices; keep APKs, screenshots, UI dumps,
  account data, and logs outside Git.
- Preserve existing data unless a reset is explicitly authorized. Do not treat a
  successful interaction as proof of provider/OAuth, restore, or security behavior.

### Controlled performance baselines — P2

Performance measurement is also device work and remains outstanding.

- Compare stock, minimally re-signed control, and selected-patch builds on the
  same device and network conditions.
- Measure cold launch, Threads scrolling, startup time, frame timing/jank,
  memory, and relevant background/network activity. Add backup scheduling when
  implemented.
- Establish control variance before setting thresholds; keep traces and heap
  dumps private and bounded. Do not make release APKs debuggable.

## Requires private release artifacts

### Repeated signing qualification — P2

The tooling is implemented, but no private APK result is recorded in Git.

- Run `:patches:qualifyZaloApk` against the pinned original APKM contents.
- Run `:patches:qualifySignedOutput` against the produced APK with
  `ZALO_OUTPUT_CERTIFICATE` and at least two inspections (the default).
- Keep the stock input certificate distinct from the output signing identity and
  record only sanitized hashes/results in the release or PR record.

Example:

```bash
ZALO_OUTPUT_APK=/private/patched.apk \
ZALO_OUTPUT_CERTIFICATE='certificate SHA-256 digest: ...' \
./gradlew :patches:qualifySignedOutput --no-daemon
```

## Deferred until a concrete trigger

- **Transformation and ambiguity fixtures:** add them when a concrete patch
  exposes a qualification gap; proprietary APKs remain excluded from Git and
  public CI.
- **Shrinker safety and size analysis:** determine whether extension packaging
  invokes R8. If enabled, protect injected names, exact descriptors/flags, and
  reflection contracts; extend embedded-Dex checks and measure savings before
  narrowing keep rules. Do not shrink the proprietary host APK.
- **Patch-time resource mapping:** resolve resource names to IDs only when a UI
  patch needs resource-literal fingerprinting; test missing resources, lifecycle,
  and repeated sessions then.
- **Compact README/catalog split:** defer `PATCHES.md` and a compact README index
  until the two-app catalog becomes unwieldy.

## Boundaries

Do not import global entitlement spoofing, HTTP-header mutation as an OAuth fix,
broad MicroG rewrites, unrelated app ports, or generic shared runtime
infrastructure without a concrete consumer and independent evidence.

## Acceptance

Complete remaining items in focused branches with targeted regression tests and
run the canonical [verification procedure](development.md#verify). Documentation
changes do not establish APK or device compatibility. Do not add maintenance-only
entries to the user-visible changelog or hand-edit release-owned generated
metadata.
