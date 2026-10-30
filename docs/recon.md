# APK inspection

These optional local tools support patch development; Gradle and Morphe remain the build and patching tools. Keep each target in the ignored `analysis/<package>/<version>/` workspace: original APKs in `apk/`, disassembly in `smali/`, evidence in `notes/`, and diagnostic outputs in `runs/`. Preserve existing evidence, and keep signing keys private. Do not put account information or tokens in evidence.

Copy original downloads into `apk/` without overwriting an existing input; keep the original download intact and verify its SHA-256. Perform subsequent inspection and patching using the workspace copy rather than the Downloads directory.

Prerequisites: Python 3, `fd`, `aapt` from Android SDK Build-Tools on `PATH`, and `baksmali` on `PATH` for disassembly. APK protection detection optionally uses `apkid` when installed; otherwise the report records it as unavailable.

```bash
# Locate existing inputs before adding another copy.
fd --hidden --no-ignore -t f -e apkm . analysis/

# Inspect package identity, splits, DEX files and native libraries.
python3 scripts/apk_recon.py analysis/<package>/<version>/apk/app.apkm analysis/<package>/<version>/notes/recon.md

# Disassemble all DEX files in the APK or split bundle.
python3 scripts/extract_smali.py analysis/<package>/<version>/apk/app.apkm analysis/<package>/<version>/smali

# Summarize candidate signals; use --files to show matching paths.
python3 scripts/hunt_signals.py analysis/<package>/<version>/smali --files
```

`extract_smali.py` replaces an existing output directory **after** successful extraction. Choose the output path carefully and preserve notes separately. If omitted, it infers `analysis/<package>/<version>/smali` only for recognizable APKMirror filenames.

For decoded text manifests, compare security-relevant components, permissions and queries:

```bash
python3 scripts/manifest_security_diff.py /private/original/AndroidManifest.xml /private/patched/AndroidManifest.xml
```

Triage output identifies candidates, not verified patch targets. Verify exact method signatures and instructions in smali, use the [target evidence template](template.md), and follow [testing](testing.md) before claiming compatibility. For instruction semantics, use the [Android bytecode specification](https://source.android.com/docs/core/runtime/dalvik-bytecode) and [Morphe fingerprint documentation](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_2_1_fingerprinting.md).
