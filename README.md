# 🧩 zeldrisho Patches

Patches for apps I like, built for [Morphe](https://morphe.software).

## ❓ About

Personal Morphe patch bundle maintained by [@zeldrisho](https://github.com/zeldrisho).
Patches, compatible app versions, and options are listed below; the list is
regenerated on every release.

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=zeldrisho/morphe-patches

Or add the source URL manually in Morphe Manager → Sources.

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->

<!-- Do not modify this section by hand. The patch list is generated when release.yml creates a new release.

     If you wish for the patches list to be collapsed, then remove the word 'EXPANDED' from the comment tag above.

     If you wish to manually keep this list updated then remove the PATCHES_START and PATCHES_END
     comment blocks entirely. -->

#### A list of your patches will automatically be shown here after your first patches release is created.

&nbsp;

## 🧑‍💻 Dev usage

- **Make all changes on the `dev` branch.**
- Build locally with `./gradlew buildAndroid`; the `.mpp` lands in `patches/build/libs/patches-*.mpp`. Apply it with [Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) like any released bundle.
- Use semantic commits: `feat:` (new feature), `fix:` (bug fix), `chore:` (no release). See `docs/release.md`.
- Merge (do not squash) `dev` into `main` for a stable release. Never create releases by hand — `release.yml` handles versioning, assets, and generated files.
- Never hand-edit `patches-list.json`, `patches-bundle.json`, `CHANGELOG.md`, or the patch list above.
- See `docs/development.md` for setup and `docs/architecture.md` for patch structure. Patcher API details: [patcher documentation](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/1_patcher_intro.md).

<!-- The patches end tag is intentionally placed here so the first release will clean up
     this readme of all developer instructions above. -->
<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

zeldrisho Patches are licensed under the [GNU General Public License v3.0](LICENSE)
