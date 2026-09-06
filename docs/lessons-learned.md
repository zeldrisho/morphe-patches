# Lessons learned

Incident-driven rules, distilled from a sibling Morphe patch bundle
(`chiggi_morphe_patches`, see its `RULEBOOK.md`). Each rule earned its place
because an incident cost someone something — the "Why" column is the actual
incident. Add rows here when it happens to us; one line each.

Read this before writing a bypass, re-signing a build, or touching release config.

## Bypasses: what is (and isn't) patchable

| Rule | Why |
| ---- | --- |
| Before bypassing a server "verdict", grep for the client enum/branch that READS it; if the payload is opaque (ByteString/blob) and only the network layer touches it, there is nothing to bypass. | A "proxy-state bypass" shipped against an opaque server-minted `ByteString` with no client-side `NO_PROXY`/`BLOCKED` enum — the bypass was fictional. |
| Never neuter a stateful echo/ACK protocol; if the client stores a server value and echoes it back on later requests, dropping it self-reports tamper on EVERY request. | Returning Unit in a proxy-state interceptor made the client echo "clean" forever while the server said BLOCKED → 24h account lock for "malicious activity". |
| An `AD_ID`/billing permission does NOT imply a patchable surface; verify real `AdView`/`InterstitialAd`/`BillingClient` usage in app code first. | An app with AD_ID had zero in-app ads and no billing (AdMob string was just the ads-identifier SDK) → nothing to patch. |
| Don't fake an "unlock" for server-config-driven lists; confirm a client gate exists. | Video quality came from a server JSON with no client subscription filter; the real ceiling was server manifest + Widevine — unpatchable. |
| Block screens rendered from server widgets (protobuf-driven fragments re-fetched on every navigation) are not client-patchable; the block text isn't in the APK. | "Go to home" just re-ran the server's own actions → same screen loop. |
| Telemetry-only signals (`isRooted`/`isEmulator`/attestation logged but never gating) must NOT be patched — risk for zero benefit. | Operator feared a block that didn't exist; nothing gated login/playback on those signals. |
| Re-signing + VPN/proxy + client spoofs on a REAL streaming/social/paid account = ban risk; recommend a throwaway account early, official app for the real one. | Re-signed build + VPN got a real account locked 24h. |

## Patching pitfalls

| Rule | Why |
| ---- | --- |
| Force boxed getters carefully: returning null NPE-crashes when the caller unboxes; return a safe value instead (e.g. version getter → `"0"`). | A forced-update patch NPE'd because the caller `.intValue()`'d the result immediately. |
| Verify a pasted `.java` isn't truncated (unterminated `/**`): `awk '{o+=gsub(/\/\*/,"&")-gsub(/\*\//,"&")} END{print o}' file.java` must print 0. | A file cut at 80 cols ate method bodies → compile break only surfaced at `:patches:build`. |
| Leave dependency-injected UI SDKs (e.g. CleverTap) intact; disable only standalone telemetry (AppsFlyer, Firebase flags). | CleverTap drives in-app overlays via DI — disabling it crashed parts of the app. |
| Renaming a package can break web-OAuth/App-Links/Google sign-in (package+cert bound); keep rename toggleable and offer label-only as fallback. | Rename risked breaking Google/Apple/OAuth login; only email/password survived. |
| PairIP-style license checks on re-signed builds must be neutered or the app won't open at all; ship that bypass on by default. | Patched app redirected to the Play Store on every launch until the license check was bypassed. |

## TV / ABI / install issues

| Rule | Why |
| ---- | --- |
| An arm64-only "universal" APK fails `INSTALL_FAILED_NO_MATCHING_ABIS` on 32-bit (armeabi-v7a) TVs; merge the ABI splits into one universal before patching. | An APK shipping only `arm64-v8a` `.so` files failed to install on an armeabi-v7a TV. |
| Morphe Manager auto-selects the config split matching the PATCHING phone, so patching a multi-ABI TV bundle on an arm64 phone drops the other ABI split; feed the Manager a pre-merged universal instead (a patch cannot override split selection). | Phone-patched TV output wouldn't install on a differently-ABI'd Android TV. |
| "Not compatible with your TV" = a REQUIRED `<uses-feature>` the device lacks; check `aapt dump badging` (e.g. `android.software.live_tv required=true` blocks tuner-less devices). Mark it optional — distinct from the touchscreen phone→TV case. | A TV build's required `live_tv` feature blocked Chromecast/Google TV installs. |
| Verify native-lib page-alignment with the REAL tool (`zipalign -c -p 4`), never a hand-rolled offset calc; `morphe-cli` already page-aligns `.so` on rebuild. | A python offset check falsely flagged aligned `.so` files; `zipalign -c` said OK. |
| Mark TV-only apps with an `(Android TV)` suffix in `Compatibility(name)`, verified by a `LEANBACK_LAUNCHER` / `android.software.leanback` manifest check. | TV builds were indistinguishable from phone apps in the Manager list. |

## Signing (morphe-cli + Morphe.keystore)

| Rule | Why |
| ---- | --- |
| Morphe.keystore is **BKS**: empty store password, alias `Morphe`, key password `Morphe`. Read/convert it with keytool only via `-provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath <morphe-cli.jar>`; plain keytool says "Unrecognized keystore format". | Recovered by listing with the BC provider. |
| Default sign = `--keystore Morphe.keystore` with NO password/alias flags; do NOT pass `--keystore-password` (BKS integrity check fails). CLI options need the `=` form (`--keystore-entry-alias=Morphe`), not space-separated. | Both mistakes cost real build cycles. |
| The CLI's "Keystore does not contain entry with alias Morphe Key" is a MASK for a swallowed earlier error; when signing masks the real failure, build `--unsigned` then sign with `apksigner` (export key to PKCS12 first). If `apksigner` then fails reading the manifest, `zipalign -p -f 4` the APK and pass `--min-sdk-version <N>`. | ~10 cycles chased on a phantom alias error. |

## Build / release hygiene

| Rule | Why |
| ---- | --- |
| `patches-list.json` is GENERATED — never hand-edit; run `./gradlew generatePatchesList` and commit the result. | Hand edits drift from sources and leak unrelated work into feature commits via shared generated files. |
| Pin exact `AppTarget` versions; a null ("any") version is rejected by Morphe Manager (the whole source fails to load), and R8-obfuscated bytecode patches only verifiably resolve on the fingerprinted version. | Null versions broke Manager loading; version drift silently broke matches. |
| `gradlew` must be tracked executable (`git update-index --chmod=+x gradlew`); `core.fileMode=false` checkouts silently commit it non-executable. | CI died with exit 126 "Permission denied", so no release was ever cut. |
| The Manager serves the `.mpp` from the GitHub RELEASE named in `patches-bundle.json` — pushing source does nothing until a versioned release is cut. | New patches "not showing in Morphe" while the release stayed stale. |
| Keep unrelated pending work OUT of commits touching shared generated files: restore the other subsystem to HEAD, regenerate, commit, then re-apply. | A pending revert leaked into unrelated commits via `patches-list.json`. |
