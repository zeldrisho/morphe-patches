# Reference checkout review

The scoped reference comparison is complete: repository inventories, relevant
feature implementations, test inventories, and Morphe build wiring were compared
with our patch sources and existing roadmaps. This was not a line-by-line audit of
all unrelated app patches, an upstream build, or device qualification. No upstream
source was copied during this review.

Local checkouts are not build inputs and need not be retained. Pending Zalo work
remains in [the feature roadmap](plan.md); engineering follow-ups remain in
[repository maintenance](maintenance.md). Completing this review does not mark
those implementation or validation tasks done.

## Reproducible sources

| Former checkout | Upstream at reviewed revision | Recovery ref | Root license |
| --- | --- | --- | --- |
| `zalo-patch` | [amarinne/zalo-patch](https://github.com/amarinne/zalo-patch/tree/deadb56fd586e68bd643ea5ea6cae3960996c9a0) | `v0.4.196` | MIT, plus third-party notices |
| `com.ez.zalopatch` | [Xposed-Modules-Repo/com.ez.zalopatch](https://github.com/Xposed-Modules-Repo/com.ez.zalopatch/tree/6f9d3bf2bb32d000c514fd7b8f3eb880ab81db76) | `208-0.4.204` | MIT |
| `morphe-patches-doom` | [rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches/tree/51561b07a5293663be070e8fce35c023d69da86e) | `v1.22.0` | GPLv3 |
| `morphe-patches-hoodles` | [hoo-dles/morphe-patches](https://github.com/hoo-dles/morphe-patches/tree/f7a88fc5fea593aa462e99e4cf3f7a10176c8c8f) | `v1.44.0` | GPLv3 |

Before retirement, `git ls-remote origin` confirmed each exact revision at the
listed ref. All four working trees were clean, with no untracked/ignored files or
branch commits missing from locally recorded remote refs. Tracked references in
this repository were documentation-only, and no workspace symlinks were found.
Upstream availability is not a permanent archival guarantee.

To recover a source, clone its upstream URL, then `git checkout --detach <full-SHA>`
using the revision in the link above. Verify `git rev-parse HEAD` before using the
source paths in the roadmap. Preserve applicable file-level attribution, LICENSE,
and NOTICE requirements if code is later reused.

## Comparison decisions

### Zalo Patch

Java paths below are relative to `app/src/main/java/com/ez/zalopatch/`.
The source and release README target 26.08.02 (`260802903`), not our 26.08.01.
The release-documentation repository contains README, SUMMARY, and LICENSE, not a
second feature implementation; its README links to source release `v0.4.196`.

| Source area | Decision |
| --- | --- |
| `xposed/features/BackupPushFeature.java`, `BackupPushDecision.java` | Keep the existing P1 native scheduling investigation; the decision helper selects 1/3/6/12 hours, not evidence of completed backup or restore. |
| `xposed/features/InboxFeature.java`, `ChatFeature.java` | Existing category/reaction-row candidates remain; preserve original datasets and ordinary popup actions. Media-box cleanup is now recorded separately. |
| `NotificationHistoryStore.java`, `xposed/features/NotificationFeature.java` | Keep opt-in history separate from promotional filtering; capture is not recovery of messages never observed. |
| `xposed/features/CallRecordingFeature.java`, `CallRecordingLifecycle.java` | Keep the existing default-off recording investigation. Upstream shared MediaStore output and optional status notifications are not our required private-storage/visible-status design. |
| `xposed/features/PasscodeGraceFeature.java` | Keep the existing security-sensitive grace-period investigation, not a global preference override. |
| `xposed/features/StatusPrivacyFeature.java`, `StatusPrivacyAckFilter.java` | Seen/typing behavior overlaps our existing patches; keep two-account validation, not a duplicate feature or asymmetric-presence claim. |
| `xposed/features/TelemetryFeature.java`, `TelemetryDaoShape.java` | Analytics DAO and Crashlytics overlap existing coverage. Preserve Firebase event/measurement-binding leads as unverified gaps; do not port framework-wide interception. |
| `xposed/features/ZinstantFeature.java`, `ZcloudBannerFeature.java` | Compare Zinstant message/feed views with current Adtima and sponsored-config gates. Record the inbox banner as optional UI cleanup, not a zCloud unlock. |
| `xposed/features/WebLinkExternalizeFeature.java`, `WebLinkExternalizeGate.java` | Missing candidate added: externalize ordinary web content only; preserve mini-app, OA H5, authenticated and payment flows. |
| `xposed/features/BottomTabsFeature.java`, `MeCleanupFeature.java` | Missing candidate added: optional navigation/Me cleanup. Do not copy reflective field-order guesses or broad text-based UI interception; zStyle remains excluded. |
| `SymbolSchema.java`, discovery/probe/trace features and LSPosed plumbing | Not a porting requirement. Keep exact patch-time fingerprints, fixed build-time rules, and private bounded diagnostics. |

The upstream test inventory includes backup decisions, recording lifecycle,
passcode grace, seen-ack filtering, external-link classification, telemetry, and
artifact identity. These are test-design references for corresponding candidates,
not proof that upstream or our patches work on the pinned APK.

### Doom and Hoodles

Neither source tree declares direct Zalo or Threads targets. Their unrelated
app/premium catalogs are not a request to expand this repository's two-app scope.
Source roots and existing candidate paths remain in [the roadmap](plan.md).

- **Doom:** retain Messenger transcoding/capture/bubble and Amazon autoplay/search
  telemetry leads. Messenger's no-op transcoding result is app-specific; it does
  not prove Zalo has a safe original-file fallback. The additional
  `messenger/linkhandling/OpenLinksExternallyPatch.kt` lead is consolidated with
  Zalo external-link work, not its unconditional all-links behavior.
- **Hoodles:** retain CamScanner/SoundCloud telemetry, Prime Video native speed,
  and GitHub/SoundCloud AMOLED leads. SoundCloud's message-handler early return
  must not become a generic Zalo handler suppression. The additional
  `googlenews/customtabs/EnableCustomTabsPatch.kt` lead belongs to the same
  external-link candidate; do not force browser support eligibility.
- **Engineering:** inspected `patches/build.gradle.kts` in both repositories.
  Doom separates generator-only Gson runtime dependencies; Hoodles includes
  native/ELF helpers and shared extension infrastructure. Existing metadata,
  dependency-boundary, embedded-extension, toolchain, and provenance work covers
  the relevant engineering concerns. Do not import their release automation,
  Java target, native helpers, or shared framework without a concrete need.
- **Excluded/deferred:** global entitlement and signature spoofing, broad MicroG
  rewrites, HTTP-header mutation as an OAuth fix, Pairip/Hermes infrastructure,
  forced version/update suppression, and unrelated app ports. Existing roadmap
  boundaries remain authoritative.

## Closure

Reference discovery and disposition are complete for these revisions. New feature
candidates still require exact 26.08.01 smali evidence, independent compatibility
checks, narrow implementations, regression tests, and device/control results.
No build or device result is claimed by this documentation-only cleanup.
