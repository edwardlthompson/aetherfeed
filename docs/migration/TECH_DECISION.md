# Tech decision — AetherFeed multiplatform

> Phase 2. Stack choice for Edward’s 2026-11-16 decisions and [`DESIGN_BRIEF.md`](DESIGN_BRIEF.md). **No app code in this phase.** Phase 3 (BUILD_PLAN + move) waits on review.
>
> Superseding architecture ADR: [`docs/adr/0013-compose-multiplatform.md`](../adr/0013-compose-multiplatform.md). ADR-0001 is kept as history.

---

## 1. Choice (one stack)

**Kotlin Multiplatform + Compose Multiplatform (Material 3)** for Android, iOS, Windows, macOS, and Linux.

| Layer | Choice |
|-------|--------|
| UI | Shared Compose (`AetherFeedTheme` + existing screens) |
| Domain | Shared Kotlin (today’s `org.aetherfeed.app.domain` / `news` / `sync` LAN client) |
| Vault | SQLDelight + SQLCipher-class encryption on every native target |
| Secrets | OS keychain / keystore via `expect`/`actual` (Android existing store; iOS Keychain; desktop OS credential store) |
| HTTP | Ktor (replace `HttpURLConnection` in shared news refresh) |
| LAN hub | Ktor server in **desktop** process (replaces Tauri `lan_hub.rs`) |
| Crypto | Shared Kotlin (today’s Android Argon2id + XChaCha20/Tink path). Rust `aetherfeed-crypto` stays a test oracle, not the shipping desktop runtime |
| Android extras | Hilt at app edge, Media3 + `MediaSessionService`, UnifiedPush, Hermes ContentProvider, AppWidget |
| iOS extras | AVFoundation + Now Playing, WidgetKit + App Group (count only), **local** `UserNotifications` (no APNs), Keychain |
| Desktop extras | Compose for Desktop window, tray, notifications, jpackage/Gradle native bundles |
| Not shipping | Public PWA, Tauri WebView UI, Drive/WebDAV v1, Electron |

This is the only option that keeps the Android Compose look (brief), one UI (decision 3), iOS (decision 1), three desktop OSes (decision 2), encrypted vault (decision 9), and lockscreen audio (decision 7) without a second design language.

---

## 2. Why not keep Tauri

Tauri + `examples/web` is the current Windows shell. Keeping it **alongside** CMP would be two product UIs (forbidden). Keeping it **as** the desktop UI would:

- Fork the look from Android Compose (already drifted: header theme toggle, stub status, no Add-feed).
- Leave iOS unsolved.
- Make SQLCipher + OS keychain and MediaSession/Now Playing WebView workarounds.

**Replace Tauri as the shipping desktop shell.** Do not dual-ship NSIS-from-Tauri and jpackage-from-CMP. Existing `examples/desktop` Rust (LAN hub, `http_get_text`, playback stub, updater pubkey empty) is a behavior reference for porting, then freeze.

`examples/web` remains in-tree for template gates (Playwright, Lighthouse, i18n parity) but is **not** a product surface (KB-018, decision 4/16). Stop adding product features there.

`shared/typescript` stays a **wire-format golden test** for pool v2 / `SyncEnvelope` until Kotlin tests own that contract.

---

## 3. Shared module layout (target)

No code move until Phase 3. Intended Gradle/Xcode shape:

```
shared/domain/          # KMP: models, unread, parsers, LAN client, envelope crypto
shared/vault/           # SQLDelight schema (= today’s Room entities)
shared/ui/              # CMP: theme, chrome, screens, Add-feed, Settings, About
examples/android/       # Application, Hilt, Hermes actual, Media3, widget, UnifiedPush, FileProvider updates
examples/ios/           # iOS app target + WidgetKit extension + audio/notifications actuals
examples/desktop/       # Compose Desktop app: window, tray, LAN hub host, installers
examples/web/           # frozen non-product (gates only)
examples/rust/          # optional crypto oracle
shared/typescript/      # envelope tests until replaced
```

`applicationId` / desktop identifier stay `org.aetherfeed.app`. Version stays `0.1.0` / `versionCode` 1 (decision 14). F-Droid `dev.foss.goldenpath` rename is later (decision 15).

**How today’s Android code moves**

| Today | Tomorrow |
|-------|----------|
| `domain/`, `news/` parsers, `NewsFeedRefresh`, `export/` | `shared/domain` (Ktor timeouts 12s, UA `AetherFeed/1`) |
| `data/` Room + `SqlCipherVault` + DAOs | SQLDelight in `shared/vault`; same tables (`feeds`, `articles`, `article_bodies`, `read_states`, `stars`, `tags`, `playback`) |
| `VaultPassphraseStore` | `actual` keystore |
| `ui/` composables | `shared/ui` (expect: back handler, WebView/Custom Tabs, file/share) |
| `sync/LanPool*` + crypto | shared client; hub **desktop actual** |
| `sync/SubscriptionPool*` | `androidMain` only |
| `DriveAppDataProvider` / `WebDavProvider` | stay unused in v1 (decision 5) |
| `podcasts/PodcastPlayer` | shared commands; Media3 / AVPlayer actuals |
| `notify/` widget + channels | shared unread math; posting/widget actuals |
| `about/` update check | shared fetch; apply-update actuals (APK vs desktop package) |
| Hilt modules | Android app; other platforms use a small manual composition root (≤10 lines per feature, same rule) |

Do not force Hilt or Room into commonMain.

---

## 4. Encrypted storage per platform

ADR-0002 still applies: queryable encrypted DB + secrets in the OS store; forgotten LAN passphrase is unrecoverable.

| Platform | DB | Key material |
|----------|----|--------------|
| Android | SQLDelight driver + SQLCipher (same role as today’s Room `SqlCipherVault`) | Keep EncryptedSharedPreferences / Android Keystore |
| iOS | SQLDelight NativeSqliteDriver linked to SQLCipher | iOS Keychain |
| Windows | SQLDelight + bundled SQLCipher native/JDBC | Credential Manager |
| macOS | same bundled SQLCipher | Keychain |
| Linux | same bundled SQLCipher | libsecret when present |

Prefs (theme, reader, LAN URL) can use SQLDelight or platform DataStore/`NSUserDefaults`/java.util.prefs — not worth a second encrypted DB. LAN passphrase **must** use the OS secret store, not plaintext prefs.

**Fragile — do not force:** unofficial SQLCipher JVM JARs on Linux distros. Prefer a **bundled native** SQLCipher for all three desktops. If a distro build cannot link it, fail the desktop vault open with the existing local-only honesty, do not silently fall back to plaintext SQLite.

Web/localStorage is not a vault. There is no shipping web vault.

---

## 5. iOS integration

| Need | Mechanism | Notes |
|------|-----------|--------|
| UI | CMP in UIViewController | Material 3, not SwiftUI screens |
| Back | Compose `BackHandler` + iOS interactive pop where the system owns the stack | Brief §9 |
| Vault | SQLCipher + Keychain | Decision 9 |
| Background audio / lockscreen | `AVAudioSession` playback category, `MPNowPlayingInfoCenter`, remote commands | Decision 7 — **must-have**. Map `PlaybackCommand` |
| Unread widget | WidgetKit + App Group | Write **integer count only** (same as Android widget). Do not decrypt the vault inside the extension if that is fragile — the app process updates a count file in the group |
| Notifications | **Local** `UserNotifications` only | **APNs deferred** (paid Apple Developer account). UnifiedPush is Android-only |
| Refresh | `BGAppRefresh` when the system allows; **always** refresh on foreground (decision 18) | iOS will not guarantee periodic fetch; no remote push wake |
| Subscribe | Shared Add-feed dialog | Decision 6/13 |
| Hermes | Not applicable | Hide Settings block |
| Biometrics | Skip | Decision 12 |
| CI | Simulator build + tests only | Edward has never owned an Apple device; on-device verification needs a tester or a later device |
| Distribute | Unsigned `.ipa` on GitHub Releases | Sideload via AltStore/SideStore. TestFlight/App Store = later `[HUMAN]` once an account exists |

### 5.1 Sideload vs paid-account capabilities

| Capability | Sideload (no paid account) | Plan |
|------------|----------------------------|------|
| Shared Compose UI, vault, LAN client, Add-feed | Yes | Ship |
| Background audio / Now Playing | Yes for sideloaded apps | Ship (decision 7) |
| WidgetKit unread count | Yes for sideloaded apps | Ship (decision 11); verify on a tester device when available |
| Local notifications | Yes | Ship |
| APNs / remote push | **No** — needs paid Developer account | **Deferred.** Do not design v1 around remote iOS push |
| TestFlight / App Store | **No** | Later `[HUMAN]` |
| Simulator CI | Yes | Required path for this migration |

Do not take a dependency on paid-account-only APIs without calling out this table.

---

## 6. Desktop packaging (Windows, macOS, Linux)

Compose for Desktop (JVM + Skiko) via Gradle:

| OS | Artifact |
|----|----------|
| Windows | MSI and/or exe (jpackage) — replaces Tauri NSIS/MSI as the shipping pair |
| macOS | `.dmg` / `.app` |
| Linux | AppImage and/or `.deb` (plus whatever Gradle already can emit without new product UI) |

Tray tooltip stays `AetherFeed — {n} unread` (wire `set_unread_total` from shared unread — today’s Tauri command is unused from TS). LAN hub binds private peers on `:8787` with the same `GET/PUT /v1/pool` + HMAC (ADR-0011). GitHub Releases update check reuses About logic; do not block on Tauri updater pubkey.

**Fragile — do not force:** Windows SMTC / macOS Now Playing / Linux MPRIS for podcasts. Android + iOS lockscreen is the must-have. Desktop media-key integration is best-effort; in-window player bar is enough if OS Now Playing is brittle. macOS WidgetKit: ship **if** the same App Group count file is cheap; skip rather than a custom WidgetKit redesign.

---

## 7. LAN sync and Hermes portability

| Path | Portability |
|------|-------------|
| Pool document v2 + LWW + `SyncEnvelope` | Shared Kotlin; keep tests vs today’s TS/Rust vectors |
| LAN HTTP client | Shared Ktor; Settings fields unchanged |
| LAN hub host | Desktop only (Windows/macOS/Linux). Mobile remains client |
| Hermes ContentProvider | **Android only.** Optional. Other OSes subscribe via Add-feed + LAN |
| Drive / WebDAV | Code may remain; **not** in production DI for v1 (decision 5) |

Do not invent mDNS/QR in this migration (ADR-0011 already deferred).

---

## 8. Notifications and background refresh

| OS | Post | Wake / schedule |
|----|------|-----------------|
| Android | `NotificationCompat` on existing channels | UnifiedPush distributor if the user has one; WorkManager when reliable; resume otherwise |
| iOS | Local `UNNotification` only (no APNs) | BGAppRefresh best-effort; resume always. Remote push deferred until a paid Apple Developer account |
| Desktop | OS notify API | In-process timer while running; resume always |

Do not add FCM, Play Services, or APNs. iOS push via APNs waits on a paid Apple Developer account. Do not post booru notifications in the demo-only cut.

---

## 9. What stays platform-specific (`actual` / app module)

- Hermes pool client and signature permission
- UnifiedPush registration
- Media3 `MediaSessionService` vs AVPlayer vs desktop audio backend
- AppWidget vs WidgetKit vs tray
- FileProvider APK apply vs desktop installer apply
- Edge-to-edge / nav-mode debug on Android About
- Optional libsecret / Credential Manager failures

---

## 10. Risks and “do not force if fragile”

| Risk | Rule |
|------|------|
| CMP iOS Material gaps | Keep layout; allow tiny inset/back differences only (§9 of the brief). No SwiftUI rewrite mid-migration |
| SQLCipher on JVM/Linux | Bundled native; never plaintext fallback |
| Desktop Now Playing | Best-effort; do not block the shell |
| macOS widgets | Only if App Group count file is straightforward |
| iOS vault in WidgetKit | Count file, not SQLCipher in the extension |
| Ktor hub vs Rust hub | Match the **wire** tests; do not require bit-identical Rust binary |
| Hilt/Room in commonMain | Do not force |
| Deleting `examples/web` | Do not, until template gates are rehomed |
| Dual desktop (Tauri + CMP) | Do not ship both |
| Jazz/Subduction, Drive UI, biometrics, Gelbooru, OPML button | Out of this migration |
| APNs / TestFlight / App Store | Deferred until `[HUMAN]` has an Apple Developer account; CI stays simulator-only |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Shared parsers keep `NewsParseError`; vault open failure → local-only UI, no crash-loop |
| Network timeout | 12s feed / 10s GitHub update timeouts stay in Ktor |
| Race conditions | Single-writer vault per process; LAN LWW for multi-device; hub HMAC on PUT |
| Unhandled exceptions | Platform roots catch; notification/widget failures must not crash the reader |
