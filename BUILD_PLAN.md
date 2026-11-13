# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · LOCAL 0 · CLOUD 0 · AUTO 0 · HUMAN 1 · ADB 1 · **2 open**
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

## Ongoing Maintenance

Not a checklist. GitHub Monday cron (`.github/workflows/weekly-health-check.yml`) already runs CI wait, security triage, parent template-gap BUILD_PLAN sync, radar, `update-deps` dry-run, Dependabot leftover list, open-PR BUILD_PLAN sync, and latest-release SBOM. `/ship` owns pre-release and the release tag.

If Monday cron is red: Cursor Automation `weekly-maintain`, then Grok Bot 4–5. Do not put those chores back on this board. [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md) · [`docs/CURSOR_AUTOMATIONS.commercial.md`](docs/CURSOR_AUTOMATIONS.commercial.md)

---

## Archive

Older sprints: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).
