# Local UserNotifications (no APNs)

Decision 10 + 19 — post **local** notices for unread/new items; **APNs / remote push deferred** until a paid Apple Developer account.

## Channels (parity names)

Match Android channel ids in copy only (iOS has no channels):

| Logical channel | Use |
|-----------------|-----|
| `aetherfeed_news` | New/unread news items |
| `aetherfeed_podcast` | Podcast episode notices |

Do **not** add a booru notification category unless booru posts real events (`DESIGN_BRIEF.md` §8).

## Permission

Request `UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge])` on first post attempt. No push entitlement in v1.

## Posting sketch

```swift
let content = UNMutableNotificationContent()
content.title = "AetherFeed"
content.body = "3 new articles"
content.sound = .default
let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil)
UNUserNotificationCenter.current().add(request)
```

## Gradle / KMP

Sprint 61 wires JVM stub posting in `examples/ios/compose/.../notify/LocalNotificationPoster.kt`
using shared `ArticleNotice`. Darwin `UNUserNotificationCenter` actual lands on `iosMain` when a Mac
host builds the target.

Sprint 61 row 1 owns **real posting** across platforms (`BUILD_PLAN.md`).