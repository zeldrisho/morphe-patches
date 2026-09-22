# Validation and release qualification

This document describes the repeatable build, installation, and device-validation
procedure for a supported target. It is a procedure, not a record of a previous
run. Record the result and provenance for each run in the release or pull-request
record.

Run manual end-to-end validation on a **throwaway account** where an account is
required. A re-signed build with a VPN/proxy on a real account may create account
risk; see [lessons learned](validation.md#device-validation-scope). Use the
version and package metadata defined by the target compatibility constants.

## Build validation

Run the repository tests and build the Android bundle:

```bash
./gradlew :patches:test buildAndroid --no-daemon
python3 -m unittest discover -s scripts/tests -v
```

Shell, shfmt, and workflow lint are covered by the `pre-commit` gate in Verify;
do not rerun `shellcheck` or `actionlint` separately here.

The bundle is written to `patches/build/libs/patches-*.mpp`. Successful Check
runs retain a `patches-<sha>-<attempt>` artifact for seven days. Record the
run/commit and downloaded bundle hash. CI artifacts are test builds, not releases.

Optional local target qualification requires the original pinned APK and fails
when it is absent; synthetic CI tests do not count as APK compatibility proof:

```bash
ZALO_TEST_APK=/private/path/to/zalo-base.apk ./gradlew :patches:qualifyZaloApk --no-daemon
```

The task checks the pinned version metadata and reports the result separately
from ordinary synthetic tests. Tests must not commit or download proprietary APKs.

## Repeatable device journeys

Run these journeys on a throwaway account with a stock or minimally re-signed
control before the selected-patch build. Use `android layout` as the primary
UI inspection method and screenshots only as secondary evidence; keep all device
artifacts under the ignored analysis directory.

| Journey | Preconditions | Required assertions |
| --- | --- | --- |
| Cold launch and resume | Fresh install, selected device | Launch, background, kill, resume, and no crash/freeze; compare control |
| Missing provider | Provider absent or disabled | Prompt is cancellable and normal Zalo use remains available |
| Account selection and refresh | Provider installed, test account | Selected account is reflected after callback; no stale-account refresh |
| Threads feed filtering | Test feed containing organic and sponsored units | Sponsored units disappear while organic ordering, scrolling, and refresh remain intact |
| Notification delivery | Notifications enabled, app backgrounded | Chat/call/alert notifications remain delivered; only intended promotional notifications change |

Versioned blank execution sheets are provided for [Zalo 26.08.01](journeys/zalo-26.08.01.md)
and [Threads 445.0.0.46.83](journeys/threads-445.0.0.46.83.md). They intentionally
contain no device results.

Evaluate each action in order. Mark every assertion `PASS`, `FAIL`, or `BLOCKED`;
if a step fails, leave later steps explicitly unexecuted. A successful tap is not
itself evidence of the expected state. Record package/version, APK and bundle
hashes, enabled patches, device/Android version, and certificate fingerprint;
never record credentials or tokens.

## Controlled performance baseline

For cold launch and Threads scrolling, repeat stock, minimally re-signed control,
and selected-patch runs on the same device and network conditions. Capture startup
time, frame timing/jank, memory, and relevant background/network activity. Run
at least three repetitions, record variance, and set thresholds only after the
control variance is known. Keep traces, heap dumps, screenshots, and UI dumps
outside Git and delete them according to the local analysis retention policy.

## Re-patch and install

Patch and install the selected input APK explicitly:

```bash
MPP="patches/build/libs/patches-<version>.mpp" \
  python3 scripts/repatch.py /path/to/input.apkm /tmp/patched.apk
adb install -r /tmp/patched.apk
```

Only update an existing installation when its signing certificate matches.

For startup isolation, `repatch.py` accepts a strict comma-separated patch
allow-list. Start with a minimal control, then enable the target's patches
incrementally; install and cold-start each control before enabling the next patch:

```bash
PATCHES='' python3 scripts/repatch.py /path/to/input.apkm /tmp/control-0.apk
PATCHES='<first patch>' python3 scripts/repatch.py /path/to/input.apkm /tmp/control-1.apk
PATCHES='<first patch>,<second patch>' python3 scripts/repatch.py /path/to/input.apkm /tmp/control-2.apk
```

`PATCHES` disables every bundle patch not named and rejects unknown names, so a
control cannot silently include default-on patches.

Release qualification includes one SDK-verified re-patch and a repeated private
output-signing identity check. Compilation alone only proves that the toolchain ran
because the patcher verifier defaults to existence checks:

```bash
MPP="patches/build/libs/patches-<version>.mpp" VERIFY_SDK=1 \
  python3 scripts/repatch.py /path/to/input.apkm /tmp/verified.apk

ZALO_OUTPUT_APK=/private/patched.apk \
ZALO_OUTPUT_CERTIFICATE='certificate SHA-256 digest: ...' \
./gradlew :patches:qualifySignedOutput --no-daemon
```

`qualifySignedOutput` repeats `apksigner` inspection twice by default; set
`QUALIFICATION_REPEATS` for a larger private release check. Keep certificate
output and APKs outside Git. The stock input certificate is validated separately
by `qualifyZaloApk`; never use it as the output identity.

`VERIFY_SDK=1` uses `$ANDROID_HOME`, then `$ANDROID_SDK_ROOT`, then the OS-default
SDK location. Set `VERIFY_SDK=/path/to/sdk` to pin a specific SDK. Record the
verification result with the bundle and input hashes.

If every available toolchain reproduces an internal D8 error that is not a patch
error, the owner may waive verification after device validation. Record the
waiver, toolchain versions, and device evidence in [lessons learned](validation.md),
and revisit it when Morphe fixes the verifier. Do not block a release indefinitely
on a broken verifier.

## Device validation scope

Validate the following areas and record each as **PASS**, **FAIL**, or **BLOCKED**
with concise evidence:

- Manifest and package metadata match the intended target, including removal of
  permissions targeted by a patch. Compare original and patched exported
  components, permissions, provider authorities, URI grants, and package
  visibility; explain every security-relevant delta.
- Splash launch, cold start, background/kill/resume, and lifecycle behavior.
- Existing-session and fresh-login behavior separately; preserve existing data
  unless the test plan explicitly authorizes a reset.
- Every enabled patch has a positive behavior check and a negative/control check.
- Inputs, navigation, network-dependent screens, notifications, media, and
  relevant background work remain functional.
- Messaging and calling, including one-to-one chats, group messaging, and VoIP.
- Notification delivery while the app is in the background.
- Target ad surfaces remain patched without crashes.
- Launch-time missing-provider guidance allows cancellation without blocking use.
- Provider-backed authentication: account selection, transport, token issuance,
  and feature access are assessed separately. Record upstream OAuth attestation
  failures as **BLOCKED**, not as patch failures.
- Provider-backed restore, including initial media restore and a complete backup /
  restore cycle where supported.
- Optional renamed-package/coexistence behavior when supported by the target and
  its signing/OAuth configuration.

If a provider is absent, the app should provide installation guidance without
blocking normal use. Keep request logs bounded and redact credentials and tokens.
Do not promote a stable release while required validation remains blocked.

Record input APK version/code and hash, bundle path/hash, enabled patches, package
ID, device/Android version, and signing certificate fingerprint. Never record
passwords. Keep screenshots, UI dumps, and logs outside Git; retain sanitized
notes in the release or PR record.

## Zalo microG/Drive issue checklist

For Zalo 26.08.01 provider failures, follow the detailed
[Zalo microG/Drive guide](zalo-microg.md). In particular, distinguish a local
provider-discovery failure from account-picker behavior and from OAuth/Drive
backend rejection. Repository issue [#11](https://github.com/zeldrisho/morphe-patches/issues/11)
reported a false “MicroG required” prompt; the patch now declares the provider
package for Android package visibility and uses the upstream MorpheApp download
link. The issue did not include enough logs or provider metadata to establish a
single root cause, so this remains a device-validation item.

## Provider boundaries

Treat provider transport, account selection, token issuance, and upstream
authorization as separate gates. A successful picker or IPC request does not prove
that the provider accepts the patched package and signing certificate. Record
upstream attestation failures as **BLOCKED**, not as patch failures, and keep
credentials and tokens out of logs.

## Version update qualification

For every newly supported version:

1. Confirm fingerprints resolve uniquely and the target ABI remains compatible.
2. Confirm each patch target still has the intended semantics; a matching
   signature alone does not prove behavior.
3. Repeat [build validation](#build-validation) through device/regression
   validation before updating target version metadata.
