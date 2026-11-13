# Locked export API (Sprint 7)

| Symbol | Role |
|--------|------|
| `exportOpml(feeds)` | OPML 2.0 UTF-8 (see `org.aetherfeed.app.news.exportOpml`) |
| `exportLibraryJson(snapshot)` | Versioned JSON dump of feeds/stars/playback (`org.aetherfeed.app.export`) |
| `LibraryExportSnapshot` | `{ version: 1, exportedAt, feeds, stars, playback }` |

Fastlane / About copy must state exports are local-only and never leave the device unless the user copies the file.
