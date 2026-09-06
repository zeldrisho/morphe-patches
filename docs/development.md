# Development guide

Use this guide when adding patches or changing build configuration.
See `architecture.md` for module layout and `release.md` for the release pipeline.

## Prerequisites

- Java 21 (Temurin, per `.github/workflows/release.yml`).
- Node.js tooling via `vp install` (semantic-release deps in `package.json`).
- GitHub PAT with `read:packages` for the Morphe registry in
  `settings.gradle.kts` (`gpr.user` / `gpr.key` or `GITHUB_ACTOR` / `GITHUB_TOKEN`).
- Morphe Desktop for local `.mpp` testing.

## First-time init

Template leftovers still present — finish these before first real patch:

- `patches/build.gradle.kts`: `group` and `patches { about { ... } }`.
- `patches/src/main/kotlin/app/template/`: rename `app.template.*` packages.
- `patches/src/main/kotlin/app/template/patches/shared/Constants.kt`: real app targets.
- `extensions/extension/build.gradle.kts`: `android { namespace }`.
- `README.md`: title, About, add-source link, License holder.
- `.github/ISSUE_TEMPLATE/`: repo links.
- Optional `patches-bundle.png` for a custom Manager icon.

## Adding a patch

- Put shared targets in `patches/src/main/kotlin/app/template/patches/shared/Constants.kt`.
- Put each patch beside its fingerprints: `patches/src/main/kotlin/app/template/patches/<app>/`.
- Declare fingerprints as named objects (e.g. `Fingerprints.kt`) so match failures show a name.
- Keep internal-only helpers as `bytecodePatch { ... }` without `name` (see `example/InternalPatch.kt`).
- Put complex runtime logic in `extensions/extension/src/main/java/` and link with `extendWith("extensions/extension.mpe")`.
- Prefer exact, apkmirror/up-to-down-available `AppTarget` versions over `version = null`.

## Verify

```bash
./gradlew buildAndroid
./gradlew generatePatchesList
./gradlew :patches:buildAndroid clean --no-daemon
```

The `.mpp` lands in `patches/build/libs/patches-*.mpp`; apply it in Morphe Desktop like any released bundle.
Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, or the `gradle.properties` version — the release pipeline owns them (see `release.md`).
