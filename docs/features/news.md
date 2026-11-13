# Feature: news

> Status: 🔲 open — Sprint 2 reader slice in progress (API locked)

## Acceptance criteria

- 🔲 Add, edit, delete, and organize RSS, Atom, and JSON Feed subscriptions
- ✅ OPML import/export (`parseOpml` / `exportOpml` + unit tests)
- ✅ Download full articles for offline reading (local body cache; images still open)
- 🔲 Reader mode strips chrome/ads/nav
- ✅ Unread / read / starred states (tagged still open)
- 🔲 Per-feed and global full-text search
- 🔲 Per-feed notification channel
- ✅ Works with zero cloud account

## Locked public API

Canonical types: `shared/typescript/models.ts` and Android `org.aetherfeed.app.domain` (`Feed`, `Article`, `ReadState`, `Star`, `Tag`).

| Symbol | Role |
|--------|------|
| `NewsRepository` | `listFeeds` / `subscribe` / `unsubscribe` / `ingest` (refresh fetch still open) |
| `FeedDocument` | Normalized parse result: `feeds: Feed[]`, `articles: Article[]` |
| `parseFeed(bytes, contentType, baseUrl)` | RSS 2.0 / Atom / JSON Feed 1.1; throw typed `NewsParseError` |
| `parseOpml(bytes)` | Return `Feed[]` from OPML 2.0 outlines (`xmlUrl` / `htmlUrl`) |
| `exportOpml(feeds)` | Serialize subscriptions to OPML 2.0 UTF-8 |
| `NewsParseError.code` | `Unsupported`, `Malformed`, `Empty` |
**Non-goals (this lock):** HTML full-text extractors, paywall bypass, remote sync of plaintext, HTTP refresh orchestration, article tag UX.

**Android boundary path:** `examples/android/app/src/main/java/org/aetherfeed/app/news/`

## Implemented vs missing (Sprint 2)

| Piece | Path | Tests |
|-------|------|-------|
| JSON / RSS / Atom parse | `news/FeedParsers.kt` | `FeedParsersTest.kt` |
| OPML round-trip | `news/FeedParsers.kt` | `FeedParsersTest.kt` |
| Ingest → unread seed | `news/NewsRepository.kt` | `NewsRepositoryTest.kt` |
| Android reader list UI | `ui/news/NewsScreen.kt` | unit coverage via repository + UI wiring |
| Web reader chrome + i18n | `examples/web/src/news/readerChrome.ts` | `readerChrome.test.ts` |
| HTTP refresh / remote fetch | — | missing |
| Atom advanced / media RSS | — | missing |
| Tag attach UX | — | missing |
## Smoke scenario

1. _Given_ a local vault
2. _When_ the user adds a feed URL (or ingests a `FeedDocument`)
3. _Then_ articles appear unread and can be starred offline

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../news/` · `examples/web/src/news/` |
| View | `examples/android/.../ui/news/` · web `readerChrome.ts` |
| Tests | co-located unit tests |
| Spec | `docs/features/news.md` |
## Tests

- Automated: yes — unit tests beside the feature container.
- Run via `feature-gate.sh --stack multi`.

## Fallback validation

```bash
python3 scripts/agent-run.py feature-gate --stack multi

```
