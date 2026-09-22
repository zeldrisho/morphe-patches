# Zalo 26.08.01 validation journey

This is a versioned execution specification, not a recorded device result.
Run it only with the procedure in [`validation.md`](../validation.md). Keep
artifacts outside Git.

## Matrix

- Package: `com.zing.zalo`
- Version code: `260801903`
- Builds: stock/control and selected patch build
- Account: throwaway test account
- Provider: absent/disabled for provider journey; enabled for Drive journey

## Journey

| Step | Assertion | Result | Evidence |
| --- | --- | --- | --- |
| Cold launch | App launches without crash or freeze | UNEXECUTED | |
| Background/resume | Existing session resumes normally | UNEXECUTED | |
| Provider prompt | Missing-provider prompt appears | UNEXECUTED | |
| Prompt cancel | Cancel returns to usable Zalo flow | UNEXECUTED | |
| Account picker | Selected `app.revanced` account is returned | UNEXECUTED | |
| Drive refresh | Selected account replaces stale account state | UNEXECUTED | |
| Notification | Intended notifications remain available | UNEXECUTED | |

Record package/version, APK and bundle hashes, device/Android version, enabled
patches, and signing fingerprint. Do not record accounts, tokens, screenshots,
UI dumps, or logs in this repository.
