# Agent Instructions

## Package Manager

- Gradle via wrapper: `./gradlew` (Java 21, see `.github/workflows/release.yml`).
- JS tooling via `vp`: `vp install`.

## Project Layout

| Path | Purpose |
| ---- | ------- |
| `patches/src/main/kotlin/` | Patch + fingerprint sources (`app/template/patches/`, `util/PatchListGenerator.kt`) |
| `extensions/extension/` | Companion extension source, outputs `extensions/extension.mpe` |
| `docs/` | Contributor guides |
| `.github/` | `workflows/release.yml`, `workflows/open_pull_request.yml`, `scripts/generate_patches_readme.py` |

## Commands

| Task | Command |
| ---- | ------- |
| Build patches bundle | `./gradlew buildAndroid` → `patches/build/libs/patches-*.mpp` |
| Regenerate patch list | `./gradlew generatePatchesList` → `patches-list.json` |
| Verify without release | `./gradlew :patches:buildAndroid clean --no-daemon` |
| Install release tooling | `vp install` |

## Key Conventions

- Work on `dev`; merge (no squash) `dev` → `main` for stable releases.
- Commits: `feat:` (minor), `fix:` (patch), `chore:` (no release); see `docs/release.md`.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, `README.md` patch list, `gradle.properties` version.
- Never create releases by hand; `release.yml` + `.releaserc` own versioning, assets, backmerge.
- Rename template defaults: `group` + `about` in `patches/build.gradle.kts`, `app.template.*` packages, `Compatibility` entries in `patches/src/main/kotlin/app/template/patches/shared/Constants.kt`.
- Project name must not imply Morphe authorship; see `NOTICE`.

## External References

| Need | File |
| ---- | ---- |
| User setup / patch list | `README.md` |
| Development setup | `docs/development.md` |
| Patch/extension structure | `docs/architecture.md` |
| Release process | `docs/release.md` |
| Project name restriction | `NOTICE` |
| Release pipeline config | `.releaserc` |
| Patcher API / fingerprinting | `https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md` |
