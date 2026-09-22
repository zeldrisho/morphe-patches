# Threads 445.0.0.46.83 validation journey

This is a versioned execution specification, not a recorded device result.
Run it only with the procedure in [`validation.md`](../validation.md). Keep
artifacts outside Git.

## Matrix

- Package: `com.instagram.barcelona`
- Version code: `511507647`
- Builds: stock/control and selected patch build
- Feed: test data containing organic and sponsored units

## Journey

| Step | Assertion | Result | Evidence |
| --- | --- | --- | --- |
| Cold launch | App launches without crash or freeze | UNEXECUTED | |
| Feed load | Organic units appear in the expected order | UNEXECUTED | |
| Sponsored filtering | Sponsored units are absent from the selected feed | UNEXECUTED | |
| Scrolling | Continued scrolling does not crash or reorder organic units | UNEXECUTED | |
| Refresh | Refresh preserves filtering and organic ordering | UNEXECUTED | |

Record package/version, APK and bundle hashes, device/Android version, enabled
patches, and signing fingerprint. Do not record accounts, tokens, screenshots,
UI dumps, or logs in this repository.
