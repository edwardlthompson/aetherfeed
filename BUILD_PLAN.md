# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 23 · LOCAL 23 · CLOUD 0 · AUTO 2 · HUMAN 5 · ADB 2 · **32 open**
<!-- /remaining-tally -->

Live board for this product repo. Finished work: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).

**Who:** `AGENT` code · `HUMAN` person · `ADB` device · `AUTO` CI/scripts
**Venue (AGENT only):** `[LOCAL]` This Computer · `[CLOUD]` Cursor Cloud — [`docs/adr/0008-agent-venue.md`](docs/adr/0008-agent-venue.md)
**State:** 🔲 open · ✅ done · ❌ blocked — reason

Format: `🔲 [AGENT][LOCAL] Short task — scope: path/prefix` (or `[CLOUD]`). Sequential `[AGENT]` first. Parallel scopes: [`docs/PARALLEL_AGENT_SCOPES.md`](docs/PARALLEL_AGENT_SCOPES.md). `/build` on This Computer picks LOCAL only; Cloud picks CLOUD only. HUMAN/ADB after automation → `HUMAN_BACKLOG.md`.

## Smoke gate (hard stop)

After every `[AGENT]` row: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

After the **last** `[AGENT]`/`[AUTO]` row in a sprint is ✅, do **not** start the next sprint until this exits 0:

```bash
python3 scripts/agent-run.py smoke-sprint --require

```

Details: [`docs/SPRINT_SMOKE.md`](docs/SPRINT_SMOKE.md). Fail → leave the last row open or ❌; fix; re-run.

---

## Product

Copy this shape when you add sprints: `### Sprint N — title`, then numbered rows. Keep it this short.

### Product (do not drift)

> Auto-managed from `AGENT.md`. Do not hand-edit inside markers. Read `AGENT.md` before any sprint row.

<!-- product-brief-sync:begin -->
> Read `AGENT.md` before any sprint row.

**One-liner:** One local-first client for feeds, podcasts, and image boards with offline stars and playback.
**Do not drift:** rss, podcasts, booru, local-first, privacy, offline, vault

**Rules:**
- Keep the vault local-first; no account required.
- Stars and playback position must survive radio-off.
- Optional sync is ciphertext-only; never upload plaintext library state.
- No analytics, crash reporters, ads, or tracking libraries.
- Do not overwrite Sacred product paths (`docs/spec.md`, `AGENTS.md` project card) with template stubs.

**First milestone:** Offline read path end-to-end with cached items, stars, and playback persisted locally.
<!-- product-brief-sync:end -->

> **Done:** Sprint 0–7 Sequential locks (vault, news/podcasts/booru/notify/sync/export APIs) — [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md) @ `/build` 2026-11-12.

### Sprint 8 — Hermes subscription pool

<!-- parallel_exception: sequential-only single agent slice -->

1. ✅ [AGENT][LOCAL] Client + codec + wire news/podcast pool (exclude booru) — scope: `examples/android/.../sync/`, `docs/features/hermes-subscription-pool.md`

### Sprint 9 — Home LAN encrypted pool (1B+2B)

<!-- parallel_exception: sequential crypto + hub wiring -->

1. ✅ [AGENT][LOCAL] Pool document v2 + LWW (subs/reads/stars/playback) — AetherFeed + Hermes codecs
2. ✅ [AGENT][LOCAL] SyncEnvelope crypto (Rust + Android/Hermes) + desktop LAN hub + Settings
3. ✅ [AGENT][LOCAL] AetherFeed + Hermes LAN clients; ContentProvider retained — ADR-0011, `docs/features/sync.md`

## Child Repo Playbook

> **Sprint 1–7** archived in COMPLETED_TASKS.md @ `/build` 2026-11-12.

### Waiting on a person

1. ✅ [HUMAN] Approve release tag when product-ready
2. 🔲 [HUMAN] Quarterly Cursor feature radar backlog review
3. 🔲 [ADB] Optional: Android SDK licenses + first AVD (`/emulator`)

### Open PRs (synced)

> Auto-managed. Do not hand-edit rows inside the markers.

<!-- open-prs-sync:begin -->
_No open Dependabot or Release Please PRs._
<!-- open-prs-sync:end -->

### Template gaps (synced)

> Auto-managed Monday cron + `sync-template-gaps-build-plan`. Do not hand-edit inside markers. Plan-only — run `/upgrade` then name item numbers.

<!-- template-gaps-sync:begin -->
_No template gaps; .template-version matches upstream (or template maintainer N/A)._
<!-- template-gaps-sync:end -->

### UX & UI inventory

Complete list from construction gaps and `/ux-review`. Status is only planned / in_progress / done. `/build` does not execute these until `/ux-apply UX-NNN` (or a Sequential row).

<!-- ux-inventory:begin -->
_No UX inventory items._
<!-- ux-inventory:end -->

### Local agent (This Computer)

Standing queue for This Computer. Rows: `🔲 [AGENT][LOCAL] … — scope: path`. `/build` claims these only.

<!-- local-agent-lane:begin -->
_No local agent items._
<!-- local-agent-lane:end -->

### Cloud agent (Cursor Cloud)

Standing queue for Cursor Cloud. Rows: `🔲 [AGENT][CLOUD] … — scope: path`. Cloud claims these only (`cursor/*`). Never edit Local lane or `[LOCAL]` rows.

<!-- cloud-agent-lane:begin -->
_No cloud agent items._
<!-- cloud-agent-lane:end -->

---

<!-- argus-android-release-sprint:begin -->
### Sprint 55 — Ship Android GitHub Release

<!-- parallel_exception: sequential-only Android release cut -->

Numbered rows so child `/build` (parse_board_queue) picks them up. **Not** under Archive lanes.

1. 🔲 [AGENT][LOCAL] Cut/update GitHub Release with signed Android APK/AAB — scope: examples/android/

#### Agent instructions (release row)

```text
Repo: edwardlthompson/aetherfeed
Goal: Latest GitHub Release must include a real Android binary (.apk and/or .aab).

Why: Latest GitHub Release has SBOMs but no APK/AAB.

Steps:
1) Read AGENT.md + Android docs. Prefer gradle under `examples/android/`.
2) Use existing signing (local keystore / repo CI secrets). Do not invent Play Store upload.
3) From `examples/android` (or documented root):
     ./gradlew assembleRelease
     ./gradlew bundleRelease
4) Outputs: app/build/outputs/apk/ and app/build/outputs/bundle/.
5) Prefer this repo's /ship or Release Please flow if documented; else:
     gh release list -R edwardlthompson/aetherfeed --limit 3
     gh release create <TAG> --generate-notes -R edwardlthompson/aetherfeed
     gh release upload <TAG> <apk> [<aab>] -R edwardlthompson/aetherfeed --clobber
6) Expected names: *-release.apk / *.aab
7) Gate:
     gh release view <TAG> -R edwardlthompson/aetherfeed --json assets --jq ".assets[].name"
   At least one asset must end with .apk or .aab.
8) Mark the BUILD_PLAN release row ✅; note tag + asset names.

No force-push to main. No committing binaries into git.
```

Releases: https://github.com/edwardlthompson/aetherfeed/releases
<!-- argus-android-release-sprint:end -->

### Sprint News — News Bot follow-ups

<!-- parallel_exception: independent news follow-ups; run sequentially -->

1. 🔲 [AGENT][LOCAL] Audit the repo for wfx-loot IOCs — scope: .github/ (news P1, 2026-11-14) https://blog.gitguardian.com/wfx-loot-a-ci-secret-stealer-pushed-with-stolen-github-credentials/

## Multiplatform Migration (Phases 3–4+)

<!-- parallel_exception: sequential-only migration; one shared KMP/CMP tree -->

> **Binding:** [`docs/migration/INVENTORY.md`](docs/migration/INVENTORY.md) (Decisions Edward 2026-11-16, incl. #19),
> [`docs/migration/DESIGN_BRIEF.md`](docs/migration/DESIGN_BRIEF.md) (deviations: none),
> [`docs/migration/TECH_DECISION.md`](docs/migration/TECH_DECISION.md),
> [`docs/adr/0013-compose-multiplatform.md`](docs/adr/0013-compose-multiplatform.md),
> [`docs/migration/PARITY_CHECKLIST.md`](docs/migration/PARITY_CHECKLIST.md).
>
> **Re-read rule:** Before starting **each** sprint below, re-read those five docs in full.
> Parity of **existing** Android behavior first (decision 17). No redesign. Version stays `0.1.0`.
> Targets: Android, iOS, Windows, macOS, Linux — one shared Compose Multiplatform UI + KMP logic.
> **CI:** Do not add push/PR/schedule Actions jobs. Release packaging only on `v*` tags or `workflow_dispatch`.
> Phase 3 = docs (this section + parity checklist). Phase 4+ = implementation after Edward merges.

### Phase 3 — Docs gate

1. ✅ [AGENT][LOCAL] Seed Multiplatform Migration board + `docs/migration/PARITY_CHECKLIST.md` from INVENTORY — scope: `BUILD_PLAN.md`, `docs/migration/`

### Sprint 56 — KMP shared core (data, sync, vault)

<!-- parallel_exception: sequential shared modules before UI -->

1. 🔲 [AGENT][LOCAL] Scaffold `shared/domain` + `shared/vault` (SQLDelight ≈ Room tables; Ktor news refresh; LAN client + SyncEnvelope; keychain expect/actual stubs) — scope: `shared/`, `examples/android/`
2. 🔲 [AUTO] Shared JVM/Android unit tests for parsers, vault open, pool codec vs existing TS/Rust vectors — scope: `shared/`, `shared/typescript/`
3. 🔲 [AGENT][LOCAL] Parity gate Sprint 56 — PARITY_CHECKLIST § Vault/security + News parse/refresh + Sync codec = Parity on Android (KMP) test surface; other platforms Pending — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 57 — Shared Compose UI (1:1 Android)

<!-- parallel_exception: sequential UI port before platform shells -->

1. 🔲 [AGENT][LOCAL] Port theme/chrome/shell/Add-feed/News/reader/Podcasts list/Booru demo/Settings/About into `shared/ui` (DESIGN_BRIEF; no redesign) — scope: `shared/ui/`, `examples/android/.../ui/`
2. 🔲 [AGENT][LOCAL] Compact podcast player bar (M3 tokens; play/pause/seek at least web `<audio>` parity) in shared UI — scope: `shared/ui/`
3. 🔲 [AGENT][LOCAL] Parity gate Sprint 57 — PARITY_CHECKLIST § Navigation/chrome + Screens + Settings/About field order = Parity on CMP/Android host — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 58 — Android switches to shared modules

<!-- parallel_exception: sequential Android cutover -->

1. 🔲 [AGENT][LOCAL] Thin Android app on shared modules; Hilt at app edge only; Hermes/Media3/widget/UnifiedPush stay androidMain; no user-visible behavior change — scope: `examples/android/`
2. 🔲 [ADB] Optional: vault-open + news smoke on device/AVD after cutover — scope: `examples/android/`
3. 🔲 [AGENT][LOCAL] Parity gate Sprint 58 — Android (KMP) column matches Android (current) for all shipped rows (Partial only where INVENTORY was already PARTIAL/STUB) — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 59 — Desktop shells (Win / macOS / Linux)

<!-- parallel_exception: sequential desktop replaces Tauri -->

1. 🔲 [AGENT][LOCAL] Compose Desktop app (1100×760): shared UI, OS keychain, tray `AetherFeed — {n} unread`, LAN hub host; replace Tauri as shipping shell — scope: `examples/desktop/`, `shared/`
2. 🔲 [AGENT][LOCAL] Desktop packaging: Windows MSI/exe, macOS dmg/app, Linux AppImage/deb (jpackage/Gradle); freeze Tauri product path — scope: `examples/desktop/`
3. 🔲 [HUMAN] Optional: macOS code signing / notarization when a signing identity exists — scope: `examples/desktop/`
4. 🔲 [AGENT][LOCAL] Parity gate Sprint 59 — Windows/macOS/Linux: shell, Add-feed, vault, Settings/About, tray = Parity (desktop media-keys / macOS widget = Partial or N/A per TECH fragile rules) — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 60 — iOS app, widget, audio, local notifications

<!-- parallel_exception: sequential iOS after Android+desktop cores -->

1. 🔲 [AGENT][LOCAL] iOS CMP app + Keychain vault + Add-feed + BGAppRefresh/foreground refresh; simulator build/tests only — scope: `examples/ios/`, `shared/`
2. 🔲 [AGENT][LOCAL] iOS WidgetKit unread count file (App Group; no vault in extension) + AVFoundation Now Playing + local UserNotifications — scope: `examples/ios/`
3. 🔲 [AGENT][LOCAL] Unsigned `.ipa` packaging notes for AltStore/SideStore on GitHub Releases — scope: `examples/ios/`, `docs/`
4. 🔲 [HUMAN] On-device verify (Edward has no Apple device): sideload, widget, background audio; Apple Developer / APNs / TestFlight deferred (decision 19) — scope: `examples/ios/`
5. 🔲 [AGENT][LOCAL] Parity gate Sprint 60 — iOS UI/vault/Add-feed/local notify/audio = Parity or Partial; on-device rows = Pending `[HUMAN]`; APNs/TestFlight = N/A (decision 19) — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 61 — Notifications, UnifiedPush, LAN/Hermes

<!-- parallel_exception: sequential notify+sync after shells -->

1. 🔲 [AGENT][LOCAL] Real notification posting on Android/iOS/desktop; UnifiedPush on Android; iOS local-only; no booru channel — scope: `shared/`, `examples/android/`, `examples/ios/`, `examples/desktop/`
2. 🔲 [AGENT][LOCAL] LAN sync parity across clients; Hermes ContentProvider Android-only (hide Settings block elsewhere); Drive/WebDAV stay out of DI — scope: `shared/`, `examples/`
3. 🔲 [AGENT][LOCAL] Parity gate Sprint 61 — PARITY_CHECKLIST § Notifications/widgets/tray + Sync = Parity where OS allows; Hermes N/A off-Android; APNs N/A — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 62 — Release packaging (five platforms)

<!-- parallel_exception: sequential release artifacts -->

1. 🔲 [AGENT][LOCAL] Document/implement release artifact matrix (APK/AAB, Win MSI/exe, macOS dmg/app, Linux AppImage/deb, unsigned iOS ipa); version `0.1.0`; **no new push/PR/schedule workflows** — only `v*` / `workflow_dispatch` — scope: `.github/workflows/`, `docs/`, `examples/`
2. 🔲 [AUTO] Tag-triggered or workflow_dispatch release dry-run attaches expected extensions — scope: `.github/workflows/`
3. 🔲 [HUMAN] Later: F-Droid id rebrand; Apple Developer / TestFlight / App Store; paid APNs — scope: `docs/`
4. 🔲 [AGENT][LOCAL] Parity gate Sprint 62 — PARITY_CHECKLIST § Packaging/CI rows honest (Parity / Pending `[HUMAN]` / N/A) — scope: `docs/migration/PARITY_CHECKLIST.md`

### Sprint 63 — Final parity sprint

<!-- parallel_exception: sequential full checklist walk -->

1. 🔲 [AGENT][LOCAL] Walk entire `PARITY_CHECKLIST.md`; fix gaps or mark Partial / N/A (reason) / `[HUMAN]` — no silent Pending on must-haves — scope: `docs/migration/PARITY_CHECKLIST.md`, `shared/`, `examples/`
2. 🔲 [HUMAN] Device matrix sign-off: Android device, Windows, Linux; macOS if available; iOS on-device via tester — scope: `docs/migration/PARITY_CHECKLIST.md`
3. 🔲 [AGENT][LOCAL] Parity gate Sprint 63 — every checklist row is Parity, Partial (noted), N/A (reason), or open `[HUMAN]` only — scope: `docs/migration/PARITY_CHECKLIST.md`

## Ongoing Maintenance

Not a checklist. GitHub Monday cron (`.github/workflows/weekly-health-check.yml`) already runs CI wait, security triage, parent template-gap BUILD_PLAN sync, radar, `update-deps` dry-run, Dependabot leftover list, open-PR BUILD_PLAN sync, and latest-release SBOM. `/ship` owns pre-release and the release tag.

If Monday cron is red: Cursor Automation `weekly-maintain`, then Grok Bot 4–5. Do not put those chores back on this board. [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md) · [`docs/CURSOR_AUTOMATIONS.commercial.md`](docs/CURSOR_AUTOMATIONS.commercial.md)

---

## Archive

Older sprints: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).


