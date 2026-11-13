# Feature: booru

> Status: 🔲 open — Sprint 4 grid slice in progress (API locked)

## Acceptance criteria

- 🔲 Add Danbooru-style, Gelbooru-style, and similar public APIs
- 🔲 Tag search, autocomplete when the source supports it
- 🔲 Saved searches, favorites, and local blacklists
- 🔲 Grid/list browsing, post detail, selected downloads
- 🔲 Favorites, blacklists, and saved queries join E2E sync
- 🔲 Per-source or per-saved-search notification channel
- 🔲 Generic media-board client: no adult branding, no copied chrome

## Locked public API

Canonical types: `shared/typescript/models.ts` and Android `org.aetherfeed.app.domain` (`BooruPost`, `Star`, `Tag`).

| Symbol | Role |
|--------|------|
| `BooruSource` | `id`, `baseUrl`, `kind` (`danbooru` \| `gelbooru` \| `generic`) |
| `BooruRepository` | `search(sourceId, tags)`, `favorite(post)`, `blacklist(tag)`, `listFavorites()` |
| `BooruSearchQuery` | `tags: List<String>`, `page: Int`, `limit: Int` |
| `BooruAdapter` | Map remote JSON → `BooruPost`; never log raw adult filenames |
**Non-goals (this lock):** Source-specific login flows, API key UX, image proxy/CDN, detail viewer zoom/gesture polish, blacklists UI, sync of favorites metadata beyond `Star`. Booru source URLs join the Hermes subscription pool for same-device AetherFeed sync; Hermes does not surface booru in the launcher (`docs/features/hermes-subscription-pool.md`).

**Android boundary path:** `examples/android/app/src/main/java/org/aetherfeed/app/booru/`

## Implemented vs missing (Sprint 4)

| Piece | Path | Tests |
|-------|------|-------|
| Locked repository stub | `booru/BooruApi.kt` (`InMemoryBooruRepository`) | Android unit (repo slice) |
| Web grid chrome + i18n | `examples/web/src/booru/gridChrome.ts` | `gridChrome.test.ts` |
| Web module tab (News / Booru) | `examples/web/src/AppShell.ts` (`activeModule`, `data-booru-mount`) | via bootstrap re-render |
| Source adapters (Danbooru / Gelbooru) | — | missing |
| Android grid + detail UI | — | missing |
| Live search + post grid cells | — | missing |
| Post detail + download | — | missing |
**Web tab switch:** Home shows a two-tab strip (`nav.news` / `nav.booru`). Choosing Booru mounts `createBooruGridChrome` on `[data-booru-mount]`; News keeps `createNewsChrome` on `[data-news-mount]`. State lives in `AppShellState.activeModule` (`news` \| `booru`).

## Smoke scenario

1. _Given_ a configured public source
2. _When_ the user searches a tag
3. _Then_ posts render in a quiet grid and can be favorited locally

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../booru/` · `examples/web/src/booru/` |
| View | `examples/android/.../ui/booru/` (planned) · web `gridChrome.ts` |
| Tests | co-located unit tests |
| Spec | `docs/features/booru.md` |
## Tests

- Automated: yes — unit tests beside the feature container.
- Run via `feature-gate.sh --stack multi`.

## Fallback validation

```bash
python3 scripts/agent-run.py feature-gate --stack multi

```
