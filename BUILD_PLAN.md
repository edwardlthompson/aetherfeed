# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 8 · LOCAL 8 · CLOUD 0 · AUTO 1 · HUMAN 0 · ADB 0 · **9 open**
<!-- /remaining-tally -->

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

**Agent rule:** Execute all `[AGENT][LOCAL]` **Sequential** items first, then dispatch **Parallel** agents with isolated file scopes (`docs/PARALLEL_AGENT_SCOPES.md`). Shared schema/types are Sequential-only.

### Parallel dispatch protocol (orchestrator)

| Step | Action                                                                                                                                                                     |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Finish all `[AGENT][LOCAL]` **Sequential** items for the active sprint/feature (shared schema/types locked)                                                                       |
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
**Default rule:** If a Sequential `[AGENT][LOCAL]` item touches two or more non-overlapping directory prefixes, **split it** — leave only schema-lock work Sequential.

**Planning (Plan Mode):** Every BUILD_PLAN proposal must include `### Parallelization` with `agent_count_target`, decomposition table, and dry-run from `plan-parallel-dispatch.sh`. Run `check-build-plan-parallel.sh` before human approval.

**Autonomous `/build`:** Runs all `[AGENT][LOCAL]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Success marks ✅; failure appends `HUMAN_BACKLOG.md` and continues — never halts on human labels. Humans review the grouped section (and backlog) after automation finishes. Status: `bash scripts/build-sprint-status.sh --json`.

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

When **Sprint 0** ends: stop re-reading `docs/INITIALIZATION_PROMPT.md` as the daily driver. `/feature` expects a copied `docs/features/{name}.md` from `_template.md`, a locked public API, then Parallel logic/view slices. Copy `scratchpad.md.example` → `scratchpad.md` (gitignored) and **reset** it on sprint/phase change — do not replace `.agent/AGENT_MEMORY.md`.

### Sprint 0 — AetherFeed seed

#### Sequential

1. ✅ [AGENT][LOCAL] Run `scripts/init-project.ps1` (`--stack multi`; AetherFeed name + purpose)
1b. ✅ [AGENT][LOCAL] Fill `branding/product.json` (`mode: product`), replace logos, prune unused stacks
2. ✅ [AGENT][LOCAL] Run `scripts/setup-github-repo.sh` — blocked until a child GitHub repo exists
3. 🔲 [AUTO] Sprint 0 sign-off (all green on `main`):
  - `validate-bootstrap.sh --quick`
  - `feature-gate.sh --stack multi`
  - `check-github-ci.sh --wait 300` (required: **CI**, **Security Scan**, **CodeQL**; **CI** must include **Template Upgrade Simulation (Windows)**, **Repo Hygiene**, **Feature Gate**)
  - `check-license-compliance.sh` (after `npm ci`)

#### Parallel (safe after Sequential step 1b)

<!-- agent_count_target: 3 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Android identity + vault contract | AGENT [LOCAL] | `examples/android/**` |
| Web domain + desktop Tauri shell | AGENT [LOCAL] | `examples/web/**`, `examples/desktop/**` |
| Crypto crate + shared schema | AGENT [LOCAL] | `examples/rust/**`, `shared/**` |

#### Human & device (after automation)

1. ✅ [HUMAN] Create the GitHub repo and point `origin` at it (do not push to the template)
1a. ✅ [HUMAN] Distribution tier is FOSS (MIT, no proprietary SDKs)
2. ✅ [HUMAN] Platform/purpose filled: Android + Windows desktop; local-first encrypted reader
2a. ✅ [HUMAN] Agent mode for approved seed execution
2b. ✅ [HUMAN] Bookmark `docs/help/BATCH_COMMANDS.md`

### Sprint 1 — Encrypted vault and shared models

#### Sequential

1. ✅ [AGENT][LOCAL] Lock shared models and `SyncProvider` (`shared/typescript/`, Android `domain/`)
2. 🔲 [AGENT][LOCAL] Wire Room + SQLCipher + Hilt on Android using the locked types — scope: examples/android/
3. ✅ [HUMAN] Approve ADR-0001, ADR-0002, and ADR-0003

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Android About + four-destination nav | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/ui/**` |
| Desktop tray unread command | AGENT [LOCAL] | `examples/desktop/src-tauri/**` |
| Shared unread tests | AGENT [LOCAL] | `shared/typescript/**`, `examples/web/src/domain/**` |

#### Human & device (after automation)

1. ✅ [HUMAN] Fill `release_repo` in `.app-update.json` and donation links
2. ✅ [HUMAN] Approve Sprint 1 vault/Hilt approach

### Sprint 2 — News / RSS reader

<!-- parallel_exception: android news logic and ui share examples/android; web owns separate reader chrome -->

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock RSS/Atom/JSON Feed + OPML public API (`docs/features/news.md`) — scope: docs/features/news.md
2. 🔲 [AGENT][LOCAL] Scaffold news repository boundary only — scope: examples/android/app/src/main/java/org/aetherfeed/app/news/

#### Parallel (safe after Sequential step 2)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Feed parse + unread/star/tag + Android reader UI | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/news/**`, `examples/android/app/src/main/java/org/aetherfeed/app/ui/news/**` |
| Web reader chrome + i18n | AGENT [LOCAL] | `examples/web/src/news/**` |
| News feature spec | AGENT [LOCAL] | `docs/features/news.md` |

### Sprint 3 — Podcasts

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock episode/queue/position API (`docs/features/podcasts.md`) — scope: docs/features/podcasts.md

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Media3 player + downloads | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/podcasts/**` |
| Desktop playback commands | AGENT [LOCAL] | `examples/desktop/src-tauri/src/**` |
| Podcast feature spec | AGENT [LOCAL] | `docs/features/podcasts.md` |

### Sprint 4 — Booru browser

<!-- parallel_exception: android booru logic and ui share examples/android; scopes nested intentionally -->

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock source/search/favorite/blacklist API (`docs/features/booru.md`) — scope: docs/features/booru.md

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Source adapters + tag search + Android grid/detail | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/booru/**`, `examples/android/app/src/main/java/org/aetherfeed/app/ui/booru/**` |
| Web grid/detail + i18n | AGENT [LOCAL] | `examples/web/src/booru/**` |
| Booru feature spec | AGENT [LOCAL] | `docs/features/booru.md` |

### Sprint 5 — Notifications and unread chrome

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock unread-total API used by widget and tray — scope: examples/android/app/src/main/java/org/aetherfeed/app/notify/

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Android channels + unread widget | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/notify/**` |
| Desktop tray badge/tooltip | AGENT [LOCAL] | `examples/desktop/src-tauri/src/**` |

### Sprint 6 — E2E sync

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock envelope format and provider interface (`docs/features/sync.md`) — scope: docs/features/sync.md

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Drive AppData provider | AGENT [LOCAL] | `examples/android/app/src/main/java/org/aetherfeed/app/sync/**` |
| WebDAV provider + desktop pull/push | AGENT [LOCAL] | `examples/desktop/src-tauri/src/**`, `examples/web/src/sync/**` |

### Sprint 7 — Import/export and polish

#### Sequential

1. 🔲 [AGENT][LOCAL] Lock OPML + JSON export API — scope: examples/android/metadata/

#### Parallel (safe after Sequential step 1)

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Android export + About/Fastlane copy | AGENT [LOCAL] | `examples/android/fastlane/**`, `examples/android/metadata/**` |
| Desktop packaging + Winget stub | AGENT [LOCAL] | `packaging/winget/**`, `examples/desktop/**` |

---

## Ongoing Maintenance

Not a checklist. GitHub Monday cron (`.github/workflows/weekly-health-check.yml`) already runs CI wait, security triage, parent template-gap sync, radar, Dependabot leftover list, and latest-release SBOM. `/ship` owns pre-release and the release tag.

Do not put standing weekly/Monday chores back on this board as open checklist rows. See [`docs/GROK_BOTS.md`](docs/GROK_BOTS.md) and [`docs/CURSOR_AUTOMATIONS.commercial.md`](docs/CURSOR_AUTOMATIONS.commercial.md).

### Human (after automation)

> Product approvals after automated pre-release gates pass. Tracked outside standing chore emoji rows.

- [HUMAN] Approve release tag when product-ready
- [HUMAN] Quarterly Cursor feature radar backlog review

---

## Archived Sprints

| Sprint                                                            | Status   | Archive                          |
| ----------------------------------------------------------------- | -------- | -------------------------------- |
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
