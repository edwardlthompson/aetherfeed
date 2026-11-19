# Parity checklist — AetherFeed multiplatform

> Phase 3 gate artifact. Binding SoT: [`INVENTORY.md`](INVENTORY.md), [`DESIGN_BRIEF.md`](DESIGN_BRIEF.md), [`TECH_DECISION.md`](TECH_DECISION.md), [ADR-0013](../adr/0013-compose-multiplatform.md).
>
> **Android (current)** seeded from INVENTORY (WORKING→Parity, PARTIAL→Partial, STUB/LOGIC-ONLY→Partial, MISSING→Pending). Other platforms start `Pending` unless `N/A (reason)`.
>
> Statuses: `Parity` · `Partial` · `Pending` · `N/A (reason)`. Hardware-only checks: tag Notes with `[HUMAN]`.
>
> Update cells when a Multiplatform Migration sprint parity gate passes. Must-haves (decisions 7–11, 13, 19): lockscreen audio, real notifications, UnifiedPush where available, iOS unread widget, Add-feed on desktop, vault+keychain, LAN+optional Hermes; booru demo-only. Version stays `0.1.0`.

## 1. Navigation / chrome

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Launch → AetherFeedTheme (system/light/dark, default system) | Parity | Pending | Pending | Pending | Pending | Pending | DESIGN_BRIEF §3 |
| TopAppBar title AetherFeed + Settings gear | Parity | Pending | Pending | Pending | Pending | Pending | |
| Bottom bar News / Podcasts / Booru (tab persisted) | Parity | Pending | Pending | Pending | Pending | Pending | Do not replace with iOS top TabView |
| FAB Add subscription on main shell only | Parity | Pending | Pending | Pending | Pending | Pending | Decision 13 — every OS |
| Settings overlay (not a tab) | Parity | Pending | Pending | Pending | Pending | Pending | |
| About overlay from Settings → About | Parity | Pending | Pending | Pending | Pending | Pending | |
| No fourth bottom destination / no URL router | Parity | Pending | Pending | Pending | Pending | Pending | |
| Desktop window 1100×760, title AetherFeed | N/A (mobile) | N/A (mobile) | N/A (mobile) | Pending | Pending | Pending | DESIGN_BRIEF §2 |
| Desktop menu Settings/About/Quit → same overlays | N/A (mobile) | N/A (mobile) | N/A (mobile) | Pending | Pending | Pending | DESIGN_BRIEF §9 |

## 2. Screens

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| App root (theme, update check, donations, DI) | Parity | Pending | Pending | Pending | Pending | Pending | INVENTORY §1.5 |
| Main shell (vault lists + demo booru inject) | Partial | Pending | Pending | Pending | Pending | Pending | Hard-coded demo booru posts |
| News list (filters, star, open) | Parity | Pending | Pending | Pending | Pending | Pending | |
| Article reader (Reading/Full/Web, swipe, park unread) | Parity | Pending | Pending | Pending | Pending | Pending | |
| Podcasts list / empty body | Partial | Pending | Pending | Pending | Pending | Pending | STUB UI — no player composed |
| Compact podcast player bar (shared UI) | Pending | Pending | Pending | Pending | Pending | Pending | Must-have migration chrome; DESIGN_BRIEF §4 |
| Booru grid/detail (demo JSON) | Partial | Pending | Pending | Pending | Pending | Pending | Demo-only decision 8 |
| Settings screen | Parity | Pending | Pending | Pending | Pending | Pending | Field order DESIGN_BRIEF §6 |
| About screen | Parity | Pending | Pending | Pending | Pending | Pending | Export hint only |
| Add subscription dialog | Parity | Pending | Pending | Pending | Pending | Pending | Web/Tauri lacked Add-feed |
| Scaffold / insets / snackbar | Parity | Pending | Pending | Pending | Pending | Pending | |
| ThemeToggle composable as product chrome | N/A (unused template) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | DESIGN_BRIEF §4 |
| ModulePane legacy | N/A (unreachable) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | |

## 3. News

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| RSS 2.0 / Atom / JSON Feed 1.1 parse | Parity | Pending | Pending | Pending | Pending | Pending | Shared Kotlin |
| HTTP refresh (news kind, 12-feed/12s, UA AetherFeed/1) | Parity | Pending | Pending | Pending | Pending | Pending | Ktor inherits timeouts |
| Offline article bodies | Parity | Pending | Pending | Pending | Pending | Pending | |
| Unread / read / star | Parity | Pending | Pending | Pending | Pending | Pending | |
| Reader modes Reading/Full/Web | Parity | Pending | Pending | Pending | Pending | Pending | |
| Open in browser / custom tab | Partial | Pending | Pending | Pending | Pending | Pending | Desktop maps custom tab → default browser |
| Browse filters all/unread/starred + sort | Parity | Pending | Pending | Pending | Pending | Pending | |
| Tags attach UX | Partial | Pending | Pending | Pending | Pending | Pending | Schema LOGIC-ONLY; after shells (decision 17) |
| OPML import/export UI | Pending | Pending | Pending | Pending | Pending | Pending | After shells (decision 17) |
| Full-text search | Pending | Pending | Pending | Pending | Pending | Pending | After shells (decision 17) |
| Edit/delete/organize feed folders | Partial | Pending | Pending | Pending | Pending | Pending | URL-derived tree today |

## 4. Podcasts

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Subscribe kind Podcast (persist feed) | Partial | Pending | Pending | Pending | Pending | Pending | Android does not HTTP-refresh podcast RSS yet |
| Episode parse / show list | Partial | Pending | Pending | Pending | Pending | Pending | |
| Play/pause/seek/speed in-app | Partial | Pending | Pending | Pending | Pending | Pending | Logic-only on Android; must ship player bar |
| Persist playback position | Partial | Pending | Pending | Pending | Pending | Pending | Schema exists; UI must drive it |
| Lockscreen / MediaSession / Now Playing | Pending | Pending | Pending | N/A (best-effort desktop) | N/A (best-effort desktop) | N/A (best-effort desktop) | Decision 7 must-have Android+iOS |
| Desktop in-window player bar | N/A (mobile) | N/A (mobile) | N/A (mobile) | Pending | Pending | Pending | Enough if OS Now Playing fragile (TECH §6) |
| Queue / chapters | Pending | Pending | Pending | Pending | Pending | Pending | After shells (decision 17) |
| Real enclosure downloads | Partial | Pending | Pending | Pending | Pending | Pending | Stub empty files today; after shells |
| Car / Android Auto | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | Feature lock |

## 5. Booru

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Demo grid/detail/favorite chrome | Partial | Pending | Pending | Pending | Pending | Pending | Decision 8 demo-only |
| Live Danbooru/Gelbooru HTTP search UI | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | After shells / Edward policy |
| Vault-backed favorites | Partial | Pending | Pending | Pending | Pending | Pending | Demo uses in-memory stars |
| Blacklists / saved searches / downloads | Pending | Pending | Pending | Pending | Pending | Pending | After shells |
| Neutral Booru branding | Parity | Pending | Pending | Pending | Pending | Pending | |
| Booru notification channel | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | DESIGN_BRIEF §8 |

## 6. Vault / security

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Encrypted DB (SQLCipher-class) | Parity | Pending | Pending | Pending | Pending | Pending | ADR-0002; SQLDelight target |
| OS keychain / keystore for secrets | Parity | Pending | Pending | Pending | Pending | Pending | Decision 9 |
| LAN passphrase in OS secret store (not plaintext prefs) | Parity | Pending | Pending | Pending | Pending | Pending | |
| Forgotten passphrase unrecoverable honesty | Parity | Pending | Pending | Pending | Pending | Pending | |
| No plaintext SQLite fallback | Parity | Pending | Pending | Pending | Pending | Pending | TECH §4 fragile rule |
| Biometric vault lock | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | |
| allowBackup false / equivalent | Parity | Pending | Pending | Pending | Pending | Pending | Platform actuals |

## 7. Settings / About / prefs

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Theme chips System/Light/Dark | Parity | Pending | Pending | Pending | Pending | Pending | Order DESIGN_BRIEF §6 |
| Check for updates switch | Parity | Pending | Pending | Pending | Pending | Pending | |
| Preferred opener chips + copy | Partial | Pending | Pending | Pending | Pending | Pending | |
| Text size chips | Parity | Pending | Pending | Pending | Pending | Pending | |
| Article images Always/Wi-Fi/Never | Parity | Pending | Pending | Pending | Pending | Pending | |
| Hermes status (read-only) | Parity | Pending | N/A (no ContentProvider) | N/A (no ContentProvider) | N/A (no ContentProvider) | N/A (no ContentProvider) | Hide off-Android |
| Home LAN pool URL/passphrase/status/Save/Sync now | Parity | Pending | Pending | Pending | Pending | Pending | Hub host is desktop-only |
| About entry + Close | Parity | Pending | Pending | Pending | Pending | Pending | |
| About: version / format / update apply | Parity | Pending | Pending | Pending | Pending | Pending | |
| About: export hint (no button) | Partial | Pending | Pending | Pending | Pending | Pending | Logic-only export |
| About: donations links | Parity | Pending | Pending | Pending | Pending | Pending | If config present |
| Browse filter prefs per module | Parity | Pending | Pending | Pending | Pending | Pending | |
| Active tab persistence | Parity | Pending | Pending | Pending | Pending | Pending | |
| Save-crashes UI | Partial | Pending | Pending | Pending | Pending | Pending | TEMPLATE/unwired |
| Drive/WebDAV credentials UI | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | Decision 5 |
| Notification channel toggles UI | Pending | Pending | Pending | Pending | Pending | Pending | After shells |

## 8. Sync

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Pool document v2 + LWW | Parity | Pending | Pending | Pending | Pending | Pending | ADR-0011 |
| SyncEnvelope crypto | Parity | Pending | Pending | Pending | Pending | Pending | Shared Kotlin |
| LAN HTTP client (Settings fields unchanged) | Parity | Pending | Pending | Pending | Pending | Pending | |
| LAN hub host GET/PUT /v1/pool + HMAC | N/A (client) | N/A (client) | N/A (client) | Partial | Pending | Pending | Tauri hub today on Windows; CMP desktop target |
| Hermes ContentProvider client | Parity | Pending | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | Decision 6 optional |
| Drive AppData / WebDAV production DI | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | Decision 5 |
| mDNS / QR pairing | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | |

## 9. Notifications / widgets / tray

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Notification channels news/podcast | Partial | Pending | Pending | Pending | Pending | Pending | Channels exist; posting missing |
| Real notification posting | Pending | Pending | Pending | Pending | Pending | Pending | Decision 10 must-have |
| UnifiedPush distributor | Pending | Pending | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | |
| iOS local UserNotifications (no APNs) | N/A (Android) | N/A (Android) | Pending | N/A (iOS) | N/A (iOS) | N/A (iOS) | Decision 19 |
| APNs / remote push | N/A (deferred) | N/A (deferred) | N/A (deferred decision 19) | N/A (deferred) | N/A (deferred) | N/A (deferred) | `[HUMAN]` Apple Developer later |
| Android unread home widget (count only) | Parity | Pending | N/A (Android) | N/A (Android) | N/A (Android) | N/A (Android) | `Unread: N` |
| iOS WidgetKit unread count (App Group file) | N/A (iOS) | N/A (iOS) | Pending | N/A (iOS) | N/A (iOS) | N/A (iOS) | Decision 11; `[HUMAN]` on-device |
| macOS WidgetKit unread | N/A (mac) | N/A (mac) | N/A (mac) | N/A (mac) | Pending | N/A (mac) | Only if App Group cheap; else N/A fragile |
| Desktop tray tooltip AetherFeed — {n} unread | N/A (mobile) | N/A (mobile) | N/A (mobile) | Partial | Pending | Pending | Tauri command unused from TS today |
| Background refresh OS scheduler / resume | Partial | Pending | Pending | Pending | Pending | Pending | Decision 18; iOS BGAppRefresh best-effort |

## 10. Updates / donations / export

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Opt-in GitHub Releases update check | Parity | Pending | Pending | Pending | Pending | Pending | |
| Apply update (platform installer) | Parity | Pending | Pending | Pending | Pending | Pending | APK vs desktop package vs sideload ipa |
| Donations links in About | Parity | Pending | Pending | Pending | Pending | Pending | |
| Library JSON export button | Pending | Pending | Pending | Pending | Pending | Pending | Hint only today; after shells |
| Public hosted PWA / aetherfeed.org reader | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | Marketing only |

## 11. Packaging / CI

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| GitHub Release APK/AAB | Partial | Pending | N/A (Android) | N/A (Android) | N/A (Android) | N/A (Android) | Keep version 0.1.0 |
| Windows MSI/exe (CMP/jpackage) | N/A (Win) | N/A (Win) | N/A (Win) | Pending | N/A (Win) | N/A (Win) | Replaces Tauri NSIS/MSI |
| macOS dmg/app | N/A (mac) | N/A (mac) | N/A (mac) | N/A (mac) | Pending | N/A (mac) | |
| Linux AppImage/deb | N/A (linux) | N/A (linux) | N/A (linux) | N/A (linux) | N/A (linux) | Pending | |
| Unsigned iOS .ipa (AltStore/SideStore) | N/A (iOS) | N/A (iOS) | Pending | N/A (iOS) | N/A (iOS) | N/A (iOS) | Decision 19 |
| iOS simulator CI build/tests | N/A (iOS) | N/A (iOS) | Pending | N/A (iOS) | N/A (iOS) | N/A (iOS) | No on-device in CI |
| On-device iOS verification | N/A (iOS) | N/A (iOS) | Pending | N/A (iOS) | N/A (iOS) | N/A (iOS) | `[HUMAN]` — no Apple device |
| TestFlight / App Store | N/A (deferred) | N/A (deferred) | N/A (deferred decision 19) | N/A (deferred) | N/A (deferred) | N/A (deferred) | `[HUMAN]` later |
| Release workflows only on v* / workflow_dispatch | Pending | Pending | Pending | Pending | Pending | Pending | No new push/PR/schedule jobs |
| F-Droid id / signing rebrand | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | |

## 12. Out of scope for first shells (decision 17)

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Full-text search UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| OPML button UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| Gelbooru / live booru sources | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| Jazz/Subduction sync | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | |
| Dual-ship Tauri + CMP desktop | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | TECH §2 |
| examples/web as product surface | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | KB-018 |
