# 🧩 Zeldris Patches

Patches for apps I like, built for [Morphe](https://morphe.software).

## ❓ About

Patches for apps I like, built for Morphe.

Click here to add these patches to Morphe: https://morphe.software/add-source?github=zeldrisho/morphe-patches

## 🩹 Patches list

<!-- PATCHES_START -->
> **[v1.10.0-dev.1](https://github.com/zeldrisho/morphe-patches/releases/tag/v1.10.0-dev.1)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;27 patches total
<details>
<summary>📦 Zalo&nbsp;&nbsp;•&nbsp;&nbsp;22 patches</summary>
<br>

**🎯 Supported versions:**

| 26.08.01 | 26.10.02 |
| :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Bypass Zalo Drive Contacts permission gate](#bypass-zalo-drive-contacts-permission-gate) | Allows Drive photo restore account selection without Contacts permission. |  |
| [Bypass native startup tamper check](#bypass-native-startup-tamper-check) | Allows the re-signed app to start on the supported arm64 Zalo build. |  |
| [Change Zalo app name](#change-zalo-app-name) | Changes the app name shown in the launcher. | • App name |
| [Change Zalo package name](#change-zalo-package-name) | Allows a renamed clone alongside Zalo. Login, push, sharing and deep links may be affected. | • Package name |
| [Configurable native backup interval](#configurable-native-backup-interval) | Sets Zalo's automatic backup interval to 1, 3, 6 or 12 hours. Existing backup requirements still apply. | • Backup interval (hours) |
| [Disable ads](#disable-ads) | Blocks selected offline and Google-served ads. Story and community sponsored placements require a separate patch. |  |
| [Disable sponsored placements](#disable-sponsored-placements) | Disables Story and community sponsored placements. Server-delivered and Official Account promotions may remain. |  |
| [Disable telemetry and crash reporting](#disable-telemetry-and-crash-reporting) | Stops selected Zalo analytics and crash reporting. Messaging is unaffected. |  |
| [Enable Google Drive photo backup](#enable-google-drive-photo-backup) | Makes Zalo's Google Drive photo backup available to more accounts. Google authorization and Drive retention still apply. |  |
| [Enable avatar saving](#enable-avatar-saving) | Restores Save photo for avatars opened from a profile and allows screenshots. Other screenshot protections are unchanged. |  |
| [Enable profile cover saving](#enable-profile-cover-saving) | Restores Save photo for profile cover photos and allows screenshots. Other viewers are unchanged. |  |
| [Filter promo notifications](#filter-promo-notifications) | Filters Timeline, Stories and Zalo Video notifications. Message, call, friend-request and birthday notifications are unaffected. |  |
| [Hide Business Box](#hide-business-box) | Hides Business Box from the chat list without hiding ordinary or user-initiated Official Account chats. |  |
| [Hide Media Box](#hide-media-box) | Hides Media Box from the chat list without deleting its content. |  |
| [Hide chat list ads](#hide-chat-list-ads) | Removes ad cards from the chat list. Server-delivered promotions and other ads may remain. |  |
| [Keep expired media accessible](#keep-expired-media-accessible) | Keeps locally stored large chat media accessible after expiry. Missing files and server restrictions are unaffected. |  |
| [Prefer original photo quality](#prefer-original-photo-quality) | Enables original photo quality by default. Upload limits and video are unaffected. |  |
| [Remove AD_ID permission](#remove-ad-id-permission) | Removes Zalo's advertising ID permissions. Messaging is unaffected. |  |
| [Remove media backup age limit](#remove-media-backup-age-limit) | Includes media of any age in Zalo's Google Drive backup. Drive retention and excluded media still apply. |  |
| [Suppress outbound seen status](#suppress-outbound-seen-status) | Stops sending read receipts. Incoming read status and message delivery are unaffected. |  |
| [Suppress outbound typing status](#suppress-outbound-typing-status) | Stops sending typing indicators. Incoming typing status is unaffected. |  |
| [microG Drive support](#microg-drive-support) | Uses microG-RE for Google Drive account selection and sign-in. Requires app.revanced.android.gms and the app.revanced account type. |  |

</details>

<details>
<summary>📦 Threads&nbsp;&nbsp;•&nbsp;&nbsp;5 patches</summary>
<br>

**🎯 Supported versions:**

| 434.0.0.41.74 | 445.0.0.46.83 | 449.0.0.54.82 | 453.0.0.56.79 |
| :---: | :---: | :---: | :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Change app name](#change-app-name) | Changes the app name shown in the launcher. | • App name |
| [Change package name](#change-package-name) | Allows a renamed clone alongside Threads. Login, content providers or push may break. | • Package name |
| [Hide ads](#hide-ads) | Removes sponsored posts from the Threads feed. Other surfaces are unaffected. |  |
| [Open links externally](#open-links-externally) | Opens web links externally when possible; otherwise Threads handles them normally. |  |
| [Remove AD_ID permission](#remove-ad-id-permission) | Removes advertising ID permissions. Meta's own analytics are unaffected. |  |

</details>

<!-- PATCHES_END -->

## 🧑‍💻 Development

- [Setup, build and release](docs/development.md)
- [Architecture](docs/architecture.md)
- [APK inspection](docs/recon.md)
- [Testing and app compatibility](docs/testing.md)
- [microG Drive support](docs/microg.md)

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Zeldris Patches are licensed under the [GNU General Public License v3.0](LICENSE).
