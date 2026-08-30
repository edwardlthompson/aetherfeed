# Build Plan

> Prioritized task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing, F-Droid submission   |
| `AUTO`  | CI/scripts/bots | GitHub Actions, Dependabot, pre-commit, update checker     |
## Status markers

Use **emoji markers** (not `- [ ]` GitHub checkboxes) so task state reads clearly in Markdown source and Preview. **Applies repo-wide** — `BUILD_PLAN.md`, module checklists, PR template, feature specs, and security triage.

| Marker | State   | Agent action                                                          |
| ------ | ------- | --------------------------------------------------------------------- |
| 🔲     | Open    | Default for new tasks; work or leave queued                           |
| ✅      | Done    | Replace 🔲 when complete; archive sprint rows to `COMPLETED_TASKS.md` |
| ❌      | Blocked | Replace 🔲 when blocked; add brief reason after the description       |
**Task format:** `🔲 [OWNER] Description` · done: `✅ [OWNER] Description` · blocked: `❌ [OWNER] Description — reason`

```bash
grep '\[AGENT\]' BUILD_PLAN.md
grep '\[HUMAN\]' BUILD_PLAN.md
grep '\[ADB\]' BUILD_PLAN.md
grep '\[AUTO\]' BUILD_PLAN.md

```

**Agent rule:** Execute all `[AGENT]` **Sequential** items first, then dispatch **Parallel** agents with isolated file scopes (`docs/PARALLEL_AGENT_SCOPES.md`). Shared schema/types are Sequential-only.

### Parallel dispatch protocol (orchestrator)

| Step | Action                                                                                                                                                                     |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Finish all `[AGENT]` **Sequential** items for the active sprint/feature (shared schema/types locked)                                                                       |
| 2    | **Discover** parallelizable work using the decomposition checklist below; add Parallel table rows with non-overlapping ``path/**`` scopes                                  |
| 3    | Run `bash scripts/plan-parallel-dispatch.sh` → read **agent_count**                                                                                                        |
| 4    | If `agent_count >= 2`, run `/scope` (auto Task dispatch); if `1`, execute inline; if `0`, run `--suggest` and expand the Parallel table (or document `parallel_exception`) |
| 5    | Sequential owner merges results, runs `watch-agent-gates.sh`, updates BUILD_PLAN (Parallel agents never edit BUILD_PLAN)                                                   |
**Decomposition checklist** (apply before finalizing Sequential items):

| Heuristic                     | Split into Parallel agents                                                                  |
| ----------------------------- | ------------------------------------------------------------------------------------------- |
| Multi-stack repo              | One agent per active module (`examples/{stack}/`**)                                         |
| Feature container (Sprint 2+) | Agent A: pure logic + unit tests; Agent B: view/Composable + i18n                           |
| Tests vs production code      | Separate `**/*.test.*`, `e2e/**`, `androidTest/**` when paths do not overlap implementation |
| Docs vs code                  | Agent A: `examples/**`; Agent B: `docs/**`, `modules/**`, `.cursor/rules/**`                |
| CI/gates vs app code          | Agent A: `scripts/**`, `.github/workflows/**`; Agent B: stack example tree                  |
**Default rule:** If a Sequential `[AGENT]` item touches two or more non-overlapping directory prefixes, **split it** — leave only schema-lock work Sequential.

**Planning (Plan Mode):** Every BUILD_PLAN proposal must include `### Parallelization` with `agent_count_target`, decomposition table, and dry-run from `plan-parallel-dispatch.sh`. Run `check-build-plan-parallel.sh` before human approval.

**Autonomous `/build`:** Runs all `[AGENT]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Success marks ✅; failure appends `HUMAN_BACKLOG.md` and continues — never halts on human labels. Humans review the grouped section (and backlog) after automation finishes. Status: `bash scripts/build-sprint-status.sh --json`.

> **Template maintainer:** no active AGENT sprint — **v0.21.0** shipped. **Child repos:** copy the playbook.

---

## Template Maintainer — Active Board

> **M39** archived in `COMPLETED_TASKS.md`. **v0.21.0** @ `1525cd6`. **M38** archived in `COMPLETED_TASKS.md`. **Coach / M37 / M36** archived in `COMPLETED_TASKS.md`. **v0.20.0** @ `b570f07`. **v0.19.0** archived in `COMPLETED_TASKS.md` @ `2bef8ac`. **v0.18.3** archived in `COMPLETED_TASKS.md` @ `013e688`. **v0.18.2** archived in `COMPLETED_TASKS.md` @ `7d46e68`. **M35 HUMAN** (Scorecard + Dependabot + radar) archived in `COMPLETED_TASKS.md`. **v0.18.1** archived in `COMPLETED_TASKS.md` @ `fe80fea`. **M35** AGENT rows archived in `COMPLETED_TASKS.md`. **v0.18.0** archived in `COMPLETED_TASKS.md` @ `3f0b5a3`. **M34** (prior-art thin steals) archived in `COMPLETED_TASKS.md`. **v0.17.0** archived in `COMPLETED_TASKS.md` @ `701cd24`. **v0.16.0** @ `90ce3db`. **v0.15.2** archived in `COMPLETED_TASKS.md` @ `634d06d`. **v0.15.0** archived in `COMPLETED_TASKS.md` @ `2e010ae`. **M33** archived in `COMPLETED_TASKS.md` @ `5d2d129`. **v0.14.1** archived in `COMPLETED_TASKS.md` @ `a6c6be1`. **M32** archived in `COMPLETED_TASKS.md` @ `e532c20`. **M31** archived in `COMPLETED_TASKS.md` @ `cd21e5a`. **v0.14.0** @ `4b94298`. **v0.13.2** @ `ff8e4e6`. **M19–M30** archived in `COMPLETED_TASKS.md`. **M18** @ `d6b92a2`. **M30** @ `508a541`.

### Open (human judgment only)

*None — M35 HUMAN items archived. Recurring maintenance: see **Ongoing Maintenance** below.*

---

## Child Repo Playbook (copy after Use this template)

> Init scripts, feature docs (`docs/features/_template.md`), and About + Settings exemplars ship with the template. Mirror the Sequential + Parallel lane structure from Sprint M9 when customizing.

### CRITICAL NOTES (phase transitions)

When **Sprint 0** ends: stop re-reading `docs/INITIALIZATION_PROMPT.md` as the daily driver. `/feature` expects a copied `docs/features/{name}.md` from `_template.md`, a locked public API, then Parallel logic/view slices. Copy `scratchpad.md.example` → `scratchpad.md` (gitignored) and **reset** it on sprint/phase change — do not replace `AGENT_MEMORY.md`.

### Sprint 0 — AetherFeed seed

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 0** archived in `COMPLETED_TASKS.md` @ `031b156`.

### Sprint 1 — Encrypted vault and shared models

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 1** archived in `COMPLETED_TASKS.md`.

### Sprint 2 — News / RSS reader

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 2** archived in `COMPLETED_TASKS.md`.

### Sprint 3 — Podcasts

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 3** archived in `COMPLETED_TASKS.md`.

### Sprint 4 — Booru browser

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 4** archived in `COMPLETED_TASKS.md`.

### Sprint 5 — Notifications and unread chrome

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 5** archived in `COMPLETED_TASKS.md`.

### Sprint 6 — E2E sync

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 6** archived in `COMPLETED_TASKS.md`.

### Sprint 7 — Import/export and polish

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 7** archived in `COMPLETED_TASKS.md`.

### Sprint 8 — Reader source import

<!-- parallel_exception: archived in COMPLETED_TASKS.md -->

> **Sprint 8** archived in `COMPLETED_TASKS.md`.

### Sprint 9 — Dedupe and news/podcast sort

<!-- parallel_exception: AGENT work archived in COMPLETED_TASKS.md; HUMAN export remains -->

> AGENT work archived in `COMPLETED_TASKS.md`. Confirm your local gReader/Inoreader files in **Human & device** (also `HUMAN_BACKLOG.md`).

#### Human & device (after automation)

1. 🔲 [HUMAN] Export OPML from gReader (phone) and Inoreader (PC) and confirm a local import

### Sprint 10 — Drive feed-source sync

<!-- parallel_exception: feed-source Drive appdata; one sequential slice -->

1. ✅ [AGENT] Shared feed-source merge + AES-GCM envelope + Drive appdata pull/push
2. ✅ [AGENT] Settings Connect/Sync on web/desktop and Android; loopback OAuth
3. 🔲 [HUMAN] Authorize Google Drive on phone and PC with the same passphrase, then Sync now

### Sprint 11 — Sequential lock (chrome + specs)

<!-- agent_count_target: 0 sequential -->
<!-- parallel_exception: Sequential schema/IA lock; Parallel starts Sprint 12 -->

1. ✅ [AGENT] Combined gap-board canvas + `docs/features/app-shell.md` + spec-hole edits (sleep, safe-mode, API keys, hash, rules, OPML export)
2. ✅ [AGENT] Thin composition mount for 3-mode chrome (web `AppShell` / Android overlay-only Settings); i18n `nav.*` / `nav_*`; relabel Booru → Boards

### Sprint 12 — Parallel P0 (chrome + modes)

<!-- agent_count_target: 0 sequential -->
<!-- parallel_exception: Sprint 12 P0 merged 2026-08-18 -->

> **Sprint 12** P0 merged. News / Podcasts / Boards panes wired in `AppShell`. Archive: `COMPLETED_TASKS.md`.

### Sprint 12b — Parallel P0 remainder + P1

<!-- agent_count_target: 0 sequential -->
<!-- parallel_exception: Sprint 12b merged 2026-08-19 -->

> **Sprint 12b** merged. Archive: `COMPLETED_TASKS.md`.

### Sprint 13 — Parallel leftover P1 + P2

<!-- agent_count_target: 0 sequential -->
<!-- parallel_exception: Sprint 13 merged 2026-08-19 -->

> **Sprint 13** P1/P2 slices merged. Archive: `COMPLETED_TASKS.md`.

#### Human & device (after automation)

1. 🔲 [HUMAN] Sprint 9 OPML confirm (gReader + Inoreader local import)
2. 🔲 [HUMAN] Sprint 10 Drive authorize + Sync now
3. 🔲 [ADB] Smoke News/Podcasts/Boards on the OPPO after merge

### Sprint 14 — App lock + bind leftovers

<!-- agent_count_target: 8 -->

#### Sequential

1. ✅ [AGENT] Lock `Article.contentHtml`, `AppLock` types, news full-text/paywall note, ADR-0002 wrap-by-PIN addendum
2. ✅ [AGENT] Unlock gate in `appBootstrap` / `AetherFeedApp`; Parallel lock, full-text, podcasts, and boards binds merged

#### Parallel

| Task | Owner | Scope |
|------|-------|-------|
| First-run PIN/passphrase, wrap/open blobs, replace plaintext reader vault | AGENT | `examples/web/src/lock/**` |
| PIN wrap for SQLCipher + encrypted article/image files | AGENT | `examples/android/app/src/main/java/org/aetherfeed/app/applock/**` |
| Parse content:encoded, fetch+extract, hook reader/rules/search | AGENT | `examples/web/src/news/**` |
| Same full-text preference order into encrypted files | AGENT | `examples/android/app/src/main/java/org/aetherfeed/app/news/**` |
| Parse real enclosures; bind downloads, sleep, directory, playlists | AGENT | `examples/web/src/podcasts/**` |
| Parse enclosures + queue + Media3 port | AGENT | `examples/android/app/src/main/java/org/aetherfeed/app/podcasts/**` |
| Add public board source + grid when sources exist | AGENT | `examples/web/src/boards/**` |
| Add-source + safe-mode + API keys in pane | AGENT | `examples/android/app/src/main/java/org/aetherfeed/app/ui/booru/**` |
### Sprint 15 — Silence skip + boards merge

<!-- agent_count_target: 2 -->

#### Parallel

| Task | Owner | Scope |
|------|-------|-------|
| On-device silence skip + voice gain; synthetic PCM tests | AGENT | `examples/web/src/silence/**` ✅ |
| Multi-source merge + pools when two sources exist | AGENT | `examples/android/app/src/main/java/org/aetherfeed/app/boardmerge/**` ✅ |
#### Human & device (after automation)

1. 🔲 [HUMAN] Sprint 9 OPML confirm (gReader + Inoreader local import)
2. 🔲 [HUMAN] Sprint 10 Drive authorize + Sync now
3. 🔲 [ADB] Smoke News/Podcasts/Boards on the OPPO after merge

### Sprint 16 — Android library seed + refresh/history

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Seed desktop library onto Android; hourly+ refresh; history count/days (default last 10)

### Sprint 17 — See articles + UI parity

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Auto-refresh + pull-to-refresh; Tauri feed fetch; smoke/seed import so both apps show articles

### Sprint 18 — Reader chrome parity

<!-- agent_count_target: 3 -->

#### Sequential

1. ✅ [AGENT] Lock UI_PARITY contract, shared chrome IDs, `ui-parity.mdc`, `check-ui-parity.py`

#### Parallel

| Task | Isolated scope | Why safe |
|------|----------------|----------|
| ✅ [AGENT] Web/desktop shell + lock remount + full-window 3-pane | `examples/web/src/AppShell*.ts`, `lock/`, `news/news.css`, `style.css`, `desktop/src-tauri` | No Android sources |
| ✅ [AGENT] Android BackHandler + saveable news IDs + HTML reader | `examples/android/.../ui/news/`, `AetherFeedScreen.kt`, `AetherFeedApp.kt` | No web shell |
| ✅ [AGENT] Encrypted image cache (web persist + Android EncryptedCache) | `examples/web/src/news/openArticle.ts`, `reader/`, `android/.../news/ArticleImages.kt` | No AppShell |
Do not mark a Parallel row ✅ until `check-ui-parity.py` and `watch-agent-gates --once --autofix` pass.

### Sprint 19 — Reader density follow-up

<!-- agent_count_target: 1 -->

#### Sequential

1. 🔲 [AGENT] List thumbnails, swipe mark-read, desktop `j`/`k` (after Sprint 18 reader + Back + images)

### Sprint 20 — Cached articles, network policy, source tree

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Persist article index; Wi-Fi vs cellular; left-column category tree on both apps

### Sprint 21 — Reader mode + lock remount

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Themed reading-mode only (no live webpage); remount desktop PIN without F5

### Sprint 22 — Collapsed folders, hide sources, resizable panes

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Folders collapsed by default (remembered); desktop mode bar at bottom; hide sources; drag-resize panes persist on both apps

### Sprint 23 — Reading-mode images, unread cache, thumbnails

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Extract reading mode first; drop social icons; cache unread articles with determinate progress; use remaining images as thumbnails

### Sprint 24 — Swipe, sort, nav unread, instant cache

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Swipe/j-k between articles; oldest-first sort; unread badges on the three mode icons; slim pane chrome; refresh in the top bar; instant cache paint; priority load of the opened article then the next in series

### Sprint 25 — PIN keyboard, filter, folders swipe, cache dot

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] PIN numeric keyboard vs passphrase; filter icon for sort; left-edge folders drawer; green cache dot; strip article/comment URLs from reader

### Sprint 26 — Seed library parity + cache all unread

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Apply desktop seed-library on Android unlock; prefetch unread from every news source under history/network rules

### Sprint 27 — Skip already-fetched article bodies

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Persist a fetched marker for each unread prefetch attempt; skip those ids on the next load; do not RSS-refresh empty indexes during prefetch

### Sprint 28 — Dead feed notice

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Refresh the open feed on select; tell the user when a source returns 404/410

### Sprint 29 — Recents privacy cover

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Black Android recents (`FLAG_SECURE` + API 33 `setRecentsScreenshotEnabled`) and web hide/blur cover; document Windows Alt-Tab limit

### Sprint 30 — Library tree, unified timelines, share, cache retain

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Library tree roots (Unified / News / Podcasts / Boards), all/folder/source + unified timelines, remove bottom mode bar, action-share (Android + web)
2. ✅ [AGENT] Settings cache retain: 30 days or next sync; starred blobs never deleted; unstar + past expiry deletes

### Sprint 31 — Donations and updates (Continuum method)

<!-- agent_count_target: 1 -->

#### Sequential

1. ✅ [AGENT] Quiet Venmo donate + once-per-version note + daily installer-asset GitHub check (web + Android)

---

## Ongoing Maintenance (recurring)

> **Template maintainer:** `bash scripts/run-maintainer-gates.sh` weekly (omit `--quick` for full CI wait).

### Weekly

- 🔲 [AUTO] `cursor-feature-radar.sh` (non-blocking; artifact in weekly-health-check)
- 🔲 [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI + Scorecard)
- 🔲 [AGENT] Apply Dependabot bumps; triage Scorecard SARIF findings
- 🔲 [AUTO] CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `simulate-template-upgrade.sh` (also in `weekly-health-check.yml`)
- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release
- 🔲 [AGENT] Review Dependabot auto-merge PRs (KB-007)

### Pre-release (every version)

- 🔲 [AUTO] `pre-release-gate.sh` + `run-maintainer-gates.sh` (includes `verify-branch-protection.sh`)
- 🔲 [AUTO] Release Please PR merged; CHANGELOG + manifest bumped

### Human (after automation)

> Product approvals after automated pre-release gates pass.

- 🔲 [HUMAN] Approve release tag when product-ready
- 🔲 [HUMAN] Quarterly Cursor feature radar backlog review (next due 2026-11-15; last pass 2026-08-15)

---

## Archived Sprints

| Sprint                                                            | Status   | Archive                          |
| ----------------------------------------------------------------- | -------- | -------------------------------- |
| Sprint 13 — leftover P1 + P2                                      | Complete | `COMPLETED_TASKS.md`             |
| Sprint 12b — Parallel P0 remainder + P1                           | Complete | `COMPLETED_TASKS.md`             |
| Sprint 12 — Parallel P0 chrome + modes                            | Complete | `COMPLETED_TASKS.md`             |
| Sprint 9 — Dedupe and news/podcast sort (AGENT)                   | Complete | `COMPLETED_TASKS.md`             |
| Sprint 8 — Reader source import                                   | Complete | `COMPLETED_TASKS.md`             |
| Sprint 7 — Import/export and polish                               | Complete | `COMPLETED_TASKS.md`             |
| Sprint 6 — E2E sync                                               | Complete | `COMPLETED_TASKS.md`             |
| Sprint 5 — Notifications and unread chrome                        | Complete | `COMPLETED_TASKS.md`             |
| Sprint 4 — Booru browser                                          | Complete | `COMPLETED_TASKS.md`             |
| Sprint 3 — Podcasts                                               | Complete | `COMPLETED_TASKS.md`             |
| Sprint 2 — News / RSS reader                                      | Complete | `COMPLETED_TASKS.md`             |
| Sprint 1 — Encrypted vault and shared models                      | Complete | `COMPLETED_TASKS.md`             |
| Sprint 0 — AetherFeed seed                                        | Complete | `COMPLETED_TASKS.md` @ `031b156` |
| M39 /ideas Windows PATH + ship hygiene                            | Complete | `COMPLETED_TASKS.md`             |
| v0.21.0 Windows PATH + Unreleased fold                            | Complete | `COMPLETED_TASKS.md` @ `1525cd6` |
| M38 /ideas ship-hardening                                         | Complete | `COMPLETED_TASKS.md`             |
| Coach / M37 / M36 (stale ✅ active-board rows)                     | Complete | `COMPLETED_TASKS.md`             |
| v0.20.0 first-run backlog + Windows upgrade-sim                   | Complete | `COMPLETED_TASKS.md` @ `b570f07` |
| v0.19.0 portable first-run release                                | Complete | `COMPLETED_TASKS.md` @ `2bef8ac` |
| v0.18.3 Compose BOM release                                       | Complete | `COMPLETED_TASKS.md` @ `013e688` |
| v0.18.2 Scorecard + Dependabot release                            | Complete | `COMPLETED_TASKS.md` @ `7d46e68` |
| M35 HUMAN — Scorecard + Dependabot + radar                        | Complete | `COMPLETED_TASKS.md`             |
| v0.18.1 Windows Python resolver release                           | Complete | `COMPLETED_TASKS.md` @ `fe80fea` |
| M35 — Audit 2026-08-15                                            | Complete | `COMPLETED_TASKS.md`             |
| v0.18.0 prior-art thin steals release                             | Complete | `COMPLETED_TASKS.md` @ `3f0b5a3` |
| M34 — Prior-art thin steals                                       | Complete | `COMPLETED_TASKS.md`             |
| v0.17.0 branding kit release                                      | Complete | `COMPLETED_TASKS.md` @ `701cd24` |
| v0.15.2 release                                                   | Complete | `COMPLETED_TASKS.md` @ `634d06d` |
| v0.15.0 release                                                   | Complete | `COMPLETED_TASKS.md` @ `2e010ae` |
| M33 — Cursor 3.9–3.11 + local-first compute                       | Complete | `COMPLETED_TASKS.md` @ `5d2d129` |
| v0.14.1 release                                                   | Complete | `COMPLETED_TASKS.md` @ `a6c6be1` |
| M32 — Audit 2026-07-12                                              | Complete | `COMPLETED_TASKS.md` @ `e532c20` |
| v0.14.0 release                                                   | Complete | `COMPLETED_TASKS.md` @ `4b94298` |
| v0.13.2 release                                                   | Complete | `COMPLETED_TASKS.md` @ `ff8e4e6` |
| M31 — Audit 2026-07-01                                            | Complete | `COMPLETED_TASKS.md`             |
| M30 — Cursor FOSS integration + feature radar                     | Complete | `COMPLETED_TASKS.md` @ `508a541` |
| M19–M29 — Cursor modes, batch commands, maintain, v0.11.0 release | Complete | `COMPLETED_TASKS.md`             |
| v0.10.0 release (`36a02e4`)                                       | Complete | `COMPLETED_TASKS.md`             |
| M5–M18 maintainer sprints (seq + P2)                              | Complete | `COMPLETED_TASKS.md` @ `d6b92a2` |
| Child Sprint 2 starter scaffold                                   | Complete | `COMPLETED_TASKS.md`             |
| v0.9.0 release (`fd699bc`)                                        | Complete | `COMPLETED_TASKS.md`             |
