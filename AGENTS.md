# Agent Instructions

## Project Layout

| Path | Purpose |
| ---- | ------- |
| `patches/` | Patch + fingerprint sources; builds `patches/build/libs/patches-*.mpp` |
| `extensions/extension/` | Companion extension source; builds `extensions/extension.mpe` |
| `scripts/` | Helper scripts (APK recon, re-patch, release staging, cleanup) |
| `docs/` | Contributor docs (setup, development, release, QA) |
| `config/` | Static-analysis config (`detekt.yml`) |
| `analysis/` | Gitignored local APK analysis scratch |

## Commands

| Task | Command |
| ---- | ------- |
| Verify | `uvx pre-commit run --all-files --show-diff-on-failure`, then `./gradlew qualityCheck :patches:test :extensions:extension:testDebugUnitTest buildAndroid --no-daemon` |
| Test single class | `./gradlew :patches:test --tests "<class>" --no-daemon` (extension: `:extensions:extension:testDebugUnitTest --tests "<class>"`) |
| Build patches bundle | `./gradlew buildAndroid` → `patches/build/libs/patches-*.mpp` |
| Re-patch + sign an APK | `bash scripts/repatch.sh <app.apkm> [out.apk]` (env overrides: see `docs/cli.md`) |
| Stage a release | `bash scripts/prepare-release.sh <X.Y.Z>` on `main`, then push tag (see `docs/release.md`) |

## Key Conventions

- Work on a branch; open a PR to `main` and merge (no squash) — see `docs/release.md`.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `README.md` patch list, `gradle.properties` version.
- `CHANGELOG.md`: user-visible app changes only, bullets under `## Unreleased` only; never touch versioned entries.
- Never create releases or `.mpp` uploads by hand; push the version tag only via release staging and let `release.yml` publish.
- Risky patches (login/providers/push at risk) ship `default = false` with a WARNING; see `docs/patch-development.md`.

## External References

| Need | File |
| ---- | ---- |
| Development entry + reading order | `docs/development.md` |
| Host setup | `docs/toolchain.md` |
| CLI patching + signing | `docs/cli.md` |
| Writing patches + fingerprints | `docs/patch-development.md`, `docs/fingerprint-guide.md` |
| Release process | `docs/release.md` |
| QA + remaining work | `docs/qa-checklist.md`, `docs/plan.md` |
