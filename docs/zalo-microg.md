# Zalo microG Drive support

Client compatibility for Zalo **26.08.01** (`260801903`), not a guarantee of
Google OAuth or Drive authorization.

## What the patch changes

- Uses MicroG-RE's `app.revanced` account type through Android `AccountManager`.
- Refreshes Drive state after the picker and supplies stock certificate metadata.
- Checks the provider before Drive operations and declares
  `app.revanced.android.gms` in manifest `<queries>` for package visibility.
- Opens the [official Morphe MicroG download page](https://morphe.software/microg)
  from the installation prompt.

## Issue #11: installed MicroG-RE was not recognized

[Issue #11](https://github.com/zeldrisho/morphe-patches/issues/11) reported a false
installation prompt and wrong download link with Zalo 26.08.01 / bundle 1.3.0;
a suggested nightly did not resolve it. Missing logs, provider metadata, version
code, and Morphe version prevent a single-root-cause conclusion. Local visibility
and link defects are addressed; close only after complete device-flow confirmation.

## Installation checklist

1. Apply **microG Drive support** from a current bundle for the pinned Zalo target.
2. Install compatible upstream MicroG-RE with package exactly
   `app.revanced.android.gms`; other forks/package names are not equivalent.
   Keep provider account/background/notification components enabled.
3. Before replacing a provider, record account state and export or explicitly
   accept loss of provider-local data. Do not blindly uninstall.
4. In Zalo's text-message backup/restore screen, select an account and test both
   backup and restore, including media/message association. Picker success or an
   account label alone is insufficient.

Reports need Zalo version/code, bundle, Morphe, provider version/package, Android/ABI,
and bounded redacted logcat. Follow [validation evidence rules](validation.md);
never include tokens, account contents, private APKs, or keys.

## Failure classification

| Symptom | Boundary / next check |
| --- | --- |
| Install prompt despite installed provider | Package name, enabled state, visibility, patched manifest. |
| Picker has no account | AccountManager registration and `app.revanced` account type; not Drive authorization. |
| Selected account rejected | OAuth/provider backend; mark attestation failures BLOCKED. No paid-state spoofing or HTTP mutation. |
| Missing restored media | Separately test retention, index, download, and message association; absent/discarded media cannot be recovered by this patch. |
| Crash after package rename | Package/certificate-bound state; test stock package first ([issue #14](https://github.com/zeldrisho/morphe-patches/issues/14)). |

## Scope and evidence

Prior device evidence covered initial photo restore and a full backup/restore cycle,
not every provider release, Android version, account, media type, or renamed package.
Repeat with the release bundle. Provider regressions belong in the
[upstream tracker](https://github.com/MorpheApp/MicroG-RE/issues).
[Issue #276](https://github.com/MorpheApp/MicroG-RE/issues/276) concerns already-backed-up
photos not appearing: provider installation does not establish index/object visibility.
