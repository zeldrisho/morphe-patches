# Zalo 26.08.01 validation journey

Blank execution sheet. Follow [validation](../validation.md) for stock/control/patched
builds, throwaway accounts, result recording, and private evidence handling.

## Matrix

- Package: `com.zing.zalo`
- Version code: `260801903`
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
