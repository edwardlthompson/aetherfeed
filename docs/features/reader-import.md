# Feature: reader-import

> Status: ✅ done · File-only import, URL dedupe, news vs podcast sort. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ Import Google Reader / gReader Takeout or OPML on Android via a local file picker
- ✅ Import Inoreader OPML or backup JSON on desktop/web; persist across reloads
- ✅ Detect vendor from filename + content; unknown OPML still yields feeds
- ✅ Skip duplicates by **normalized** feed URL (scheme, `www.`, trailing slash, `feed/` prefix)
- ✅ Classify each outline as `news` or `podcast` from folder, title, and host (no feed fetch)
- ✅ Optional stars/unread when the export includes them
- ✅ Offline/error: empty/malformed files report typed errors; vault unchanged on hard parse failure
- ✅ Accessibility: file picker and result summary are keyboard/TalkBack reachable
- ✅ i18n: `readerimport.*` web keys and `readerimport_*` Android strings
- ✅ Works with zero cloud account — no live Google or Inoreader login

## Export steps (user)

1. **Phone:** Export OPML (or a Takeout zip with `subscriptions.xml`). In AetherFeed Settings, tap Import subscriptions.
2. **PC:** Export OPML from your reader. In AetherFeed Settings, choose that file.

## Smoke scenario

1. _Given_ a local vault and a gReader or Inoreader export
2. _When_ the user picks that file
3. _Then_ news URLs land under News, podcast URLs under Podcasts, and a second import of the same feeds increments skipped

## Locked public API

| Name | Shape |
|------|--------|
| `normalizeFeedUrl(url)` | Canonical key: https, no `www.`, no trailing slash, strip `feed/` |
| `classifyFeedKind(outline)` | `"news" \| "podcast"` from folder/title/host/path |
| `OpmlOutline.folder` / `type` | Optional; parser attaches nearest parent folder |
| `ReaderImportResult` | vendor, feedsAdded, feedsSkipped, starsApplied, newsAdded, podcastsAdded, errors[] |
| `detectReaderVendor` / `parseReaderImport` / `apply` | Unchanged contracts plus kind + normalize |

No OAuth or vendor SDKs.

## Synthetic fixtures

**Dedupe** — these three URLs are one subscription: `HTTP://WWW.Example.invalid/rss.xml/`, `https://example.invalid/rss.xml`, `feed/https://www.example.invalid/rss.xml`

**Podcast folder**

```xml
<opml><body>
  <outline text="Podcasts">
    <outline title="Cast" xmlUrl="https://feeds.libsyn.com/9/rss"/>
  </outline>
  <outline title="Local" xmlUrl="https://example.invalid/rss.xml"/>
</body></opml>
```

Hard-failure fixture: `{not-json` → `malformed`, zero feeds written.

## Container map

| Layer | Path |
|-------|------|
| Logic | `shared/typescript/feedIdentity.ts`, `readerImport*.ts`, Android `readerimport/` |
| View | `examples/web/src/readerimport/`, Android News import + Podcasts list |
| Tests | co-located unit tests + fixtures above |
| Wiring | News/Podcasts panes; AppShell already mounts the web picker |

## Definition of Done

`feature-gate.sh --stack multi` plus parser/identity unit tests.

## Notes

- Google Reader / gReader = export files, not a live Reader API
- Inoreader = user-exported OPML/JSON, not the developer API
- Ask before adding cloud logins
