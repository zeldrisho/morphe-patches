# Architecture

## Modules

| Module | Entry | Output |
| ------ | ----- | ------ |
| `patches` | `patches/build.gradle.kts`, `patches/src/main/kotlin/` | `patches/build/libs/patches-*.mpp` |
| `extensions/extension` | `extensions/extension/build.gradle.kts`, `extensions/extension/src/main/java/` | `extensions/extension.mpe`, embedded via `extendWith` |

Plugin `app.morphe.patches` (see `settings.gradle.kts`, `gradle/libs.versions.toml`) builds both.
`patchListGeneratorClasspath` keeps `gson` available to the generator without bundling it.

## Patch anatomy

- `example/ExamplePatch.kt`: user-visible `bytecodePatch` with `compatibleWith(...)`, `dependsOn(...)`, `extendWith(...)`, `execute { ... }`.
- `example/Fingerprints.kt`: named `Fingerprint` objects (class, method, access flags, return type, parameters, instruction filters).
- `example/InternalPatch.kt`: unnamed `bytecodePatch` — hidden from Manager/CLI, used via `dependsOn`.
- `shared/Constants.kt`: `Compatibility` records (package name, `ApkFileType`, icon color, `AppTarget` versions, optional `versionCodes` per `SupportedAbi`).

## Generated data flow

1. `./gradlew generatePatchesList` runs `util/PatchListGenerator.kt`, which loads the `.mpp` and writes `patches-list.json`.
2. Release (`exec` step in `.releaserc`) stamps `version`, then runs `.github/scripts/generate_patches_readme.py` to inject the patch table into `README.md` between `PATCHES_START` / `PATCHES_END`.
3. `patches-bundle.json` is written by the `@MorpheApp/changelog` plugin with the release download URL.

`patches-list.json` groups by `compatiblePackages[].packageName`; entries with null compatibility are universal.
