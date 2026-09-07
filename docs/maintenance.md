# Maintenance — durable project decisions

Distilled from the Threads fix (issue #5) and the 2026-09-07 tech-debt pass.
Nothing feature-specific lives here — this is how the repo is operated.

## Release home is this repo
- Ship from `zeldrisho/morphe-patches` (`dev` → release → backmerge, see
  `docs/release.md`). `scripts/repatch.sh` defaults `GITHUB_REPO` here.
- Don't split work across sibling patch repos; porting patches between repos
  duplicates fingerprint maintenance with no benefit.

## The extension `.mpe` is a build artifact, not a source file
- `extendWith("extensions/extension.mpe")` resolves relative to the patch
  working dir (repo root), but the extension module only emits
  `extensions/extension/build/morphe/extensions/extension.mpe`.
- `:patches:copyExtensionMpe` bridges the gap automatically and
  `:patches:verifyExtensionMpe` fail-fasts when the dex is missing
  (`extendWith` is a load-time reference, so the bundle builds fine without
  it — the failure would otherwise surface on-device). `buildAndroid`
  depends on both; CI runs the verify step explicitly. The repo-root copy
  stays git-ignored.
- Never commit `extensions/extension.mpe` or `analysis/` (see
  `scripts/clean-analysis.sh`).

## Risky patches default off
- A patch that can break core flows (login, providers, push) ships
  `default = false` and earns opt-in status with a WARNING in its description.
  Precedent: Change package name (renaming breaks package+cert-bound SSO).
- `PatchesListShapeTest` guards the default, so a regression fails CI.

## Version drift is routine, not an incident
- Pin exact `AppTarget` versions plus the tested `versionCode`
  (`Constants.TESTED_VERSION_CODE`) — same version *name* from different
  mirrors can carry different version *codes* (APKMirror vs APKPure).
- `FingerprintSurfaceTest` pins the obfuscated surface so the next app update
  fails loudly with a re-hunt pointer instead of silently dead patches.
- Per-update routine lives in `docs/qa-checklist.md` §4 (recon → re-hunt →
  device pass → update pin). Worst case for a missed drift is ads returning,
  never a broken feed — keep it that way (reflection wrapped, no-ops on mismatch).
