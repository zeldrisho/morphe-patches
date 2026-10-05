# Agent Instructions

## Toolchain
- Use the checked-in Gradle wrapper (`./gradlew`) with Java 21.
- Use `uvx` for on-demand Python tools.
- Use `adb` for device operations.

## Commands
| Task | Command |
| ---- | ------- |
| Test Python script file | `python3 -m unittest discover -s scripts/tests -p 'test_<script>.py' -v` |
| Test patch class | `./gradlew :patches:test --tests '<fully.qualified.Class>' --no-daemon` |
| Test Threads extension class | `./gradlew :extensions:threads:testDebugUnitTest --tests '<fully.qualified.Class>' --no-daemon` |
| Test Zalo extension class | `./gradlew :extensions:zalo:testDebugUnitTest --tests '<fully.qualified.Class>' --no-daemon` |
| Build bundle and verify embedded extensions | `./gradlew :patches:verifyBundleExtension --no-daemon` |
| Check Morphe patch flags | `morphe patch --help` |
| Full verification | Follow `docs/development.md#verify` |
| Locate downloaded APKM | `fd -t f -e apkm . /mnt/c/Users/zeldrisho/Downloads` |
| Re-patch and sign | `python3 scripts/repatch.py <app.apkm> [out.apk]` (options: `docs/cli.md`) |
| Extract APK/APKM smali | `python3 scripts/extract_smali.py <apk-or-bundle> [output]` |

## Key Conventions
- Patch sources and adjacent fingerprints live under `patches/src/main/kotlin/com/zeldrisho/patches/`; app-agnostic helpers belong in `patches/src/main/kotlin/com/zeldrisho/patches/shared/`.
- Keep runtime extensions app-specific: `extensions/threads/` and `extensions/zalo/` are independent modules.
- Extension artifact or class-descriptor renames must update both Gradle wiring in `patches/build.gradle.kts` and injected bytecode call sites.
- Follow `docs/patch-development.md#file-layout` for exact compatibility targets, patch descriptions, and risky-patch defaults.
- Follow `docs/release.md#rules` for generated-file ownership; do not hand-edit release metadata or the generated README patch list.
- Follow `docs/release.md#changelog-policy` for `CHANGELOG.md`; add only user-visible app changes under `## Unreleased`.
- Follow `docs/release.md` for staging and publishing.
- Follow `docs/development.md#testing-guidance` for synthetic DEX tests, Robolectric tests, and opt-in private APK qualification.
- Locate downloaded APKMs under `/mnt/c/Users/zeldrisho/Downloads` before looking elsewhere; then inspect the existing gitignored `analysis/<app>/<version>/` workspace and notes, reuse verified evidence, and avoid redundant extraction. Follow `docs/reverse-engineering.md#analysis-workspace`.
- Build and unit-test success does not establish real-APK compatibility or device behavior; use `docs/validation.md`.
- Keep credentials, signing keys, APK analysis, logs, and screenshots out of Git.

## External References
| Need | File |
| ---- | ---- |
| Host setup, Morphe, and DEX tools | `docs/toolchain.md` |
| Morphe CLI workflow and flags | `docs/cli.md` |
| APK analysis, workspace layout, and cleanup | `docs/reverse-engineering.md` |
| Bytecode and smali reference | `docs/bytecode-reference.md` |
| Fingerprints, bypass patterns, and target selection | `docs/patch-development.md#fingerprints`, `docs/patch-development.md#target-selection` |
| Patch scope and code provenance | `docs/patch-development.md#provenance-and-scope` |
