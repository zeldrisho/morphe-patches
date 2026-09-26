# Development guide

## Reading order

1. [Toolchain setup](toolchain.md) — provision a host and registry credentials.
2. [CLI patching](cli.md) — patch, sign, and install an APK.
3. [Reverse engineering](reverse-engineering.md) and [analysis workspace](analysis.md) — find targets and organize local evidence.
4. [Patch development](patch-development.md) — file layout, fingerprints, and the build/test loop.
5. [Validation](validation.md) — real-APK and device checks.
6. [Release process](release.md) — branching, changelog, generated-file ownership, and publishing.

## Repository structure

| Location | Responsibility |
| --- | --- |
| `patches/src/main/kotlin/com/zeldrisho/patches/` | App patches and adjacent fingerprints; reusable helpers in `shared/` |
| `extensions/threads/`, `extensions/zalo/` | Independent, app-specific runtime extensions |
| `patches/build/libs/patches-*.mpp` | Built bundle, including extension artifacts |

Injected extension descriptors are compatibility interfaces: renames must update
both call sites and `patches/build.gradle.kts` wiring. Each app must call only its
matching extension. `extendWith(...)` loads artifacts through the bundle classloader.

```text
original split APK → jadx/apktool → fingerprint + patch → .mpp → Morphe → patched APK → device validation
```

## Verify

Run from the repository root with the [configured toolchain](toolchain.md):

```bash
uvx pre-commit run --all-files --show-diff-on-failure
python3 -m unittest discover -s scripts/tests -v
./gradlew verify --no-daemon
```

`verify` runs quality checks, patch tests, both extension unit-test suites, and
`:patches:verifyBundleExtension` (builds the `.mpp` and checks embedded artifacts).
`:patches:checkExtensionArtifact` is the faster artifact pre-check.
`buildAndroid` alone does **not** run the quality gate. `coverageReport` generates
JaCoCo output for `:patches` and AGP coverage reports for both extensions;
`coverageVerification` (included in `verify`) enforces line-coverage floors of
80% for patches, 91% for Threads, and 45.5% for Zalo. Current measured baselines
are 80.3%, 91.9%, and 46.2%, respectively. Reports are written beneath each
module's `build/reports/` directory.
Successful verification still requires [real-APK and device validation](validation.md).

## Code quality

CI runs the same check-only commands above; hooks are optional. Generated metadata,
build output, and local APK analysis are not formatting targets.

| Check | Configuration / scope |
| --- | --- |
| Spotless: ktlint + google-java-format | Root `build.gradle.kts`; Kotlin, Gradle scripts, extension Java |
| detekt | `patches/build.gradle.kts`, `config/detekt/detekt.yml`; Kotlin, without type resolution |
| Android Lint | Extension production and test sources |
| Ruff check + format | `.pre-commit-config.yaml`; `scripts/*.py` |
| actionlint | `.pre-commit-config.yaml`; workflows; ShellCheck when on PATH (explicitly installed in CI) |
| Conflict markers + mixed line endings | `.pre-commit-config.yaml`; tracked text files |

`qualityCheck` aggregates Spotless, detekt, and Android Lint. Reports live in
`patches/build/reports/detekt/` and `extensions/*/build/reports/`.
Tool versions are pinned in Gradle, hook revisions, and CI. Detekt **2.0.0-alpha.6**
is intentional: its compiler matches Morphe's Kotlin **2.4.10**, unlike 1.23.8.
Check [compatibility](https://detekt.dev/docs/introduction/compatibility/) when
upgrading. Use documented rule exceptions, not a baseline of ignored findings.

### Optional commit hooks

```bash
uvx pre-commit install
uvx pre-commit uninstall # removes only the pre-commit-managed hook
```

The first run needs network access for isolated environments (including Go for
actionlint). Commits do not run Gradle or SDK builds.

### Apply formatting explicitly

```bash
./gradlew spotlessApply --no-daemon
# Python checks only; these do not rewrite files:
uvx ruff check scripts && uvx ruff format --check scripts
```

Review the diff and rerun verification. Fix non-autoformattable naming/KDoc errors manually.

## Operating rules

- Pin exact target versions and tested version codes; verify fingerprints against smali.
- Establish a stock control; record artifact hashes, options, signing certificate, and bounded logs.
- Keep risky patches disabled until device validation proves the default path.
- Keep credentials, keys, APK analysis, logs, and screenshots out of Git.
- Follow [release rules](release.md#rules) and [changelog policy](release.md#changelog-policy); do not hand-edit generated release metadata.
