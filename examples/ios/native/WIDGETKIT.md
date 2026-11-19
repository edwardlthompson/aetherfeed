# WidgetKit unread count (App Group file)

Decision 11 — **count only**, same copy as Android (`Unread: %d`). The widget extension must **not** open SQLCipher or Keychain.

## App Group

| Key | Value |
|-----|--------|
| Group ID | `group.org.aetherfeed.app` |
| File | `unread_widget_snapshot.json` (UTF-8) |

Kotlin SoT: `shared/domain/.../WidgetUnreadSnapshot.kt`.

### JSON schema (version 1)

```json
{
  "version": 1,
  "unreadTotal": 42,
  "updatedAtEpochMs": 1700000000000
}
```

## Write path (main app)

After vault read-state changes in the main process:

1. Compute `unreadTotal` via `totalUnread(...)`.
2. Write `WidgetUnreadSnapshot` JSON atomically to the App Group container URL (`FileManager.default.containerURL(forSecurityApplicationGroupIdentifier:)`).
3. Call `WidgetCenter.shared.reloadTimelines(ofKind:)` for kind `AetherFeedUnreadWidget`.

## Read path (WidgetKit extension)

1. Read JSON from the App Group file only.
2. Render `Text("Unread: \(unreadTotal)")` — no network, no vault.

## Placeholder Swift

See `UnreadWidget.swift` — compile on Mac when the extension target is added; not built on Windows.