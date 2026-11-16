# Design brief — AetherFeed (binding)

> Phase 1 of the multiplatform migration. **Preserve** the current Android Compose look, layout, navigation, terminology, and flows on every platform. Platform conventions only where unavoidable (iOS back gesture, desktop menus/window chrome). **No redesign.**
>
> SoT for “what exists today”: [`INVENTORY.md`](INVENTORY.md). Tokens: [`design-tokens/design-tokens.json`](../../design-tokens/design-tokens.json). Law: [`docs/DESIGN_GUIDE.md`](../DESIGN_GUIDE.md), [`docs/ux-ui-guidelines.md`](../ux-ui-guidelines.md). Decisions: INVENTORY § Decisions (Edward, 2026-11-16).

This brief is binding for Phase 3+ implementation. Visual or IA changes that are not listed under **Platform conventions** or **Deviations** need Edward’s approval before they ship.

---

## 1. Binding principle

The Android app in `examples/android/.../ui/` is the product UI. Compose Multiplatform clones that UI — same Material 3 components, same bottom destinations, same Settings/About overlays, same Add-feed dialog, same reader chrome — on Android, iOS, Windows, macOS, and Linux.

Do not “iOS-ify” the tab bar into a top `TabView`. Do not replace FilterChips with iOS segmented controls. Do not add a web-style header theme toggle (that is `examples/web`; Android keeps theme in Settings). Do not invent a new information architecture.

**Parity of existing behavior** (decision 17): ship the news-first slice that already works, plus native must-haves from the 2026-11-16 decisions (lockscreen audio, notifications, widgets, encrypted vault, Add-feed on desktop). Do **not** pull in unbuilt acceptance rows (search, OPML UI, real downloads, Gelbooru, Drive/WebDAV, biometrics) in the first shells.

---

## 2. Platforms and chrome

| Surface | Product UI | Allowed native chrome |
|---------|------------|------------------------|
| Android | Shared Compose | System bars, IME, home widget, notification shade, UnifiedPush, Hermes (optional) |
| iOS | Shared Compose | Edge swipe back, status/home indicator, WidgetKit, Now Playing, local notifications |
| Windows / macOS / Linux | Shared Compose in a desktop window | Title bar, app menu, tray unread tooltip, window close/minimize, desktop notifications |

Window default (from today’s Tauri config, keep the size): 1100×760, resizable, title `AetherFeed`.

---

## 3. Navigation flow (do not change)

```
Launch
  → AetherFeedTheme (system / light / dark, default system)
  → Main shell
       TopAppBar: title “AetherFeed” + Settings gear
       Bottom bar: News | Podcasts | Booru  (last tab persisted)
       FAB: Add subscription (visible on main shell, not on Settings/About/reader)
       Overlay: Settings (gear)
       Overlay: About (from Settings → About)
News → article list → Article reader (or external browser per opener pref)
       Reader back → list (system back / iOS swipe / toolbar back)
Podcasts → show list or empty body
Booru → demo grid → post detail → back to grid
```

No URL router. No fourth bottom destination. Settings and About are **overlays**, not tabs.

Icons (current `AppDestination`): News `Icons.Outlined.Home`, Podcasts `Icons.Outlined.PlayArrow`, Booru `Icons.Outlined.Favorite`. Settings gear `Icons.Filled.Settings`. FAB `Icons.Filled.Add`.

---

## 4. Screens and components inventory

Clone these composables; do not replace them with new designs.

| Surface | Android source | Components |
|---------|----------------|------------|
| Theme | `ui/theme/Theme.kt`, `Color.kt`, `Type.kt`, `Dimens.kt` | `AetherFeedTheme`, M3 `colorScheme` / `typography` |
| Scaffold | `ui/components/AetherFeedScaffold.kt`, `ui/insets/` | Edge-to-edge, snackbar host, `bottomInsetPadding` |
| Chrome | `ui/AetherFeedChrome.kt` | `AetherFeedTopBar`, `AetherFeedBottomBar` |
| Shell | `ui/AetherFeedApp.kt`, `AetherFeedScreen.kt`, `AetherFeedBody.kt` | Tab persistence, vault load, FAB |
| Add feed | `ui/AddFeedFab.kt` | FAB + `AlertDialog`: URL, optional title, News/Podcast/Booru chips, Add / Cancel |
| News list | `ui/news/NewsScreen.kt`, `ui/browse/BrowseFilterBar.kt` | `ReaderSplit` tree + list; unread •; star; empty `filter_empty` |
| Article reader | `ui/news/ArticleReaderScreen.kt`, `ArticleReaderBlocks.kt`, `ArticleSwipeOverlay.kt` | Toolbar: back, mode Reading/Full/Web, star, park unread, open in browser; swipe prev/next; text scale from Settings |
| Podcasts | `ui/podcasts/PodcastsScreen.kt` | Same `ReaderSplit` + show title/URL rows; empty `pane_podcasts_body` |
| Booru | `ui/booru/BooruScreen.kt` | Grid + detail + tags heading + favorite star; **demo posts only** |
| Settings | `ui/settings/SettingsScreen.kt`, `SettingsReaderSection.kt` | See §6 |
| About | `ui/about/AboutScreen.kt` | Version, format, update status, Apply update, export **hint**, donations links, Close |
| Widget | `notify/UnreadWidgetProvider.kt`, `unread_widget.xml` | Count only: `Unread: N` / `AetherFeed unread` |

`ThemeToggle.kt` and `ModulePane.kt` are **not** product chrome (unused / unreachable). Do not resurrect them.

**Podcast playback chrome (must-have native, not a redesign):** keep the Android list. Add a compact player bar using the same M3 surfaces (existing tokens, no new visual system) at least as capable as today’s web `<audio controls>` (play/pause/seek). Lockscreen / Now Playing is OS chrome, not an in-app skin.

---

## 5. Terminology (i18n)

Use Android `res/values/strings.xml` keys as the shared string SoT (CMP resources). Do not switch to web `locales/en.json` copy where they differ (web still says “AetherFeed stub”; Android stub status strings are unused — **do not start displaying them**).

| UI word | Meaning |
|---------|---------|
| News / Podcasts / Booru | Bottom destinations |
| Settings / About | Overlays |
| Add subscription | FAB dialog title |
| Star / Remove star / Favorite post | Toggle; booru uses “favorite” in content description only |
| Park unread | Reader action |
| Unread / Starred / All subscriptions | Filters |
| Newest first / Oldest first | Sort |
| In-app reader / Browser / Custom tab | Opener |
| Reading / Full / Web | Reader mode |
| Always / Wi-Fi / Never | Article images |
| Home LAN pool / Hub URL / Shared passphrase / Save LAN settings / Sync now | Sync v1 |
| Hermes subscription sync | Android-only status; omit the block on iOS/desktop |
| Check for updates | Opt-in |

English only for this migration. No new locales.

---

## 6. Settings and About field order

**Settings** (scroll, `SpacingMd`, one primary Close at the bottom):

1. Heading `Settings`
2. Theme label + FilterChips: System / Light / Dark
3. Check for updates switch
4. Preferred opener (chips) + body copy
5. Text size chips (percent)
6. Article images chips
7. Hermes status (**Android only**, read-only)
8. Home LAN pool: hint, Hub URL, Shared passphrase, status, Save + Sync now (Sync now is the one filled primary in that row)
9. About (outlined)
10. Close settings (filled, bottom inset)

**About:** version, installed format, update status, Apply update if compatible, export hint (no export button — logic-only stays later), Donations heading + links, Close.

Do not put Donate in the TopAppBar or on the update dialog (`docs/features/donations-updates.md`).

---

## 7. Theming tokens

Edit only `design-tokens/design-tokens.json`; run `scripts/sync-design-tokens.py`. No raw hex in UI source.

| Role | Light | Dark |
|------|-------|------|
| Primary | `#3D5A6C` | `#8FB4C7` |
| Secondary | `#5C6B5A` | `#A8B6A4` |
| Tertiary | `#6A5E52` | `#C9B8A6` |
| Background | `#F7F6F2` | `#12161A` |
| Surface | `#F1F0EB` | `#1A1F24` |
| On background | `#1A1F24` | `#E6E4DC` |
| Error | `#BA1A1A` | `#FFB4AB` |

**Type:** system-ui / sans-serif. titleLarge 22sp, headlineMedium 28sp, bodyLarge 16sp, bodyMedium 14sp, labelLarge 14sp/500.

**Space:** 4 / 8 / 16 / 24 / 32. Radius 4 / 8 / 16. Elevation 0 / 1 / 3.

**Theme:** default `system`. Persist per device. Contrast already gated (`check-token-contrast`).

Touch targets ≥ 44px. Logical start/end alignment. Snackbars not Toasts (`DESIGN_GUIDE.md`).

---

## 8. Behavior rules

| Rule | Detail |
|------|--------|
| Local-first | Vault works with no account; LAN/Hermes optional |
| Empty states | Honest copy + next action (`pane_*_body`, `filter_empty`, add-feed FAB). Demo booru posts are the **existing** first-cut data, not a new fake catalog |
| Loading | In-place; do not block the app for a local save |
| Errors | Stay on the screen; snackbar confirms (update available) |
| Offline | Settings and library load from disk; feed refresh skips when offline |
| Back | Reader and booru detail consume back; Settings/About Close buttons remain |
| Add-feed | Same dialog on **every** OS (decision 13). News kind refreshes HTTP; podcast/booru kinds persist as today |
| Sync | LAN URL + passphrase; Sync now + resume. No Drive/WebDAV UI in v1 |
| Hermes | Optional; hide section where the OS cannot host a ContentProvider |
| Updates | Opt-in GitHub Releases check; Apply update uses the platform installer (APK / desktop package). Failed fetch stays silent |
| Notifications | Post unread/new-item notices using existing channel names `aetherfeed_news` / `aetherfeed_podcast` (create a booru channel only if we start posting booru events; first cut is demo-only — **do not add a new channel without cause**) |
| Widget | Unread **count only**, same string `Unread: %d` |
| Booru | Demo JSON grid only; no live search UI beyond what the demo screen already has |
| Out of scope | Biometrics, OPML button, search field, download manager, Gelbooru, F-Droid id rename |

---

## 9. Platform conventions (allowed without a Deviation)

These are OS chrome, not product redesigns.

| Platform | Allowed |
|----------|---------|
| iOS | Interactive pop / edge-swipe for reader and overlays; safe-area insets; no Android 3-button debug line on About (debug-only today — keep Android-debug only) |
| iOS | `POST_NOTIFICATIONS` analogue: system permission prompt on first post |
| iOS / macOS | WidgetKit presentation (home screen / Notification Center / desktop widgets) using the **same count copy** |
| Desktop | Native window controls; optional menu: Settings, About, Quit — items open the **same overlays** |
| Desktop | Tray tooltip `AetherFeed — {n} unread` (already the Android/desktop contract) |
| Desktop | “Custom tab” opener maps to the default browser (no Chrome Custom Tabs) |
| Linux | libsecret / tray may no-op with an in-app status, not a new Settings page |

---

## 10. Deviations

_None._ No visual or IA change is proposed. Native must-haves (player bar, notifications, widgets, keychain) reuse existing components and copy.

---

## 11. Out of scope for this brief

Redesign, new brand, extra destinations, hosted PWA, Drive/WebDAV settings, biometric lock, F-Droid metadata rename, new locales, live booru sources, OPML/export buttons, full-text search.
