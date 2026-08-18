# Feature: podcasts

> Status: 🔲 open

## Acceptance criteria

- 🔲 Subscribe to podcast RSS feeds with audio enclosures
- 🔲 Play, pause, skip, seek, speed, queue, chapters when present
- 🔲 Persist playback position
- 🔲 Auto-download, download queue, delete downloaded episodes
- 🔲 Car/lockscreen controls
- 🔲 Unread = unplayed or in-progress per user settings
- 🔲 Per-show notification channel

## Smoke scenario

1. _Given_ a subscribed show
2. _When_ a new episode is fetched
3. _Then_ it is unplayed and can download to app-private storage
