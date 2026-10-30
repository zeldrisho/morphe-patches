# Development

## Setup

Use JDK 21, an Android SDK with accepted licenses, and the checked-in Gradle wrapper. Set `ANDROID_HOME` to the SDK; Gradle can install missing SDK packages. See the [official Morphe setup guide](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md).

GitHub Packages requires a classic personal access token with `read:packages`. Put unquoted `gpr.user` and `gpr.key` values in `~/.gradle/gradle.properties`, or set `GITHUB_ACTOR` and `GITHUB_TOKEN`. Never put credentials in this repository.

Install [Vite+](https://viteplus.dev/) and the release dependencies:

```bash
vp install --frozen-lockfile
```

Vite+ selects pnpm from `pnpm-lock.yaml`; Gradle builds the patch bundle.

## Build and validate

```bash
./gradlew :patches:buildAndroid --no-daemon
morphe --help
morphe list-patches --patches patches/build/libs/patches-<version>.mpp --with-packages --with-versions
```

The bundle is written to `patches/build/libs/`. Follow [testing and qualification](testing.md) before claiming app compatibility. A successful build does not prove that patches apply or work on a device. See [architecture](architecture.md), [bytecode](bytecode.md), the [target evidence template](template.md), and [Zalo microG Drive support](microg.md) for patch development.

## Branches and releases

Develop on `dev` using semantic commits. See [releases](releasing.md) for branch conventions, generated files and publication.
