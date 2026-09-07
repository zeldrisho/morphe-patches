# Plan — remaining work (Threads feed ad-removal, issue #5)

The rework is implemented and device-verified. What's left:

## Ship it
- [ ] Full-combo device pass on default-on patches (Hide ads + Remove AD_ID +
      Change app name) → confirm login & feed still OK (`docs/qa-checklist.md`
      §§2–3). Change package name is opt-in — test side-by-side install separately.
- [ ] Commit `fix:`/`feat:` on `dev`; cut the release via semantic-release
      (never by hand — see `docs/release.md`). `patches-list.json` regenerates
      in the flow.

## Follow-ups
- [ ] Optional: extend Hide ads to clips/reels sponsored surfaces (feed-only today).
- [ ] Recurring, per Threads update: re-verify fingerprints and update the pin
      (`docs/qa-checklist.md` §4, `docs/maintenance.md`).
