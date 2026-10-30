# Agent Instructions

## Toolchain

- Use JDK 21, the checked-in Gradle wrapper, an Android SDK, and Vite+; see `docs/development.md`.
- Use `python3`, `fd`, `morphe`, `aapt`, `baksmali`, and `adb` for inspection and validation; see `docs/recon.md` and `docs/testing.md`.

## Commands

| Task | Command |
| --- | --- |
| Install release dependencies | `vp install --frozen-lockfile` |
| Build patch bundle | `./gradlew :patches:buildAndroid --no-daemon` |
| Find local APKM inputs | `fd --hidden --no-ignore -t f -e apkm . analysis/` |
| Inspect an APK or APKM | `python3 scripts/apk_recon.py <input> <report>` |
| Disassemble an APK or APKM | `python3 scripts/extract_smali.py <input> <output>` |

## Rules

- Validate patch changes with Morphe Desktop (`morphe --help`) against the intended app version; follow `docs/testing.md`. A successful build does not establish compatibility.
- Keep APK inputs, smali, notes, and diagnostic outputs under ignored `analysis/<package>/<version>/`; preserve existing evidence and never commit private artifacts.
- Before patching, ask which output package ID to use. Never install over a user-owned app, uninstall it, or clear its data without explicit approval.
- Put Kotlin patch definitions and fingerprints under `patches/src/main/kotlin/app/template/patches/`.
- Put app-specific runtime Java extension code under `extensions/extension/src/main/java/`; preserve injected class descriptors.
- Keep injected Dalvik class descriptors and method signatures consistent with the actual extension classes.
- Keep `extendWith(...)` paths aligned with the extension artifact name in `extensions/extension/build.gradle.kts`.
- Declare shared app compatibility and tested target versions in `patches/src/main/kotlin/app/template/patches/shared/Constants.kt`.
- Keep Gson off the bundled runtime classpath; `patches/build.gradle.kts` provides a separate configuration for the patch-list generator.
- Do not manually edit or commit generated `patches-list.json`, `patches-bundle.json`, or `CHANGELOG.md`.
- Do not hand-edit the generated patch-list section between `PATCHES_START` and `PATCHES_END` in `README.md`.
- Make development changes on `dev`; use the commit and merge conventions in `docs/releasing.md`.
- Use `.github/workflows/release.yml` and `.releaserc` for releases; do not create replacement release scripts or manually upload releases.
- Do not force-push semantic-release commits.

# References

| Need | File |
| --- | --- |
| Local build, development, and release conventions | `docs/development.md` |
| Patch and extension layout | `docs/architecture.md` |
| Release process and automation | `docs/releasing.md`, `.github/workflows/release.yml`, `.releaserc` |
| Dependency versions and plugin setup | `gradle/libs.versions.toml`, `settings.gradle.kts` |
| Zalo microG Drive support | `docs/microg.md` |
| Patcher API and examples | https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md |
| Validation and version updates | `docs/testing.md` |
| APK inspection and evidence | `docs/recon.md` |
