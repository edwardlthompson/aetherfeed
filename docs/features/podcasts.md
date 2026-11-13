# Feature: podcasts

> Status: 🔲 open — public API locked for Sprint 3

## Acceptance criteria

- 🔲 Subscribe to podcast RSS feeds with audio enclosures
- 🔲 Play, pause, skip, seek, speed, queue, chapters when present
- ✅ Persist playback position (local vault / offline library)
- 🔲 Auto-download, download queue, delete downloaded episodes
- 🔲 Car/lockscreen controls
- 🔲 Unread = unplayed or in-progress per user settings
- 🔲 Per-show notification channel
- ✅ Works with zero cloud account

## Locked public API

Canonical types: `PodcastShow`, `Episode`, `PlaybackPosition` (`shared/typescript/models.ts`, Android `domain`).

| Symbol | Role |
|--------|------|
| `PodcastRepository` | List shows/episodes; enqueue; persist position via `LibraryRepository.upsertPlayback` |
| `PlaybackQueue` | Ordered `episodeId` list + `currentIndex` |
| `PlaybackCommand` | `Play`, `Pause`, `Seek(ms)`, `Skip(delta)`, `SetSpeed(rate)` |
| `EpisodeDownloadState` | `None`, `Queued`, `Downloading(progress)`, `Ready(localPath)`, `Failed` |
**Non-goals (this lock):** Car UI / Android Auto, lock-screen media session wiring, remote chapter CDN, cloud queue sync, proprietary cast SDKs.

**Android boundary path:** `examples/android/app/src/main/java/org/aetherfeed/app/podcasts/` (Media3 player + download worker — Sprint 3 row 1).

**Desktop boundary path:** `examples/desktop/src-tauri/src/playback.rs` — Tauri commands `playback_play`, `playback_pause`, `playback_seek`, `playback_skip`, `playback_set_speed`, `playback_position` mirroring `PlaybackCommand` (in-memory transport stub until native audio lands).

## Implemented vs missing (Sprint 3)

| Piece | Path | Tests |
|-------|------|-------|
| Vault playback rows | `RoomLibraryRepository` / `PlaybackDao` | `OfflineReadPathTest.kt` |
| `PlaybackCommand` + queue API | `podcasts/PodcastApi.kt` | — |
| Library persist hook | `LibraryPodcastRepository.savePosition` | via vault tests |
| Desktop playback commands | `desktop/src-tauri/src/playback.rs` | `playback.rs` unit tests (seek/skip) |
| Media3 player + foreground service | — | missing |
| Episode download queue | — | missing |
| Car / lockscreen controls | — | missing (non-goal until shipped) |
| Per-show notification channel | — | missing |
## Smoke scenario

1. _Given_ a subscribed show and a saved `PlaybackPosition` in the vault
2. _When_ the user resumes from the podcasts surface (Android Media3 or desktop `playback_seek` + `playback_play`)
3. _Then_ position restores from local storage and advances only while playing

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../podcasts/` · `examples/desktop/src-tauri/src/playback.rs` |
| Vault | `examples/android/.../data/` (`PlaybackEntity`, `PlaybackDao`) |
| Spec | `docs/features/podcasts.md` |
## Tests

- Automated: yes — unit tests beside the feature container.
- Run via `feature-gate.sh --stack multi`.

## Fallback validation

```bash
python3 scripts/agent-run.py feature-gate --stack multi

```
