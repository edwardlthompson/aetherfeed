# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · LOCAL 0 · CLOUD 0 · AUTO 0 · HUMAN 5 · ADB 2 · **7 open**
<!-- /remaining-tally -->

Live board. Finished work: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md) · Backlog: [`HUMAN_BACKLOG.md`](HUMAN_BACKLOG.md).

**Who:** `AGENT` · `HUMAN` · `ADB` · `AUTO` · **Venue:** `[LOCAL]` / `[CLOUD]` · **State:** 🔲 open · ✅ done · ❌ blocked

Format: `🔲 [AGENT][LOCAL] Short task — scope: path`. After AGENT rows: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`. Sprint smoke: `python3 scripts/agent-run.py smoke-stack --require`.

---

## Product

### Product (do not drift)

> Auto-managed from `AGENT.md`. Do not hand-edit inside markers.

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

## Waiting on a person

Open HUMAN/ADB only. Details: [`HUMAN_BACKLOG.md`](HUMAN_BACKLOG.md).

1. 🔲 [HUMAN] Quarterly Cursor feature radar backlog review
2. 🔲 [ADB] Optional: Android SDK licenses + first AVD (`/emulator`)
3. 🔲 [ADB] Optional: vault-open + news smoke on device/AVD after cutover — scope: `examples/android/`
4. 🔲 [HUMAN] Optional: macOS code signing / notarization — scope: `examples/desktop/`
5. 🔲 [HUMAN] On-device iOS verify (sideload, widget, audio); Apple Developer / APNs / TestFlight deferred (decision 19) — scope: `examples/ios/`
6. 🔲 [HUMAN] F-Droid id rebrand; Apple/TestFlight/APNs; attach desktop/ipa to next GitHub Release — scope: `docs/`, `examples/`
7. 🔲 [HUMAN] Device matrix sign-off: Android, Windows, Linux; macOS if available; iOS via tester — scope: `docs/migration/PARITY_CHECKLIST.md`

### Open PRs (synced)

> Auto-managed. Do not hand-edit rows inside the markers.

<!-- open-prs-sync:begin -->
_No open Dependabot or Release Please PRs._
<!-- open-prs-sync:end -->

### Template gaps (synced)

> Auto-managed Monday cron + `sync-template-gaps-build-plan`. Do not hand-edit inside markers.

<!-- template-gaps-sync:begin -->
_No template gaps; .template-version matches upstream (or template maintainer N/A)._
<!-- template-gaps-sync:end -->

### UX & UI inventory

<!-- ux-inventory:begin -->
_No UX inventory items._
<!-- ux-inventory:end -->

### Local agent (This Computer)

<!-- local-agent-lane:begin -->
_No local agent items._
<!-- local-agent-lane:end -->

### Cloud agent (Cursor Cloud)

<!-- cloud-agent-lane:begin -->
_No cloud agent items._
<!-- cloud-agent-lane:end -->

---

## Migration SoT (read before new work)

[`INVENTORY`](docs/migration/INVENTORY.md) · [`DESIGN_BRIEF`](docs/migration/DESIGN_BRIEF.md) · [`TECH_DECISION`](docs/migration/TECH_DECISION.md) · [ADR-0013](docs/adr/0013-compose-multiplatform.md) · [`PARITY_CHECKLIST`](docs/migration/PARITY_CHECKLIST.md) · [`RELEASE_ARTIFACTS`](docs/RELEASE_ARTIFACTS.md)

Shipping shells: Android `examples/android/` · Desktop `examples/desktop/compose/` · iOS stub `examples/ios/`. Tauri frozen.

## Ongoing Maintenance

Monday cron owns weekly health. `/ship` owns release tags. Red cron → Automations / Grok Bots — not this board. [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md)

---

## Archived Sprints

| Sprint | Complete | Archive |
|--------|----------|---------|
| 0–7 Sequential locks | 2026-11-12 | COMPLETED_TASKS.md |
| 8 Hermes pool · 9 LAN pool | 2026-11-19 | COMPLETED_TASKS.md |
| 55 Android release `v1.13.4` · News wfx-loot | 2026-11-19 | COMPLETED_TASKS.md |
| 56–63 Multiplatform migration (AGENT/AUTO) | 2026-11-19 | COMPLETED_TASKS.md @ `1f8e245` |

> **Sprint 0–63 AGENT/AUTO** archived in COMPLETED_TASKS.md @ `1f8e245`. Open work is **Waiting on a person** only.
