# Reverse engineering workflow

How to go from an APK file to a working patch in this repo.
Companion doc: `fingerprint-guide.md` (writing the actual fingerprint + patch code).

## Pipeline

```
RECON → DECOMPILE → HUNT → WRITE → TEST
```

| Stage | Question | Output |
| ----- | -------- | ------ |
| Recon | What app is this? | Identity + protections + framework notes |
| Decompile | What does it do? | `decompiled/` (jadx Java) + `smali/` (baksmali) |
| Hunt | Where is the check? | Smali-verified target (class, method, instruction sequence) |
| Write | How to bypass it? | `Fingerprints.kt` + `*Patch.kt` under `patches/src/main/kotlin/app/template/patches/<app>/` |
| Test | Does it match? | `./gradlew buildAndroid`, then apply the `.mpp` in Morphe Desktop |

Keep per-app work outside this repo (e.g. a sibling `analysis/<app>/` folder with
`apk/`, `decompiled/`, `smali/`, `notes/`). Only the `.kt` patch sources live here.

## Tools

| Tool | Purpose | Install |
| ---- | ------- | ------- |
| `aapt` | Package, version, SDK levels from an APK | `apt install aapt` |
| `apkid` | Obfuscator / packer / anti-debug / anti-VM detection | `uvx apkid` |
| `jadx` | APK → Java source | `apt install jadx` |
| `baksmali` | DEX → smali bytecode | `apt install libsmali-java` |
| `apktool` | Decode / rebuild resources | `apt install apktool` |
| `rg` | Fast search over decompiled output | `apt install ripgrep` |
| `adb` | Install patched APK on device | `apt install adb` |

`scripts/apk-recon.sh` wraps the recon step; `scripts/extract-smali.sh` wraps the
DEX → smali step (including split `.apkm`/`.xapk` handling).

## 1. Recon

Run `scripts/apk-recon.sh <file.apk>` (or do it manually):

1. `aapt dump badging <apk>` — package, version, versionCode, SDK levels, label, launch activity.
2. `aapt dump xmltree <apk> AndroidManifest.xml | rg -i 'split|requiredSplit'` — split-APK detection.
   For `.apkm`/`.xapk`, extract `base.apk` to a temp dir first and run `aapt` on that.
3. `uvx apkid <apk>` — compiler, obfuscator, packer, anti-debug, anti-VM (per DEX / lib).
4. `unzip -l <apk> | rg '\.dex'` — DEX count.
5. `unzip -l <apk> | rg 'index.android.bundle|libflutter|libapp'` — framework:
   `index.android.bundle` = React Native, `libflutter.so`/`libapp.so` = Flutter, else native.
6. Record native-lib architectures and notable permissions (billing, internet, etc.).

Save as `analysis/<app>/notes/recon.md` (rename the APK to `<app>_<version>.<ext>`).

## 2. Decompile

```bash
jadx -d analysis/<app>/decompiled <apk>
scripts/extract-smali.sh analysis/<app>/apk/<app>_<version>.apk analysis/<app>/smali
```

Remote decompilation (large APKs — local jadx OOMs):

```bash
KAGGLE_API_TOKEN=... KAGGLE_KERNEL_ID=user/jadx-apk-decompiler \
  scripts/remote-decompile.sh "<direct-apk-url>" analysis/<app>/
cd analysis/<app> && unzip *_decompiled.zip -d decompiled/
```

Needs the `kaggle` CLI plus a private Kaggle notebook with internet access.
The URL must be a direct download link (mirror links expire in ~1 hour — use a fresh one).

Notes:

- If you have a big machine handy, local `jadx` there works too — the rest of the
  workflow only needs the files copied back.
- `"finished with errors"` from jadx is normal for obfuscated apps. Continue as long
  as `.java` files were produced.
- Always extract smali from **all** DEX files; the class you need is often in
  `classes2.dex` or later, not `classes.dex`.

## 3. Hunt (find targets)

Search in a fixed order — protections first, because an integrity/root check will
break testing of everything else:

1. **Protections** — integrity/license, signature verification, root, pinning:
   `pairip|PairIp|PlayIntegrity|IntegrityManager|processLicenseResponse`,
   `GET_SIGNATURES|checkSignature|verifySignature`,
   `isRooted|checkRoot|RootBeer|magisk|Superuser`,
   `CertificatePinner|TrustManager|checkServerTrusted|HostnameVerifier`
2. **Billing SDK detection** — tells you which pattern applies:
   `revenuecat|adapty|qonversion|superwall|BillingClient|LicenseChecker`
3. **SDK-specific search** — e.g. RevenueCat: `CustomerInfo|EntitlementInfos|getActive|getEntitlements`;
   Play Billing: `queryPurchases|isAcknowledged`; local fallback:
   `isPro|isPremium|isSubscribed|hasPremium`.
4. **Ads** — `showAd|loadAd|interstitial|MobileAds|AdRequest|UnityAds|AppLovin|IronSource`;
   per-SDK load/show/initialize methods (SDK class names are stable, app class names are not).
5. **Feature gates** — `RemoteConfig|getBoolean|featureFlag|isFeatureEnabled`.

Example:

```bash
rg 'revenuecat|adapty|BillingClient' analysis/<app>/decompiled -g '*.java' -l
rg 'isPro|isPremium|isSubscribed' analysis/<app>/decompiled -g '*.java' -l
```

### Smali verification (mandatory)

Never trust jadx output alone — it mis-decompiles obfuscated code. For every candidate:

1. Find the smali file across **all** DEX dirs: `find analysis/<app>/smali -name '<ClassName>.smali'`.
2. Read the exact method: `rg -B 2 -A 50 '\.method.*<methodName>' <file>`.
3. Record: access flags, return type, full parameter descriptors, register count,
   invoke sequence **in order**, and which DEX it came from.
4. If Java and smali disagree, **trust smali**.
5. Write the finding down (`analysis/<app>/notes/<topic>.md`) with the smali evidence
   quoted, plus a fingerprint strategy (which stable strings/calls to match on —
   see `fingerprint-guide.md`). Unverified findings are not ready for patch-writing.

## 4. Write

Covered in `fingerprint-guide.md` and `development.md`. The handoff from hunting is:

- Fully qualified class + exact smali method signature.
- Ordered instruction sequence (invoke calls / const-strings).
- Suggested bypass (`returnEarly(true)`, instruction override, etc.).

## 5. Test

```bash
./gradlew buildAndroid
```

Check the patch is registered (`list-patches` in Morphe Desktop/CLI against
`patches/build/libs/patches-*.mpp`), apply to the **original** APK (never an
extracted `base.apk`), install via `adb install -r`. If a fingerprint fails to
match, go back to the hunt step and re-verify smali — the app version probably
moved the code.
