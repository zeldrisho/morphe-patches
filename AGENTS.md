# Agent Instructions

## Tools

- Gradle via wrapper: `./gradlew` (Java 21, see `docs/toolchain.md`).
- JS tooling via `vp`: `vp install` (see `docs/toolchain.md`).
- Android SDK, Morphe Desktop CLI, and host tools: see `docs/toolchain.md`.

## Project Layout

| Path | Purpose |
| ---- | ------- |
| `patches/` | Patch + fingerprint sources; builds `patches/build/libs/patches-*.mpp` |
| `extensions/extension/` | Companion extension source; builds `extensions/extension.mpe` |
| `scripts/` | Helper scripts (APK recon, re-patch, cleanup) |

## Commands

| Task | Command |
| ---- | ------- |
| Build patches bundle | `./gradlew buildAndroid` → `patches/build/libs/patches-*.mpp` |
| Regenerate patch list | `./gradlew generatePatchesList` → `patches-list.json` |
| Verify without release | `./gradlew :patches:test :extensions:extension:testDebugUnitTest buildAndroid --no-daemon` |
| Re-patch + sign an APK | `bash scripts/repatch.sh <app.apkm> [out.apk]` (`APP_NAME`, `PACKAGE_NAME`, `MPP`/`GITHUB_REPO`, `KEYSTORE`, `MORPHE_CLI` env overrides) |

## Key Conventions

- Work on `dev`; merge (no squash) `dev` → `main` for stable releases.
- Commits: `feat:` (minor), `fix:` (patch), `chore:` (no release); see `docs/release.md`.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, `README.md` patch list, `gradle.properties` version.
- Never create releases by hand; `release.yml` + `.releaserc` own versioning, assets, backmerge.
- Risky patches (login/providers/push at risk) ship `default = false` with a WARNING; see `docs/patch-development.md`.

## External References

| Need | File |
| ---- | ---- |
| User setup / patch list | `README.md` |
| Host setup (Fedora WSL + macOS installs) | `docs/toolchain.md` |
| CLI patching (Desktop JAR flags, repatch flows, signing) | `docs/cli.md` |
| Development entry + reading order | `docs/development.md` |
| Patch/extension structure | `docs/architecture.md` |
| Finding targets (recon→hunt) | `docs/reverse-engineering.md` |
| Writing fingerprints | `docs/fingerprint-guide.md` |
| Writing/building patches | `docs/patch-development.md` |
| Incident-driven rules (signing, TV/ABI, unpatchable) | `docs/lessons-learned.md` |
| Smali, obfuscation, match debugging | `docs/bytecode-reference.md` |
| Bypass techniques per system | `docs/bypass-patterns.md` |
| Release process | `docs/release.md` |
| Remaining work | `docs/plan.md` |
| Per-release / per-update QA | `docs/qa-checklist.md` |
| Recurring maintenance / project decisions | `docs/maintenance.md` |
