# Changelog

All notable changes to this project are documented here, newest first.
Entries describe user-visible app patch changes (added/changed/fixed support,
removals, warnings) — not commits or refactoring. Headings stay plain
`## <version> (<YYYY-MM-DD>)` with per-app `**App:**` bullets so Morphe
Manager can parse them.

## Unreleased

### Added
* **Threads:** Support 445.0.0.46.83 alongside 434.0.0.41.74 — Hide ads re-hunted for the new build (feed merge A0F → A0G, Media.DED → DGK); device QA passed on a single phone (fresh login, feed, pagination, video, no crash, no AD_ID).

## 1.0.0 (2026-09-07)

### Added
* **Threads:** Initial patch set for 434.0.0.41.74 — Hide ads (removes sponsored posts from the feed), Remove AD_ID permission, Change app name, and Change package name (opt-in; renaming can break login, providers, or push).
