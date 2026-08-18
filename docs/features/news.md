# Feature: news

> Status: 🔲 open

## Acceptance criteria

- 🔲 Add, edit, delete, and organize RSS, Atom, and JSON Feed subscriptions
- 🔲 OPML import/export
- 🔲 Download full articles plus inline images for offline reading
- 🔲 Reader mode strips chrome/ads/nav
- 🔲 Unread / read / starred / tagged states
- 🔲 Per-feed and global full-text search
- 🔲 Per-feed notification channel
- 🔲 Works with zero cloud account

## Smoke scenario

1. _Given_ a local vault
2. _When_ the user adds a feed URL
3. _Then_ articles appear unread and can be starred offline
