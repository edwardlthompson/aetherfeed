# Feature: app-shell

> Status: 🔲 open · IA locked 2026-08-18. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 Three modes only: News, Podcasts, Boards (`ModuleKind` stays `"news" | "podcast" | "booru"`; UI label is Boards)
- 🔲 Settings and About are overlays, never modes or Android tabs
- 🔲 Import lives in Settings, not on the News surface
- 🔲 Wide and narrow: no bottom mode bar. News / Podcasts / Boards / Unified are left-tree roots (Unified is not a fourth `AppDestination`). News uses source | timeline | reader on wide viewports.
- 🔲 Offline/error: chrome still renders with empty designed states when the vault has no feeds or sources
- 🔲 Accessibility: tree roots are keyboard/TalkBack reachable; current pick uses `aria-current`
- 🔲 i18n: `nav.*` web keys and `nav_*` Android strings (`nav.boards` / `nav_booru` label Boards; `nav.unified` / `nav_unified`)
- 🔲 App lock: first-run PIN (6+ digits) or passphrase (8+); unlock on cold start and after 2 minutes in background; chrome stays locked until `unlock` succeeds
- 🔲 Forgot secret wipes the local vault and caches (no recovery)
- ✅ Recents / tab preview: Android `FLAG_SECURE` plus API 33 `setRecentsScreenshotEnabled(false)` on `MainActivity` (lock included). Web/desktop black cover when `document.visibilityState === "hidden"` or the window blurs. Windows cannot hide OS Alt-Tab frames (see `docs/UI_PARITY.md`).

## Smoke scenario

1. _Given_ the desktop library with 66 imported subscriptions (53 news, 13 podcasts)
2. _When_ the user opens the left tree News, then Podcasts, then Boards, then Unified
3. _Then_ News lists imported folders, Podcasts lists the 13 shows, Boards shows a designed empty (zero sources), and Unified merges those timelines without console/logcat errors

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/web/src/shell/` (Parallel), Android `ui/navigation/` |
| View | `AppShell.ts` mount (Sequential); `AetherFeedChrome.kt` |
| Tests | `AppShell.test.ts`, `privacyCover.test.ts`, `AppDestinationTest.kt`, `SecureRecentsTest.kt`; instrumented `MainActivitySmokeTest.windowIsFlagSecure`. Recents UI itself is not inspectable — `FLAG_SECURE` on the activity window is the fallback. |
| Wiring | `appBootstrap.ts` / `AetherFeedApp.kt` ≤10 lines |

## Definition of Done

See `docs/FEATURE_MODULES.md` per-feature checklist and BUILD_PLAN Sprint 11 Sequential rows.

## Locked AppLock API

Canonical types: `shared/typescript/appLock.ts`.

| Name | Shape |
|------|--------|
| `AppLockSecretKind` | `"pin" \| "passphrase"` |
| `AppLockState` | `"unset" \| "locked" \| "unlocked"` |
| `setSecret(secret, kind)` | First run; wraps the vault key |
| `unlock(secret)` | `true` on success; backoff after 5 failures |
| `lock()` | Drops the in-memory session key |
| `wipe()` | Deletes local vault and caches |
| `APP_LOCK_TIMEOUT_MS` | `120000` |

Forgot PIN/passphrase is a wipe. The secret is never stored in Settings plaintext.

## Notes

- Sequential owns composition roots and this spec. Parallel rows fill one mode per stack.
- Do not add a fourth mode. Unified is `LibraryPick.Unified` only. Do not rename `ModuleKind` to `"boards"`.
- Locked pick API: `Unified` | `All(mode)` | `Folder(mode, folder)` | `Source(mode, folder, sourceId)` in `LibraryPick` / `libraryPick.ts`.
- Chevron expands; the label selects. Selecting a mode root or folder merges that scope’s timeline (newest/oldest).
- Action bar stays; Share (`action-share`) sends the focused item URL. Cache retain lives in Settings (`cache-retain`): 30 days or next sync; starred ids are never deleted.
