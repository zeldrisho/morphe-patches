# Development guide

Developer entry point. Start here, then follow the reading order below.
For environment setup see [toolchain setup](toolchain.md).

## Reading order

1. [Toolchain setup](toolchain.md) — install once per host.
2. [CLI patching](cli.md) — terminal flows (Desktop JAR flags, `repatch.sh`, signing).
3. [Architecture](architecture.md) — module and data-flow overview.
4. [Reverse engineering workflow](reverse-engineering.md) — finding targets.
5. [Fingerprint guide](fingerprint-guide.md) — writing fingerprints.
6. [Patch development](patch-development.md) — writing, building, and testing patches.
7. [QA checklist](qa-checklist.md) — per-release and per-update device procedure.
8. [Release process](release.md) — branching, versioning, and publishing.
9. [Maintenance](maintenance.md) — durable decisions index.
10. [Lessons learned](lessons-learned.md) — incident context.

## Prerequisites

All tools, SDK packages, Vite+, GitHub Packages credentials, and Morphe Desktop
come from [toolchain setup](toolchain.md). Original APKs/APKMs come only from
[APKMirror](https://www.apkmirror.com/).

## Repo state

Template init is complete; this repo is already renamed to `com.zeldrisho.threads`.
Only re-scaffold from the upstream template when starting a new bundle repo.

## Adding a patch

Covered in [patch development](patch-development.md) (file layout, patch types,
build/test loop, troubleshooting) — nothing patch-specific lives here by design.

## Verify

Canonical local verification (bash):

```bash
./gradlew :patches:test :extensions:extension:testDebugUnitTest buildAndroid --no-daemon
```

The `.mpp` lands in `patches/build/libs/patches-*.mpp`. This only proves the
toolchain works — for the real loop (apply in Morphe Desktop, single-patch
isolation, troubleshooting) see [patch development](patch-development.md#build-and-test).
Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, or the
`gradle.properties` version — the release pipeline owns them (see [release process](release.md)).
