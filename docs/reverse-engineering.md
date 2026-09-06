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
| `apktool` | Decode / rebuild resources (rarely needed — prefer `bytecodePatch`) | `apt install apktool` |
| `rg` | Fast search over decompiled output | `apt install ripgrep` |
| `strings` | DEX string extraction (`apk-recon.sh` stack signals) | `apt install binutils` |
| `python3` | Kotlin name-recovery mapping (`recover-kotlin-names.sh`) | preinstalled |
| `kaggle` | Remote-decompile uploads (large APKs only) | `pipx install kaggle` |
| `adb` | Install patched APK on device | `apt install adb` |

`scripts/apk-recon.sh` wraps the recon step (Phase-0 triage: framework, HTTP/DI/billing
stack signals via DEX strings, obfuscation estimate, split-aware native libs, recommended
next step); `scripts/extract-smali.sh` wraps the
DEX → smali step (including split `.apkm`/`.xapk` handling);
`scripts/hunt-signals.sh <decompiled|smali>` counts protection/billing/ads/Ktor/Koin
signals in one pass before hunting; `scripts/recover-kotlin-names.sh <decompiled>`
rebuilds obfuscated → real Kotlin class names from `@DebugMetadata`/`@Metadata`.

## 1. Recon

Run `scripts/apk-recon.sh <file.apk>` (or do it manually):

1. `aapt dump badging <apk>` — package, version, versionCode, SDK levels, label, launch activity.
2. `aapt dump xmltree <apk> AndroidManifest.xml | rg -i 'split|requiredSplit'` — split-APK detection.
   For `.apkm`/`.xapk`, extract `base.apk` to a temp dir first and run `aapt` on that.
3. `uvx apkid <apk>` — compiler, obfuscator, packer, anti-debug, anti-VM (per DEX / lib).
4. `unzip -l <apk> | rg '\.dex'` — DEX count.
5. `unzip -l <apk> | rg 'index.android.bundle|libflutter|libapp'` — framework:
   `index.android.bundle` = React Native, `libflutter.so`/`libapp.so` = Flutter,
   `assets/www|public/` = Cordova/Capacitor, `libmonodroid.so|assemblies/` = Xamarin/MAUI,
   else native (Compose vs Kotlin distinguished via `androidx.compose` / `kotlin_module`
   DEX strings — `apk-recon.sh` does all of this automatically).
6. Record native-lib architectures and notable permissions (billing, internet, etc.).
7. Note HTTP/DI/billing stack signals from the recon report (Retrofit/OkHttp/Ktor/Apollo,
   Hilt/Koin, RevenueCat/Adapty/Play Billing) — they pick the hunt patterns in §3.

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
break testing of everything else. Start with a one-pass triage:

```bash
scripts/hunt-signals.sh analysis/<app>/decompiled [--files]
```

Then work the buckets below (highest signal first). These patterns are embedded
in `scripts/hunt-signals.sh` above — the script is the canonical copy; when a
pattern changes, update both places:

0. **BuildConfig sweep** (almost never obfuscated — base URLs, flavors, keys):
   `rg 'BASE_URL|API_URL|FLAVOR|API_KEY' -g 'BuildConfig.java' analysis/<app>/decompiled`
   Read every hit; each Gradle module emits its own file.

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
6. **Modern Kotlin stacks** (when Retrofit patterns miss — KMP/Kotlin-only apps):
   Ktor `client.get\(|client.post\(|defaultRequest|BearerTokens|loadTokens|refreshTokens`,
   Apollo `serverUrl|OPERATION_DOCUMENT`, Koin `module {|single<|factory<|by inject`,
   request signing `HmacSHA|SecretKeySpec|x-signature|computeSignature`.

### 3.5 Recover Kotlin names (obfuscated Kotlin apps only)

R8 renames JVM symbols but cannot strip `@DebugMetadata(c="…")` / `@Metadata(d2)`
strings. Before tracing call flows, rebuild the real names:

```bash
scripts/recover-kotlin-names.sh analysis/<app>/decompiled analysis/<app>/mapping
# → mapping.tsv / mapping.json / by_package/; typically ~100% of
# *Repository/*ViewModel/*UseCase/*Impl, ~80% of DTOs
```

Use the mapping to *find* classes (never to *match* — fingerprints still anchor on
SDK calls/strings/opcodes per `fingerprint-guide.md`). `jadx --deobf` alone is not
equivalent: it invents synthetic names instead of recovering the originals.

Obfuscation-resistant fallback: when call sites inline to `a.b(c, "…")`, grep the
path literals themselves — R8 never obfuscates string contents:

```bash
rg -o '"(/[A-Za-z0-9_{}.\-]+(/[A-Za-z0-9_{}.\-]+)+/?)"' analysis/<app>/decompiled -g '*.java'
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
