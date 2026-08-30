# Feature: booru

> Status: 🔲 open · Public API locked 2026-08-18

## Acceptance criteria

- 🔲 Add Danbooru-style, Gelbooru-style, and similar public APIs
- 🔲 Tag search, autocomplete when the source supports it
- 🔲 Saved searches, favorites, and local blacklists
- 🔲 Rating / safe-mode filter (hide unsafe ratings unless the user opts in)
- 🔲 Per-source API keys for engines that require them (Gelbooru-family reads)
- 🔲 Hash / MD5 dedup so the vault stores one file per digest; optional filename tokens
- 🔲 Grid/list browsing, post detail, selected downloads
- 🔲 Favorites, blacklists, and saved queries join E2E sync
- 🔲 Per-source or per-saved-search notification channel
- 🔲 Generic media-board client: no adult branding, no copied chrome

## Smoke scenario

1. _Given_ a configured public source
2. _When_ the user searches a tag
3. _Then_ posts render in a quiet grid and can be favorited locally
