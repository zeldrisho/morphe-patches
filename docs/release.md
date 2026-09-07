# Release process

Canonical release authority. Branching, versioning, publishing, and release
recovery live here — other docs link here instead of restating them.
Branches are configured in `.releaserc`: `main` (stable) and `dev` (prerelease).

## Daily flow

- Commit to `dev` with semantic messages: `feat:` (minor), `fix:` (patch), `chore:` (no release). `bump:`, `perf:`, and `build(Needs bump):` are also configured — see `.releaserc`.
- Non-release branches and `chore:` commits only run the verify step in `.github/workflows/release.yml`.
- Enable `pre-release` in Morphe Manager sources to consume `dev` releases.
- When `dev` is stable, merge (no squash) `dev` → `main`. `.github/workflows/open_pull_request.yml` opens that PR automatically on pushes to `dev`.
- Ship from this repo (`dev` → release → backmerge). `scripts/repatch.sh` defaults `GITHUB_REPO` here. Don't split work across sibling patch repos; porting patches between repos duplicates fingerprint maintenance with no benefit.

## PR checks

`check.yml` runs on pull requests and manual dispatch, not pushes to `dev`.
The open `dev` → `main` PR therefore covers daily pushes without duplicate
push/PR check runs. New commits cancel superseded checks on the same PR.
Use **Actions → Check → Run workflow** for a branch without a PR.

The auto-PR workflow uses `GITHUB_TOKEN`. GitHub may require selecting
**Approve workflows to run** on a bot-created PR before checks start; see
[GitHub's workflow-trigger rules](https://docs.github.com/en/actions/how-tos/write-workflows/choose-when-workflows-run/trigger-a-workflow#triggering-a-workflow-from-a-workflow).
If no run appears, dispatch Check for `dev` manually. Release verification
remains separate in `release.yml`; it is not removed by this change.

## What `release.yml` does

1. `semantic-release` analyzes commits (`commit-analyzer`, `release-notes-generator`).
2. `@MorpheApp/changelog` writes `patches-bundle.json`; `gradle-semantic-release-plugin` builds the `.mpp` and bumps `gradle.properties`.
3. `exec.prepareCmd` runs `./gradlew generatePatchesList`, stamps `patches-list.json` version, regenerates the `README.md` patch table.
4. `git` plugin commits `CHANGELOG.md`, `gradle.properties`, `patches-bundle.json`, `patches-list.json`, `README.md` as `chore: Release v... [skip ci]`.
5. `github` plugin attaches `patches-*.mpp` to the release; `attest-build-provenance` attests it.
6. `backmerge` merges `main` back into `dev` with a clean workspace.

## Release recovery

- Before retrying a release that reports `tag already exists`, check whether
  the published tag is an ancestor of the release branch. Rewritten history
  can leave a tag unreachable and make semantic-release calculate an existing
  version again.
- A failed release can already have pushed its generated commit. Fetch and
  inspect remote state before retrying; do not delete/repoint published tags
  or force-push release history. Any ancestry repair needs explicit review.
- Verify publication directly; a green workflow does not by itself prove an
  artifact published. For a supplied CodeQL alert, inspect its specific ID and
  PR instance state rather than the default-branch alert listing.
- If fetch/prune or branch inspection times out, report cleanup as unverified.
  Do not infer merge status from stale refs or force-delete branches.

## Rules

- Never create releases, tags, or `.mpp` uploads by hand.
- Never force-push a release commit; ship a new release instead.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`,
  `README.md` patch list, or the `gradle.properties` version — the pipeline above owns them.
- Keep unrelated pending work out of commits touching shared generated files:
  restore the other subsystem to HEAD, regenerate, commit, then re-apply.
- The Manager serves the `.mpp` from the GitHub release named in `patches-bundle.json`
  — pushing source does nothing until a versioned release is cut.
- `gradlew` must stay tracked executable (`git update-index --chmod=+x gradlew`);
  `core.fileMode=false` checkouts can silently commit it non-executable (CI exit 126).
- Requires Settings → Actions → General → "Allow GitHub Actions to create and approve pull requests".
- `GITHUB_TOKEN` needs `contents:write`, `packages:write`, `id-token:write`, `attestations:write` (already set in `release.yml`).
