# Releases

Releases are managed by [semantic-release](../.releaserc) in the existing [release workflow](../.github/workflows/release.yml). Do not publish assets by hand, create replacement release scripts, move tags, or force-push release commits.

## Branches

- Develop on `dev` using semantic commits. `feat:` and `fix:` create prereleases; `chore:` does not. The configured release rules also cover `bump:` and `perf:`.
- When ready for a stable release, merge `dev` into `main` without squashing. Semantic-release publishes from `main` and backmerges to `dev`.
- Push `dev` to the remote before relying on prereleases, automated pull requests, or the stable-release backmerge. Enable “Allow GitHub Actions to create and approve pull requests” in repository Actions settings for the [automated PR workflow](../.github/workflows/open_pull_request.yml).

Before a release, build and apply the bundle to the intended app versions as described in [testing](testing.md). A successful CI build alone is not compatibility validation. The release workflow and first release after migration still require validation on GitHub. Migration commits marked only `chore:` do not trigger publication; use an appropriate release-triggering semantic commit when the migration is ready.

## Changelog policy

`CHANGELOG.md`, `patches-list.json`, `patches-bundle.json`, and the generated README patch-list section are release-owned. Preserve existing history; do not edit or commit these generated files manually. The release bot updates them from release commits and the built bundle.

The patch-list generator builds and selects the `.mpp` for the current Gradle project version. Do not delete or rewrite published releases to fix a release: make a new release instead.
