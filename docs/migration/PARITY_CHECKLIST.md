# Parity checklist — AetherFeed multiplatform

> Phase 3 gate artifact. Binding SoT: [`INVENTORY.md`](INVENTORY.md), [`DESIGN_BRIEF.md`](DESIGN_BRIEF.md), [`TECH_DECISION.md`](TECH_DECISION.md), [ADR-0013](../adr/0013-compose-multiplatform.md).
>
> **Android (current)** seeded from INVENTORY (WORKING→Parity, PARTIAL→Partial, STUB/LOGIC-ONLY→Partial, MISSING→Pending). Other platforms start `Partial` (stub/shell) or `N/A (reason)` unless parity is proven.
>
> Statuses: `Parity` · `Partial` · `Pending [HUMAN]` · `N/A (reason)`. Hardware-only / store checks: use `Pending [HUMAN]` in the status cell and cite in Notes. No bare `Pending` on must-haves (decisions 7–11, 13, 19).
>
> Update cells when a Multiplatform Migration sprint parity gate passes. Must-haves (decisions 7–11, 13, 19): lockscreen audio, real notifications, UnifiedPush where available, iOS unread widget, Add-feed on desktop, vault+keychain, LAN+optional Hermes; booru demo-only. Version stays `0.1.0`.
>
> Sprint 57: `shared/ui` chrome+screens presentational port.
>
> Sprint 58: Android app thin-wires shared domain/ui (theme/chrome/Add-feed/Settings/About); Room+Hermes/Media3 stay androidMain; vault SQLDelight still Partial.
>
> Sprint 59: Compose Desktop shell + Win MSI/exe packaged; macOS/Linux Partial until host builds; Tauri frozen.
>
> Sprint 60: `examples/ios/` CMP skeleton + WidgetKit/AVFoundation/UserNotifications **contracts** only — iOS UI is **Partial**, not Parity (no simulator-built product chrome; Edward has no Apple device — decision 19).
>
> Sprint 61: Real local notification posting on Android + desktop stub; UnifiedPush connector registration (FOSS distributor API); shared notify contracts; Hermes Settings block hidden off-Android via `showHermesBlock`; iOS LAN/notify JVM stubs; Drive/WebDAV stay out of Hilt DI.
>
> Sprint 62: `docs/RELEASE_ARTIFACTS.md` + `scripts/check-release-artifact-matrix.sh`; `release.yml` workflow_dispatch dry-run prints expected extensions; Android APK/AAB on `v1.13.4`; desktop/iOS attach still `Pending [HUMAN]`; no new packaging schedules.
>
> Sprint 63: Final parity walk — all must-have cells are `Parity`, `Partial` (noted), `N/A (reason)`, or `Pending [HUMAN]` only; decision-17 deferrals marked `N/A (after shells)`; device matrix sign-off backlogged (`HUMAN_BACKLOG.md`).

## 1. Navigation / chrome

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Launch → AetherFeedTheme (system/light/dark, default system) | Parity | Parity | Partial | Parity | Partial | Partial | Sprint 60 JVM stub — not simulator UI |
| TopAppBar title AetherFeed + Settings gear | Parity | Parity | Partial | Parity | Partial | Partial | Sprint 60 stub text only |
| Bottom bar News / Podcasts / Booru (tab persisted) | Parity | Parity | Partial | Parity | Partial | Partial | Do not replace with iOS top TabView |
| FAB Add subscription on main shell only | Parity | Parity | Partial | Parity | Partial | Partial | Decision 13 — dialog composable stub |
| Settings overlay (not a tab) | Parity | Parity | Partial | Parity | Partial | Partial | Full overlay not wired on iOS/macOS/Linux |
| About overlay from Settings → About | Parity | Parity | Partial | Parity | Partial | Partial | |
| No fourth bottom destination / no URL router | Parity | Parity | Partial | Parity | Partial | Partial | |
| Desktop window 1100×760, title AetherFeed | N/A (mobile) | N/A (mobile) | N/A (mobile) | Parity | Partial | Partial | DESIGN_BRIEF §2 |
| Desktop menu Settings/About/Quit → same overlays | N/A (mobile) | N/A (mobile) | N/A (mobile) | Partial | Partial | Partial | DESIGN_BRIEF §9 |
## 2. Screens

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| App root (theme, update check, donations, DI) | Parity | Parity | Partial | Partial | Partial | Partial | INVENTORY §1.5; desktop/iOS composition roots stubbed |
| Main shell (vault lists + demo booru inject) | Partial | Partial | Partial | Partial | Partial | Partial | Hard-coded demo booru posts |
| News list (filters, star, open) | Parity | Parity | Partial | Partial | Partial | Partial | Desktop `DestinationBody` stub until vault cutover |
| Article reader (Reading/Full/Web, swipe, park unread) | Parity | Parity | Partial | Partial | Partial | Partial | |
| Podcasts list / empty body | Partial | Partial | Partial | Partial | Partial | Partial | STUB UI — no player composed |
| Compact podcast player bar (shared UI) | Partial | Partial | Partial | Partial | Partial | Partial | Must-have migration chrome; DESIGN_BRIEF §4; Android logic-only today |
| Booru grid/detail (demo JSON) | Partial | Partial | Partial | Partial | Partial | Partial | Demo-only decision 8 |
| Settings screen | Parity | Parity | Partial | Parity | Partial | Partial | Field order DESIGN_BRIEF §6 |
| About screen | Parity | Parity | Partial | Parity | Partial | Partial | Export hint only |
| Add subscription dialog | Parity | Parity | Partial | Parity | Partial | Partial | Sprint 60 `IosAddFeedDialog` stub; decision 13 |
| Scaffold / insets / snackbar | Parity | Parity | Partial | Partial | Partial | Partial | |
| ThemeToggle composable as product chrome | N/A (unused template) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | DESIGN_BRIEF §4 |
| ModulePane legacy | N/A (unreachable) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | N/A (do not resurrect) | |
## 3. News

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| RSS 2.0 / Atom / JSON Feed 1.1 parse | Parity | Partial | Partial | Partial | Partial | Partial | shared/domain models + NewsHttp contract; parsers still in app until cutover |
| HTTP refresh (news kind, 12-feed/12s, UA AetherFeed/1) | Parity | Partial | Partial | Partial | Partial | Partial | Constants locked in shared/domain; Ktor client next |
| Offline article bodies | Parity | Partial | Partial | Partial | Partial | Partial | Room runtime on Android; SQLDelight target Partial |
| Unread / read / star | Parity | Partial | Partial | Partial | Partial | Partial | |
| Reader modes Reading/Full/Web | Parity | Partial | Partial | Partial | Partial | Partial | |
| Open in browser / custom tab | Partial | Partial | Partial | Partial | Partial | Partial | Desktop maps custom tab → default browser |
| Browse filters all/unread/starred + sort | Parity | Partial | Partial | Partial | Partial | Partial | |
| Tags attach UX | Partial | Partial | Partial | Partial | Partial | Partial | Schema LOGIC-ONLY; after shells (decision 17) |
| OPML import/export UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | Decision 17 |
| Full-text search | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | Decision 17 |
| Edit/delete/organize feed folders | Partial | Partial | Partial | Partial | Partial | Partial | URL-derived tree today |
## 4. Podcasts

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Subscribe kind Podcast (persist feed) | Partial | Partial | Partial | Partial | Partial | Partial | Android does not HTTP-refresh podcast RSS yet |
| Episode parse / show list | Partial | Partial | Partial | Partial | Partial | Partial | |
| Play/pause/seek/speed in-app | Partial | Partial | Partial | Partial | Partial | Partial | Logic-only on Android; must ship player bar |
| Persist playback position | Partial | Partial | Partial | Partial | Partial | Partial | Schema exists; UI must drive it |
| Lockscreen / MediaSession / Now Playing | Partial | Partial | Partial | N/A (best-effort desktop) | N/A (best-effort desktop) | N/A (best-effort desktop) | Decision 7 must-have on mobile; iOS AVFoundation doc + contract (Sprint 60) |
| Desktop in-window player bar | N/A (mobile) | N/A (mobile) | N/A (mobile) | Partial | Partial | Partial | Enough if OS Now Playing fragile (TECH §6); decision 7 desktop fallback |
| Queue / chapters | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | Decision 17 |
| Real enclosure downloads | Partial | Partial | Partial | Partial | Partial | Partial | Stub empty files today; after shells for production cutover |
| Car / Android Auto | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | N/A (non-goal) | Feature lock |
## 5. Booru

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Demo grid/detail/favorite chrome | Partial | Partial | Partial | Partial | Partial | Partial | Decision 8 demo-only |
| Live Danbooru/Gelbooru HTTP search UI | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | N/A (demo-only cut) | After shells / Edward policy |
| Vault-backed favorites | Partial | Partial | Partial | Partial | Partial | Partial | Demo uses in-memory stars |
| Blacklists / saved searches / downloads | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | After shells |
| Neutral Booru branding | Parity | Partial | Partial | Partial | Partial | Partial | Shared UI strings |
| Booru notification channel | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | N/A (do not add without cause) | DESIGN_BRIEF §8 |
## 6. Vault / security

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Encrypted DB (SQLCipher-class) | Parity | Partial | Partial | Partial | Partial | Partial | ADR-0002; shared/vault schema SoT; Room still runtime on Android |
| OS keychain / keystore for secrets | Parity | Partial | Partial | Partial | Partial | Partial | iOS `expect/actual` InMemory until SecItem on Mac; decision 9 |
| LAN passphrase in OS secret store (not plaintext prefs) | Parity | Partial | Partial | Partial | Partial | Partial | Android Parity; desktop credential store Partial |
| Forgotten passphrase unrecoverable honesty | Parity | Partial | Partial | Partial | Partial | Partial | Copy in shared Settings |
| No plaintext SQLite fallback | Parity | Partial | Partial | Partial | Partial | Partial | TECH §4 fragile rule |
| Biometric vault lock | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | N/A (decision 12 later) | |
| allowBackup false / equivalent | Parity | Partial | Partial | Partial | Partial | Partial | Platform actuals |
## 7. Settings / About / prefs

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Theme chips System/Light/Dark | Parity | Partial | Partial | Partial | Partial | Partial | Order DESIGN_BRIEF §6; shared UI |
| Check for updates switch | Parity | Partial | Partial | Partial | Partial | Partial | |
| Preferred opener chips + copy | Partial | Partial | Partial | Partial | Partial | Partial | |
| Text size chips | Parity | Partial | Partial | Partial | Partial | Partial | |
| Article images Always/Wi-Fi/Never | Parity | Partial | Partial | Partial | Partial | Partial | |
| Hermes status (read-only) | Parity | Partial | N/A (no ContentProvider) | N/A (no ContentProvider) | N/A (no ContentProvider) | N/A (no ContentProvider) | Hidden off-Android (`showHermesBlock=false`) |
| Home LAN pool URL/passphrase/status/Save/Sync now | Parity | Partial | Partial | Partial | Partial | Partial | Android/desktop wired; iOS JVM client stub |
| About entry + Close | Parity | Partial | Partial | Partial | Partial | Partial | |
| About: version / format / update apply | Parity | Partial | Partial | Partial | Partial | Partial | |
| About: export hint (no button) | Partial | Partial | Partial | Partial | Partial | Partial | Logic-only export |
| About: donations links | Parity | Partial | Partial | Partial | Partial | Partial | If config present |
| Browse filter prefs per module | Parity | Partial | Partial | Partial | Partial | Partial | |
| Active tab persistence | Parity | Partial | Partial | Partial | Partial | Partial | |
| Save-crashes UI | Partial | Partial | Partial | Partial | Partial | Partial | TEMPLATE/unwired |
| Drive/WebDAV credentials UI | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | N/A (v1 LAN+Hermes only) | Decision 5 |
| Notification channel toggles UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | After shells |
## 8. Sync

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Pool document v2 + LWW | Parity | Partial | Partial | Partial | Partial | Partial | Android full; iOS/desktop stubs |
| SyncEnvelope crypto | Parity | Partial | Partial | Partial | Partial | Partial | SyncEnvelope type in shared/domain; codec bodies still in app/TS/Rust |
| LAN HTTP client (Settings fields unchanged) | Parity | Partial | Partial | Partial | Partial | Partial | Android `LanPoolSync`; desktop hub host; iOS stub |
| LAN hub host GET/PUT /v1/pool + HMAC | N/A (client) | N/A (client) | N/A (client) | Partial | Partial | Partial | CMP `LanHubHost` stub; Tauri frozen |
| Hermes ContentProvider client | Parity | Partial | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | Android-only optional |
| Drive AppData / WebDAV production DI | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | N/A (v1 deferred) | Decision 5 |
| mDNS / QR pairing | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | N/A (deferred ADR-0011) | |
## 9. Notifications / widgets / tray

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Notification channels news/podcast | Parity | Partial | N/A (no channels) | N/A (no channels) | N/A (no channels) | N/A (no channels) | News+podcast only; no booru |
| Real notification posting | Parity | Partial | Partial | Partial | Partial | Partial | Android `ArticleNotificationPoster`; desktop tray balloon; iOS JVM stub |
| UnifiedPush distributor | Partial | Partial | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | N/A (Android-only) | `connector:3.0.4` register + receiver; E2E `Pending [HUMAN]` distributor install |
| iOS local UserNotifications (no APNs) | N/A (Android) | N/A (Android) | Partial | N/A (iOS) | N/A (iOS) | N/A (iOS) | JVM poster; Darwin UNUserNotificationCenter pending iosMain |
| APNs / remote push | N/A (deferred) | N/A (deferred) | N/A (deferred decision 19) | N/A (deferred) | N/A (deferred) | N/A (deferred) | `Pending [HUMAN]` Apple Developer later |
| Android unread home widget (count only) | Parity | Partial | N/A (Android) | N/A (Android) | N/A (Android) | N/A (Android) | `Unread: N` |
| iOS WidgetKit unread count (App Group file) | N/A (iOS) | N/A (iOS) | Partial | N/A (iOS) | N/A (iOS) | N/A (iOS) | JSON contract + Swift placeholder; on-device `Pending [HUMAN]` |
| macOS WidgetKit unread | N/A (mac) | N/A (mac) | N/A (mac) | N/A (mac) | N/A (App Group not shipped) | N/A (mac) | TECH §6 — skip rather than custom redesign |
| Desktop tray tooltip AetherFeed — {n} unread | N/A (mobile) | N/A (mobile) | N/A (mobile) | Parity | Partial | Partial | Compose Desktop `TrayUnread`; macOS/Linux Partial until host build |
| Background refresh OS scheduler / resume | Partial | Partial | Partial | Partial | Partial | Partial | iOS policy stub + BGAppRefresh notes |
## 10. Updates / donations / export

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Opt-in GitHub Releases update check | Parity | Partial | Partial | Partial | Partial | Partial | |
| Apply update (platform installer) | Parity | Partial | Partial | Partial | Partial | Partial | APK vs desktop package vs sideload ipa |
| Donations links in About | Parity | Partial | Partial | Partial | Partial | Partial | |
| Library JSON export button | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | Hint only today; after shells |
| Public hosted PWA / aetherfeed.org reader | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | N/A (KB-018 / decision 16) | Marketing only |
## 11. Packaging / CI

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| GitHub Release APK/AAB | Partial | Partial | N/A (Android) | N/A (Android) | N/A (Android) | N/A (Android) | Shipped `v1.13.4`; About `0.1.0`; `release.yml` SBOM-only auto |
| Windows MSI/exe (CMP/jpackage) | N/A (Win) | N/A (Win) | N/A (Win) | Partial | N/A (Win) | N/A (Win) | Local jpackage OK; Release attach `Pending [HUMAN]` |
| macOS dmg/app | N/A (mac) | N/A (mac) | N/A (mac) | N/A (mac) | Pending [HUMAN] | N/A (mac) | Needs Mac host build + signing |
| Linux AppImage/deb | N/A (linux) | N/A (linux) | N/A (linux) | N/A (linux) | N/A (linux) | Pending [HUMAN] | Host build only |
| Unsigned iOS .ipa (AltStore/SideStore) | N/A (iOS) | N/A (iOS) | Partial | N/A (iOS) | N/A (iOS) | N/A (iOS) | `PACKAGING.md`; export `Pending [HUMAN]` on Mac |
| iOS simulator CI build/tests | N/A (iOS) | N/A (iOS) | Partial | N/A (iOS) | N/A (iOS) | N/A (iOS) | Gradle KMP; link on Mac CI only |
| On-device iOS verification | N/A (iOS) | N/A (iOS) | Pending [HUMAN] | N/A (iOS) | N/A (iOS) | N/A (iOS) | No Apple device (decision 19) |
| TestFlight / App Store | N/A (deferred) | N/A (deferred) | Pending [HUMAN] | N/A (deferred) | N/A (deferred) | N/A (deferred) | Decision 19 — `HUMAN_BACKLOG.md` |
| Release workflows only on v* / workflow_dispatch | Parity | Parity | Parity | Parity | Parity | Parity | `release.yml`; matrix check on dry-run |
| Release artifact matrix doc + gate | Parity | Parity | Parity | Parity | Parity | Parity | `docs/RELEASE_ARTIFACTS.md`; no binaries in git |
| F-Droid id / signing rebrand | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | N/A (decision 15 later) | `Pending [HUMAN]` — `HUMAN_BACKLOG.md` |
## 12. Out of scope for first shells (decision 17)

| Feature | Android (current) | Android (KMP) | iOS | Windows | macOS | Linux | Notes |
|---------|-------------------|---------------|-----|---------|-------|-------|-------|
| Full-text search UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| OPML button UI | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| Gelbooru / live booru sources | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | N/A (after shells) | |
| Jazz/Subduction sync | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | N/A (ADR-0012) | |
| Dual-ship Tauri + CMP desktop | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | N/A (forbidden) | TECH §2 |
| examples/web as product surface | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | N/A (gates only) | KB-018 |