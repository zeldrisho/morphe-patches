# Zalo microG Drive support

This note explains the Zalo 26.08.01 `microG Drive support` patch and the
known installation-detection report. It documents a client-side compatibility
fix; it does not guarantee Google OAuth or Drive authorization.

## What the patch changes

For Zalo `26.08.01` (version code `260801903`), the patch:

- uses the `app.revanced` account type expected by MicroG-RE;
- routes account selection through Android `AccountManager`;
- refreshes Zalo's Drive account state after the picker returns;
- supplies Zalo's stock certificate metadata to the provider;
- checks for the provider before Drive account operations; and
- declares `app.revanced.android.gms` in the manifest's `<queries>` package list.

The last item is important on Android versions that restrict package visibility:
being installed does not guarantee that an app can discover another package with
`PackageManager`. The earlier patch checked the provider package but did not
explicitly request visibility, which could produce a false “MicroG required”
prompt even when MicroG-RE was installed.

The prompt's **Install** action now opens the upstream MorpheApp repository:
[MorpheApp/MicroG-RE releases](https://github.com/MorpheApp/MicroG-RE/releases),
not a project fork.

## Issue #11: installed MicroG-RE was not recognized

Repository report: [zeldrisho/morphe-patches#11](https://github.com/zeldrisho/morphe-patches/issues/11).
The report used Zalo `26.08.01`, patch bundle `1.3.0`, and the Drive backup
account flow. The reporter had MicroG-RE installed, but Zalo still displayed
the installation prompt and the button opened the wrong repository. A later
comment reported that the suggested nightly did not fix the problem; the owner
then suggested testing the latest patch with a MicroG-RE logout, uninstall, and
nightly reinstall.

The report did not include a version code, Morphe version, logs, provider package
listing, or a `dumpsys package` result. Therefore it did not prove whether the
failure was package visibility, a disabled/mismatched provider package, or a
provider-side authorization failure. The patch now addresses the identifiable
local visibility and download-link defects, but issue #11 should only be closed
after device testing confirms the complete flow.

## Installation checklist

1. Use a current Zalo 26.08.01 patch bundle and apply **microG Drive support**.
2. Install a compatible MicroG-RE build from the upstream repository. The
   provider package must be exactly `app.revanced.android.gms`; a different
   MicroG fork or package name is not equivalent.
3. Confirm Android settings show the provider enabled. Do not disable its
   account, background, or notification components during testing.
4. If replacing an old provider, first record the account state and understand
   that uninstalling it can remove provider-local data. Reinstall only after
   exporting or otherwise accepting that risk.
5. Open Zalo, enter the text-message backup/restore screen, and select the
   account through the picker.
6. Test both a backup and a restore. A provider prompt, picker success, or
   account name alone is not proof that Drive upload/download and message-media
   association work.

For a useful report, include the Zalo version code, patch bundle, Morphe
version, MicroG-RE version, Android version/ABI, provider package name, and
redacted logcat around the failed operation. Never include OAuth tokens,
account contents, private APKs, or signing keys.

## Failure classification

| Symptom | Likely boundary | Meaning |
| --- | --- | --- |
| Install prompt while provider is installed | Local package discovery, visibility, disabled package, or wrong package name | Check `app.revanced.android.gms`, provider enabled state, and the patched manifest. |
| Picker opens but no account is listed | AccountManager/provider account registration | Recheck the provider account and account type; this is not proof of Drive authorization. |
| Account is selected but backup/restore is rejected | Google OAuth/provider backend | Treat as provider/server-dependent; do not spoof paid state or mutate HTTP traffic. |
| Existing media is missing after restore | Drive index, retention, download, or Zalo association | Test each stage separately; the patch cannot recover media absent from Drive or discarded upstream. |
| Zalo crashes after package renaming | Package/certificate-bound login or provider state | See [issue #14](https://github.com/zeldrisho/morphe-patches/issues/14); test stock-package Zalo first. |

## Scope and evidence

The patch is pinned to Zalo 26.08.01. Existing device validation covered an
initial photo restore and a complete backup/restore cycle, but those results do
not establish compatibility with every MicroG-RE release, Android version,
provider account, video/file backup, or renamed package. The upstream MicroG-RE
repository currently publishes its own release and issue history; provider
regressions must be checked there rather than treated as Zalo patch failures.

Related upstream references:

- [MicroG-RE latest releases](https://github.com/MorpheApp/MicroG-RE/releases)
- [MicroG-RE issue tracker](https://github.com/MorpheApp/MicroG-RE/issues)
- [MicroG-RE issue #276: already-backed-up photos not showing](https://github.com/MorpheApp/MicroG-RE/issues/276)

Issue #276 illustrates why a successful provider installation is not the same
as a complete restore result: indexing and existing-object visibility remain
separate provider/Drive behaviors.
