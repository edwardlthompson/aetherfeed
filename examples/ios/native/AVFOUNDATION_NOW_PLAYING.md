# AVFoundation — background audio & Now Playing (decision 7)

Podcast playback must expose lockscreen controls on iOS. Implementation stays in `iosMain` / Swift interop — **not** verifiable on Windows.

## Session

```swift
try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
try AVAudioSession.sharedInstance().setActive(true)
```

Use `AVPlayer` for enclosure URLs; map shared `PlaybackCommand` to `MPRemoteCommandCenter` (play/pause/seek).

## Now Playing

Update `MPNowPlayingInfoCenter.default().nowPlayingInfo` with title, artist/podcast show, duration, elapsed time, and optional artwork. Enable:

- `MPRemoteCommandCenter.shared().playCommand`
- `pauseCommand`
- `changePlaybackPositionCommand`

## CMP boundary

Compose UI shows the compact player bar (`DESIGN_BRIEF.md` §4); platform code owns audio session + remote commands.

## Simulator limits

Lockscreen Now Playing UI is unreliable in Simulator — physical device check is `[HUMAN]` (decision 19).