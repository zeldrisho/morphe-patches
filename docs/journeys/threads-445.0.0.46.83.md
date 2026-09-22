# Threads 445.0.0.46.83 validation journey

Blank execution sheet. Follow [validation](../validation.md) for stock/control/patched
builds, result recording, and private evidence handling.

## Matrix

- Package: `com.instagram.barcelona`
- Version code: `511507647`
- Feed: test data containing organic and sponsored units

## Journey

| Step | Assertion | Result | Evidence |
| --- | --- | --- | --- |
| Cold launch | App launches without crash or freeze | UNEXECUTED | |
| Feed load | Organic units appear in the expected order | UNEXECUTED | |
| Sponsored filtering | Sponsored units are absent from the selected feed | UNEXECUTED | |
| Scrolling | Continued scrolling does not crash or reorder organic units | UNEXECUTED | |
| Refresh | Refresh preserves filtering and organic ordering | UNEXECUTED | |
