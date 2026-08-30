# Feature: podcasts

> Status: 🔲 open · Public API locked 2026-08-18

## Acceptance criteria

- 🔲 Subscribe to podcast RSS feeds with audio enclosures
- 🔲 Play, pause, skip, seek, speed, queue, chapters when present
- 🔲 Sleep timer that stops playback after a chosen duration
- 🔲 Silence skip and voice boost processed on-device (no cloud audio upload)
- 🔲 Persist playback position
- 🔲 Auto-download, download queue, delete downloaded episodes
- 🔲 Car/lockscreen controls
- 🔲 Unread = unplayed or in-progress per user settings
- 🔲 Per-show notification channel

## Locked public API

| Name | Shape |
|------|--------|
| `PodcastPlayer.play/pause/seekTo` | Control one episode |
| `PodcastPlayer.position(episodeId)` | `PlaybackPosition` or null |
| `NoopPodcastPlayer` | Hermetic test double; Media3 binds later |
| Desktop `playback_state` | `stopped` / `playing` / `paused` |
Reuse locked `PodcastShow`, `Episode`, and `PlaybackPosition` from shared models.

## Transcripts fallback

On-device transcripts ship only if a FOSS speech path exists. Current command: `python scripts/agent-run.py feature-gate --stack web` (`examples/web/src/transcripts/`).

## Smoke scenario

1. _Given_ a subscribed show
2. _When_ a new episode is fetched
3. _Then_ it is unplayed and can download to app-private storage
