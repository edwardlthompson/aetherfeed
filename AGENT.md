# AGENT.md — original product brief (Sacred)

<!-- agent-brief:one-liner -->
One local-first client for feeds, podcasts, and image boards with offline stars and playback.
<!-- /agent-brief:one-liner -->

<!-- agent-brief:keywords -->
rss, podcasts, booru, local-first, privacy, offline, vault
<!-- /agent-brief:keywords -->

## Rules

- Keep the vault local-first; no account required.
- Stars and playback position must survive radio-off.
- Optional sync is ciphertext-only; never upload plaintext library state.
- No analytics, crash reporters, ads, or tracking libraries.
- Do not overwrite Sacred product paths (`docs/spec.md`, `AGENTS.md` project card) with template stubs.

## First milestone

Offline read path end-to-end with cached items, stars, and playback persisted locally.
