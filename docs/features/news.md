# Feature: news

> Status: 🔲 open · Public API locked 2026-08-18

## Acceptance criteria

- 🔲 Add, edit, delete, and organize RSS, Atom, and JSON Feed subscriptions
- 🔲 OPML import/export (export writes the current folder tree; import stays file-only)
- 🔲 Import subscriptions from Google Reader Takeout, Inoreader, and other OPML readers (`docs/features/reader-import.md`)
- 🔲 Rules / keyword include-exclude filters and auto-tag (Inoreader analog; local only)
- 🔲 Download full articles plus inline images for offline reading
- 🔲 Reader mode strips chrome/ads/nav
- 🔲 Unread / read / starred / tagged states
- 🔲 Per-feed and global full-text search
- 🔲 Per-feed notification channel
- 🔲 Works with zero cloud account
- 🔲 Offline: cached articles remain readable when fetch fails
- 🔲 Accessibility: feed list and reader are keyboard/TalkBack reachable
- 🔲 i18n: `news.*` web keys and `news_*` Android strings

## Locked public API

Canonical types live in `shared/typescript/news.ts` and
`examples/android/.../news/NewsModels.kt`. Repository port:
`NewsRepository` / `UnimplementedNewsRepository`.

| Name | Shape |
|------|--------|
| `FeedFormat` | `"rss" \| "atom" \| "jsonfeed"` |
| `ParsedFeed` | format, title, optional siteUrl, items |
| `ParsedArticle` | id, title, url, optional publishedAt/summary/contentHtml |
| `OpmlOutline` | title, optional xmlUrl/htmlUrl, children |
| `NewsRepository.subscribe(url)` | Fetch + persist a `Feed` |
| `updateFeed` / `unsubscribe` | Edit or delete a subscription |
| `importOpml(xml)` / `exportOpml()` | Nested outlines; only `xmlUrl` rows become feeds |
| `refresh(feedId)` | Pull items into `Article` rows |
| `articles(feedId?)` | List cached articles |
`flattenOpml` is the shared helper for nested outlines. Parsers and reader
UI are Parallel work after this lock.

## Smoke scenario

1. _Given_ a local vault
2. _When_ the user adds a feed URL
3. _Then_ articles appear unread and can be starred offline

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../news/`, `shared/typescript/news.ts` |
| View | `examples/android/.../ui/news/`, `examples/web/src/news/` |
| Tests | `news.test.ts`, `NewsApiTest.kt` |
| Wiring | composition root later; do not open the network from `MainActivity` |
## Definition of Done

See `docs/FEATURE_MODULES.md` per-feature checklist.

## Notes

- Reuse locked `Feed` / `Article` / `ReadState` from `shared/typescript/models.ts`
- `Article.contentHtml` is optional full text for reader mode and search
- No extra cloud accounts; fetch is user-initiated or local WorkManager refresh (shortest interval: 1 hour; vault must be unlocked)
- History is user-set by last N articles (default 10) or last 1–30 days per feed
- Headline lists persist encrypted per feed (`article-index` / `af-lock-article-index`) so they survive restart after unlock
- Settings: Wi-Fi only (default) or Wi-Fi and cellular for refresh, full-text GET, and image fetch
- Left column is a vertical source tree of every folder and feed (same on PC and Android). On a phone, swipe from the left edge or tap the folders button to open that tree. Folders start collapsed; tapping a header expands or collapses that folder’s feeds (remembered) and highlights the category. Last folder, last feed, and last News/Podcasts/Boards tab persist across cold start (`af-news-chrome` / `news_chrome` / `af-app-mode` / `app_shell`). Folder and feed rows show unread counts (hidden at zero). Drag pane widths persist. Timeline sort lives behind a filter icon (newest or oldest first) and is remembered.
- Open and prefetch convert to reading mode first, then download remaining in-article images (no social/share icons, comment threads, or article URLs). Cached articles reopen instantly and show a green dot on the headline. Opening an uncached article cancels the queue, loads that story, then the next in the sorted list. The timeline shows a determinate unread-cache bar and story thumbnails from the first remaining image.
- Desktop puts News / Podcasts / Boards on a bottom bar with per-mode unread badges. Refresh lives in the top bar. The tagline lives in About, not the News rail.
- Android applies plaintext `files/seed-library.json` after unlock (merges folders; skips duplicate URLs) and keeps the file so a later unlock can merge again. An encrypted desktop vault file is skipped (not deleted). Empty libraries stay empty (no automatic smoke feeds). The Smoke demo folder (HN/NPR) is pruned on every unlock. Unread full-text cache walks every news source, not just the open feed. Each unread id is fetched once after a real network attempt; a `fetched` marker or stored `articles` blob skips later launches. A rebuild/`adb install -r` keeps those files: existing blobs are not re-downloaded even if the headline index still looks like an RSS teaser. Only a decrypted teaser body (`webfeedsFeaturedVisual` / `link_thumbnail`) retries. Opening a cached usable body does not re-fetch images. Offline misses are not remembered as fetched. Green dots stay on usable reader bodies only. Prefetch does not RSS-refresh empty indexes (opening a feed still refreshes that source). HTTP 404/410 shows that the feed is no longer available.
- Desktop writes `%AppData%/org.aetherfeed.app/seed-library.json` after unlock for adb copy. Feed/article HTTP goes through a Tauri `feed_fetch` command (WebView CORS cannot load RSS).

## Full-text retrieval (honest reader mode)

Prefer RSS/Atom `content:encoded` / `content` / JSON Feed `content_html` already on `ParsedArticle.contentHtml`. RSS `description` and Atom `summary` stay on `summary` only — WordPress teasers (MotoIQ) often exceed 400 characters and are not full text. Markers `webfeedsFeaturedVisual` / `link_thumbnail` force a page fetch. If that body is missing or shorter than 400 characters, one user-initiated `GET` of `article.url` (timeout + abort) then `extractReadable`.

If the extracted body is still short or matches in-package paywall markers (`subscribe`, `subscriber-only`, `paywall`), show designed empty copy. Do not implement archive proxies, cookie replay, or site-specific bypasses. Soft-paywall sites may still yield a body from the first HTML the origin sent; sites that never emit the story without an authenticated session stay empty.

Reader HTML and inline images persist as ciphertext after unlock (ADR-0002). Search indexes rebuild in memory from decrypted bodies.

The reader never navigates to `article.url`. It shows extracted text plus cached images, using the app font and theme. Site links, CSS, and iframes are stripped. Android WebView blocks network loads.
