# Remaining work

This roadmap is scoped to Zalo Android APK `26.08.01` (version code
`260801903`). APKs, smali, logs, screenshots, and generated analysis files stay
under the ignored `analysis/zalo/26.08.01/` directory.

Cross-app structure, patch safety, tests, tooling, release safeguards, and
documentation work are tracked in the [repository maintenance plan](maintenance.md).
The current maintenance implementation has completed bundle/metadata checks,
split APK qualification, certificate fixtures, manifest-diff tooling, and
toolchain inventory, and fresh patch-metadata fixtures. Remaining maintenance
work is limited to the final Android extension failure paths and release-time
repeated/output-signing checks; device journeys and performance remain
intentionally skipped.

zStyle is excluded. Video Original quality is also excluded: the pinned APK
contains `VIDEO` and `VIDEO_HD`, but no `VIDEO_ORIGINAL` path.

## Zalo 26.08 requested features

- Username friend search and additional logged-in devices were removed from the
  shipped patch set. Keep these as investigation-only backlog items until a
  compatible fingerprint and independent runtime/server evidence exist.
- Keep muted-chat count and asymmetric online/seen privacy classified as
  server-dependent unless runtime evidence identifies a client-side gate. The
  outbound seen-acknowledgement investigation below does not establish asymmetric
  privacy or control online presence.

## Photo Original quality — remaining validation

- Confirm that the current patch does not affect video sending.
- Complete stock/control/patch testing with identical source photos and compare
  source and received hashes, dimensions, metadata, and encoding.
- Verify ordinary quality selection, single-photo, multi-image, video-only, and
  mixed selections with explicit received-byte/hash assertions.
- Do not claim recovery of originals discarded by the client or server.

## Validation still outstanding

The repository safeguards and unit tests are implemented, but they do not prove
real-APK behavior. The analysis workspace contains the pinned base APK and all
recorded splits, but no current release-validation record. Before release,
complete and record the following against an unmodified control and the
selected-patch build:

- Run the documented cold-launch, background/resume, provider-cancellation,
  account-refresh, notification, and Threads feed journeys on the pinned APK.
- Compare original and patched manifests for exported components, permissions,
  provider authorities, URI grants, and package visibility; investigate every
  unexplained security-relevant difference.
- Run pinned-APK qualification, including base/split metadata, signing
  certificate, arm64 native-library, and unsupported-ABI checks.
- Complete stock/control/patch performance baselines for cold launch and feed
  scrolling; record startup, frame timing, memory, background activity, and
  variance.
- Keep all results PASS, FAIL, or BLOCKED with hashes, device/Android version,
  enabled patches, and sanitized evidence outside Git.

### Next device-validation batch — blocked pending device

The pinned APKM is available at the Windows Downloads path supplied during
validation and matches SHA-256
`b5deaaef517d1ab666cfe6b1d5280969738e054d4765a72d0bafede2a8aa6e88`.
Local qualification passed package, version, stock certificate, arm64 ABI,
native-library, and unsupported-ABI checks. `adb devices` currently reports no
connected device, so the following batch remains unexecuted.

Run this against the APKM represented by `analysis/zalo/26.08.01/apk/`, its
minimally re-signed no-patch control, and the selected-patch build:

1. Qualify split installation, manifest/package metadata, signing certificate,
   arm64 native libraries, cold launch, background/resume, force-stop, reboot,
   login, notifications, messaging, attachments, camera, and one-to-one/group
   calls.
2. Validate Original photo and media patches with a second account, including
   source/received hashes, metadata, single/multi-photo, video-only, expired
   local media, missing files, and deleted messages.
3. Validate seen and typing suppression with delivery acknowledgements,
   reconnect/retry, queued messages, and unchanged incoming rendering.
4. Validate ads and promotional notifications without suppressing organic feed,
   chat, group activity, friend requests, calls, or alerts.
5. Re-run the microG provider/Drive evidence, then qualify the media-age-limit
   patch and failure cases: offline, process death, low storage, wrong account,
   duplicate restore, and missing Drive objects.
6. Measure stock, control, and patched cold-launch/feed performance with at
   least three repetitions and record variance.

Do not treat unimplemented roadmap candidates (catalog, username changes, gold
badge entitlement, local export, inactivity prevention, or backup scheduling)
as expected device behavior.

## Feature feasibility investigations

Before implementing any item, record the exact smali gate, callers, local data
flow, server dependencies, narrow proposed change, and regression risks in
`analysis/zalo/26.08.01/notes/`. Classify each result as **ready to implement**,
**needs runtime proof**, or **server-dependent**. Do not globally spoof a paid
account or mutate HTTP traffic.

### zBusiness product catalog

- Trace entry points, product creation/editing, count limits, storage, sync, and
  sharing.
- Determine whether local templates are usable without an authorized backend.
- Verify persistence after restart and what recipients see when a product is
  shared.

### Local backup/export

- Trace phone-transfer and local export machinery, including messages, media
  associations, database snapshots/WAL, schema, and encryption keys.
- If an extension is needed, export only to a user-selected external location.
  Test unauthorized access, unexpected destinations, and overly broad URI grants;
  apply the same access-boundary checks to notification-history and recording
  exports below.
- Prove stock-to-patched migration, patched reinstall, and cross-device restore
  separately.
- Require integrity/version checks, bounded extraction, recoverable staging, and
  tests for corrupt archives, low space, and interrupted transfers.

### Gold Business badge

- Identify the exact asset and entitlement/display path.
- Separate local cosmetic rendering from server-visible verification.
- Treat server-issued verification as server-dependent unless contrary evidence
  is established.

### Change username

- Distinguish display name, unique handle, and business contact link.
- Trace validation, cooldowns, persistence, update requests, and visibility from
  another account.
- Patch only proven local restrictions; a local alias is not a server rename.

### Inactivity deletion

- Verify the current policy and what counts as activity.
- Determine whether deletion is server-managed; do not silently generate account
  activity.
- If no client enforcement point exists, classify prevention as server-dependent.
  Optional reminders and backup are separate mitigations.

### Google Drive backup and restore — remaining work

- Validate **Remove media backup age limit** against an unmodified control.
  Confirm that the 365-day filter is removed during both backup and restore;
  existing Drive objects must still be indexed and downloadable.
- Re-run the recorded initial photo restore and complete backup/restore cycle
  with the release bundle, then test scheduling, Wi-Fi constraints, token
  refresh, upload completion, pagination, retention, and restore order.
- Account for every test photo: excluded by policy, absent from the Drive index,
  upload failure, download failure, missing message association, or restored.
- Compare first-login restore with manual restore, including checkpoints,
  retries, process death, offline recovery, low storage, duplicate work, and
  wrong-account restore.
- Preserve live data and the last usable backup after failure; never log tokens
  or backup contents. Keep videos, files, voice messages, and groups over 100
  members classified as explicit stock exclusions unless separately proven.
- Treat Google OAuth/provider authorization as a backend boundary, not a local
  unlock. zCloud is out of scope for this investigation.

## Remaining Zalo investigations

Only items without a completed implementation or sufficient runtime evidence
remain here. Use patch-time fingerprints and app-specific extension code; do not
add broad runtime plumbing or remote symbol catalogs.

### P1: Configurable native backup interval

- Trace `SERVER_CONFIG_SYNC_MESSAGE_INTERVAL_*` and investigate 1/3/6/12-hour
  scheduling while preserving native opt-in, authentication, network, and other
  backup guards.
- Verify completed backups and restore round trips, not merely timer execution;
  measure battery/network impact, wakeups, network use, and account switching.
- Repeat stock, minimally re-signed control, and selected-patch measurements on
  the same device before setting regression thresholds.

### P2: Inbox and navigation controls

- Re-hunt stable 26.08.01 anchors for chat/group/official-account/stranger
  categories and a configurable initial filter. Preserve datasets, unread counts,
  refresh, pagination, search, and unknown-category handling.
- Re-hunt the main-tab, Me, media-box, and inbox-banner surfaces before changing
  UI. Prove a container is promotional-only; preserve alerts and backup access.
  Prefer exact IDs and scoped resource or binding changes; test localization,
  accessibility, badges, deep links, and resume behavior.

### P2: Open ordinary content links externally

- Trace the exact 26.08.01 URL dispatch boundary. Limit external dispatch to
  validated HTTP(S) content links; keep mini-app, OA H5, authenticated, payment,
  OAuth, and non-web flows in their intended handlers.
- Test malformed URLs, absent browsers, cancellation, redirects, chat/feed links,
  login/payment journeys, unchanged deep links, and a safe in-app fallback.

### P2: Analytics and privacy candidates

- Trace the analytics Room DAO and upload workers before suppressing writes;
  avoid null DAOs and preserve application stability. Decide whether a separate
  opt-in analytics patch is justified.
- Evaluate a Zalo-scoped advertising-ID reduction only after confirming all app
  and SDK consumers. Preserve unrelated advertising and attribution behavior.
- Keep capture controls, notification history, and call recording as separate,
  default-off investigations requiring privacy, consent, storage, and second-
  account validation. Do not globally clear security flags.

### P3: Security and appearance

- Trace the passcode grace-period preference and lifecycle before considering a
  default-off option with a clear warning; preserve authentication and grace
  expiry behavior.
- Investigate a true-black dark theme using stable resource/runtime-color
  evidence. Validate light mode, contrast, dialogs, system bars, and switching.

### Boundaries

- Forced-update suppression remains investigation-only until its trigger chain is
  fully traced.
- The permission audit found no safe zero-usage removal candidates.
- Catalog, username changes, gold badge entitlement, local export, inactivity
  prevention, zCloud capacity, and business quotas remain unproven or
  server-dependent; do not treat them as local unlocks.
- HTTP-header mutation, global paid-account spoofing, broad MicroG rewrites,
  unrelated app ports, and generic shared runtime infrastructure remain out of
  scope.

## Additional investigation candidates

These candidates remain exploratory and are not verified compatible patches.
Apply the evidence and classification requirements above before implementing
any of them.

### P2: Content and playback controls

- Investigate content-WebView autoplay. Trace native Timeline/Video players
  separately; preserve tap-to-play, calls, and explicit media previews.
- Compare search keypress/focus events against existing telemetry coverage. Add
  only proven gaps and preserve suggestions and actual searches.
- Investigate whether Zalo has equivalent local playback controls and player
  support; preserve default speed, seeking, audio sync, and lifecycle behavior.
  Exclude calls and live playback initially.

### P3: Optional capture, bubbles, and theme controls

- Establish whether Zalo has capture restrictions and outbound capture events.
  Treat capture permission and notification suppression as separate behaviors;
  keep options default-off, warn about sensitive-content exposure, and verify
  remote effects with a second account. Do not globally clear security flags.
- Investigate existing native bubble support. Preserve Android API, permission,
  and resource requirements; do not force unsupported devices to report
  eligibility.
- Investigate resource and runtime-color approaches for a true-black theme,
  without relying on app-specific parameter positions or resource names. Scope
  changes to the existing dark theme and keep the option default-off. Validate
  light mode, contrast, dialogs, system bars, and theme switching.

### Boundaries

- HTTP-header mutation is not an acceptable Drive OAuth fix.
- Do not add global paid-account spoofing, broad MicroG rewrites, or shared
  helper frameworks without a concrete, independently verified need.
- Pairip, native, and Hermes infrastructure require a demonstrated consumer;
  unrelated app ports remain outside this roadmap.
- Signing-identity qualification and other engineering ideas belong in the
  [repository maintenance plan](maintenance.md#remaining-repository-maintenance).

## Deferred validation

These are release checks for implemented patches, not pending implementation
requests.

- Validate the outbound seen-status patch with two accounts, including
  one-to-one/group chats, delivery acknowledgements, reconnect/retry, queued
  messages, Android/Web/Desktop visibility, and unchanged incoming rendering.
- Validate expired-media behavior in chat, including missing local files,
  unusable remote URLs, restore, and deleted messages. Confirm whether the
  existing local expiry patch changes only presentation or also affects access.
- Validate the existing `SOCIAL_STORY` / `ZALO_VIDEO` notification filter without
  suppressing chat, group activity, friend requests, calls, or alerts.
- Validate QR login, read/delivery state, message history, media scope, and
  session revocation across Android, Web, and Desktop.
- Update the MicroG-RE source only after a stable upstream release or official
  project page provides the OAuth SHA-1 normalization fix; verify provenance and
  checksum first.

## Acceptance evidence

For every implemented candidate, record a positive behavior check and an
unmodified/control comparison. Include remote visibility or a complete restore
round trip where relevant. Before release, repeat build, original-APKM repatch,
installation, and device validation with the published `.mpp`; record the APK
hash, patch bundle hash, enabled patches, device, Android version, and signing
certificate fingerprint outside this repository.
