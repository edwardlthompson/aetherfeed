# Android platform notes

**applicationId:** `org.aetherfeed.app`  
**UI:** Jetpack Compose, Material 3, dynamic color  
**Architecture:** Clean domain + MVI screens  
**Store:** Room + SQLCipher (production open path after Sprint 1)  
**DI:** Hilt (composition root in Sprint 1; seed uses in-memory library)

## OS features

| Feature | Notes |
|---------|--------|
| Notification channels | News, Podcasts, Booru, plus per-subscription toggles |
| Widget | Home-screen total unread across all modules |
| WorkManager | Background fetch, download, and optional sync |
| Biometric unlock | Optional vault lock |
| Media3 | Podcast playback, queue, speed, sleep timer, lockscreen |

## FOSS constraints

- No Play Services, Firebase, or crash SDKs
- Google Drive sync uses REST + `drive.appdata` only
- Fastlane metadata under `examples/android/fastlane/`
