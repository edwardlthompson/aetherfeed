# Decision Log

> Chronological register of major technical trade-offs, accepted architectures, and rejected alternatives.
> **Treat past entries as immutable history; append only.**

## Format

```markdown
### YYYY-MM-DD — [Title]
- **Status:** Accepted | Rejected | Superseded
- **Context:** ...
- **Decision:** ...
- **Alternatives considered:** ...
- **Consequences:** ...

```

## Entries

### 2026-08-20 — Donations and updates (Continuum method)
- **Status:** Accepted
- **Context:** AetherFeed already had an opt-in tag check and a GitHub Sponsors block. Continuum Calendar already shipped a quieter policy: always-available Venmo, one note after a version change, and a daily installer-filename check.
- **Decision:** Reuse that method on web and Android. Compare `AetherFeed-X.Y.Z-*.exe` / `aetherfeed-X.Y.Z-foss.apk`. Keep donate prefs and last-check timestamps device-local. Settings remains an opt-out of the daily fetch. Never mix donate with the update dialog.
- **Alternatives considered:** Keep tag-based About status as the only prompt (rejected — template tags are not product installers). Put donate on the install dialog (rejected — dark pattern).
- **Consequences:** First run is silent. A later version shows one optional Venmo note. Failed GitHub fetches stay silent.

### 2026-08-19 — Library tree, Unified pick, share, cache retain
- **Status:** Accepted
- **Context:** News / Podcasts / Boards lived on a bottom mode bar. Folder taps only highlighted. Cache trim only touched headline indexes.
- **Decision:** Keep three `AppDestination` values. Unified is `LibraryPick.Unified` in the left tree. Chevron expands; the label selects All / Folder / Source / Unified timelines. Share lives on the action bar. Settings cache retain drops unstarred offline blobs after 30 days or the next successful sync (Refresh-all if Drive is off). Starred ids are never deleted.
- **Alternatives considered:** Fourth `AppDestination` for Unified (rejected — spec is three modes). Episode browser in this row (deferred — podcast All is still shows).
- **Consequences:** Prev/next stay in the current pick list. Action-bar kind follows the focused Unified row. Headline history policy is unchanged.

### 2026-08-19 — Recents privacy cover
- **Status:** Accepted
- **Context:** The Android app switcher can snapshot article text, lock UI, and the library. Windows has no equivalent recents card.
- **Decision:** Always set `FLAG_SECURE` on `MainActivity` (lock included) and `setRecentsScreenshotEnabled(false)` on API 33+. Web/desktop paints a black cover on `visibilitychange` / `blur`. Document that Alt-Tab can still capture a pre-cover frame.
- **Alternatives considered:** Blur-only recents (rejected — needs proprietary or OEM APIs). Settings toggle (out of scope). Windows `WDA_EXCLUDEFROMCAPTURE` (not in parity scope).
- **Consequences:** Users cannot screenshot the Android app. Recents itself is not UI-testable; `FLAG_SECURE` on the window is the gate.

### 2026-08-19 — Skip already-fetched article bodies
- **Status:** Accepted
- **Context:** Prefetch treated any body under 400 characters as a miss, and empty extracts were not persisted, so every launch re-downloaded unread full text. Prefetch also RSS-refreshed feeds whose in-memory index looked empty.
- **Decision:** Persist a `fetched` marker (Android) or any `af-lock-articles` row (web) after each attempt, including failures. The next prefetch skips those ids. Green dots still require a usable reader body. `headlinesFromAll` loads stored indexes only; empty feeds refresh on Refresh, opening that feed, or the background worker.
- **Alternatives considered:** Retry failed extracts on every launch (rejected — that is the bug). Refresh empty indexes during prefetch (rejected — repeats 50+ RSS pulls when the index is missing).
- **Consequences:** Opening a story can still retry a short body. New headlines after Refresh are fetched once.

### 2026-08-19 — Desktop seed on Android + cache every unread
- **Status:** Accepted
- **Context:** Phone only had the Smoke seed. PC had 66 feeds in 24 folders. Prefetch only cached the open feed.
- **Decision:** After unlock, Android applies plaintext `files/seed-library.json` (merge, skip duplicate URLs, drop smoke-only HN/NPR if they are not in the seed). Unread full-text prefetch walks every news source under Wi-Fi and history rules. Refresh in the top bar / pull-to-refresh refreshes all news feeds.
- **Alternatives considered:** Keep auto-smoke when seed is missing (rejected). Drive-only library copy (not needed; seed file is the documented adb path).
- **Consequences:** Unlock once after a seed push. Encrypted desktop vault files are still skipped.

### 2026-08-19 — PIN keyboard, folder drawer, cache dot
- **Status:** Accepted
- **Context:** Unlock used a generic password field so PIN did not open the numeric keyboard. Narrow Android hid folders above a long list or behind the reader. HN RSS stubs painted article/comment URLs. Sort was a text button.
- **Decision:** Persist lock `kind` on the wrap envelope. PIN uses `NumberPassword` / `inputmode=numeric`; passphrase uses the password keyboard. Narrow News is a left-edge drawer (plus a folders button). Sort lives behind a filter icon. Reader rejects stub bodies; headlines show a green cached dot.
- **Alternatives considered:** Default unknown wraps to passphrase (rejected: this vault is PIN). Stack the tree above the list on phones (rejected: users could not find folders). Keep comment URLs as metadata (rejected).
- **Consequences:** Existing wraps without `kind` show PIN/passphrase chips. Article swipe starts past the left edge so the drawer can open.

### 2026-08-19 — Swipe, oldest-first, nav unread, instant cache
- **Status:** Accepted
- **Context:** Users wanted article-to-article swipe, oldest-first sort, unread on the three mode icons, no pane titles/import/hide/smoke on News, refresh in the top bar, and no URL/link placeholder while cache loads.
- **Decision:** Sort and swipe walk the same list. Opening an uncached article cancels prefetch, hydrates that id, then continues with the next row. Cached HTML paints immediately; uncached shows title only. Import lives in Settings. Unread badges sit on News / Podcasts / Boards.
- **Alternatives considered:** Keep hide-sources (rejected). Auto smoke feeds when empty (rejected). Use RSS HTML with links as a loading placeholder (rejected).
- **Consequences:** Sprint 19 swipe-to-mark-read remains open. `j`/`k` on desktop move between articles.

### 2026-08-19 — Reading-mode image cache + unread prefetch
- **Status:** Accepted
- **Context:** Images could include share icons; opening an article re-fetched; there was no unread prefetch or thumbnail.
- **Decision:** Extract reading mode first, drop social/tracker/tiny icons, then fetch remaining images in document order into the encrypted article cache. Prefetch unread rows with a determinate progress bar (width %, no animation). First remaining image is the timeline thumbnail.
- **Alternatives considered:** Fetch all page images then extract (rejected: pulls share sprites). Indeterminate spinner (rejected: fails when animations are off).
- **Consequences:** Cached stories open from ciphertext. Sprint 19 swipe/`j`/`k` stay open.

### 2026-08-19 — Collapsed folders, hide sources, resizable panes
- **Status:** Accepted
- **Context:** The left column opened every category at once, desktop mode types plus the tagline ate the top of the News rail, and pane widths were fixed.
- **Decision:** Folders start collapsed and persist expand/collapse. Desktop mode nav is a bottom bar like Android; the greeting lives in About. Hide sources and drag-resize pane weights persist (`af-news-chrome` / DataStore `news_chrome`). Drag updates CSS/weights live and writes storage on pointer up.
- **Alternatives considered:** Horizontal mode chips in the header (rejected: Android already uses a bottom bar). Shared Compose/DOM widgets (rejected: UI_PARITY IDs only).
- **Consequences:** Opening a folder no longer clears the current feed. Sprint 19 density work stays open.

### 2026-08-19 — Reading-mode only + desktop lock remount
- **Status:** Accepted
- **Context:** Opening a story still felt like a webpage (site CSS/links; Android WebView could fetch). Desktop idle lock expired the vault key without remounting the PIN form, so users had to press F5.
- **Decision:** Reader shows extracted text + cached images only, themed with app tokens; links/CSS/iframes stripped; Android WebView blocks network and uses `about:blank`. Session idle expiry notifies the shell immediately; pointer/key and Tauri window focus remount the lock form.
- **Alternatives considered:** Compose-only Android reader (rejected this slice: themed WebView with a network block keeps HTML lists/images). Drop extract-from-URL (rejected: short RSS still needs a one-shot HTML fetch that is never displayed).
- **Consequences:** No in-app browser. PIN appears without refresh when the 2-minute idle timer fires.

### 2026-08-19 — Cached headlines, Wi-Fi policy, source tree
- **Status:** Accepted
- **Context:** Headlines vanished after process death; refresh ignored metered links; folders were a horizontal chip row so categories scrolled off the left column.
- **Decision:** Persist trimmed article lists in the encrypted cache (`article-index` / `af-lock-article-index`). Settings default to Wi-Fi only (cellular is an explicit choice) and that flag gates refresh, full-text GET, and image fetch. The left column is a vertical folder+feed tree on both apps.
- **Alternatives considered:** Room article table this slice (rejected: keep history trim + encrypted files). One shared flag with podcast downloads (rejected: podcast web still defaults to allow cellular).
- **Consequences:** Cached headlines survive restart after unlock. Sprint 19 density (thumbnails, swipe, `j`/`k`) stays open.

### 2026-08-19 — Android library seed, hourly refresh, history caps
- **Status:** Accepted
- **Context:** The desktop vault already held 66 feeds; the phone needed the same library plus user-set refresh and history limits without committing that file.
- **Decision:** One-shot `files/seed-library.json` import after unlock; WorkManager periodic refresh with a 1-hour minimum (3/6/12/24 h options); history by last N articles (default 10) or last 1–30 days. Persist optional `Feed.folder` via Room v2.
- **Alternatives considered:** Commit OPML into git (rejected: hygiene / personal library). Sub-hour refresh (rejected: user asked hourly as shortest). Persist every article in Room this slice (rejected: in-memory cache + trim is enough).
- **Consequences:** Settings owns interval and history chips. Background refresh no-ops while the vault is locked. HUMAN OPML/Drive and ADB smoke rows stay open.

### 2026-08-18 — Combined gap board and 3-mode IA lock
- **Status:** Accepted
- **Context:** Feature matrix and unusable desktop stub needed one prioritized board and a BUILD_PLAN that can dispatch every missing P0/P1 row.
- **Decision:** One canvas (`aetherfeed-gap-board.canvas.tsx`), three modes (News / Podcasts / Boards), Settings/About overlays only. Spec holes locked into existing feature docs plus `docs/features/app-shell.md`. Parallel tables split into Sprint 12 + 12b because `check-build-plan-parallel` caps `agent_count_target` at 8.
- **Alternatives considered:** Separate UI matrix canvas (rejected: one board). `agent_count_target: 16` on a single sprint (rejected: gate max 8). Fourth Settings mode (rejected: overlay).
- **Consequences:** Sequential 11 is done. Sprint 12/12b dispatch 16 P0+P1 AGENT scopes. HUMAN Drive/OPML and ADB smoke stay open.

### 2026-08-18 — Drive appdata feed-source sync for test builds
- **Status:** Accepted
- **Context:** Phone and PC need a shared subscription list without writing My Drive or uploading the passphrase.
- **Decision:** Sync only `aetherfeed-feeds.enc` in Drive Application Data (`drive.appdata`). Seal with AES-256-GCM + PBKDF2 (allowed AEAD alternate). Merge by normalized feed URL. Desktop and Android use loopback PKCE on `http://127.0.0.1:17890`. Composio confirmed the Google account is live; a Desktop OAuth client still has to be authorized in the apps (Cloud Console sign-in is HUMAN).
- **Alternatives considered:** Visible My Drive folder via Composio (rejected: ADR-0003). Play Services Sign-In (rejected: FOSS). Device-code flow only (rejected: Drive appdata is unreliable on TV clients).
- **Consequences:** Settings has Connect / Sync now / Disconnect. Debug builds share a local passphrase via gitignored `local.properties` and `examples/web/.env.local`.

### 2026-08-18 — Automate remaining HUMAN seed steps
- **Status:** Accepted
- **Context:** Seed left GitHub repo creation, security defaults, ADR sign-off, and release/donation config as HUMAN rows.
- **Decision:** On request, treat those rows as automatable: fill `edwardlthompson/aetherfeed` + GitHub Sponsors, mark ADR-0001/0002/0003 and the vault/Hilt approach approved, create the public GitHub repo, and run `setup-github-repo`.
- **Alternatives considered:** Leave settings for a later UI pass (rejected: the user asked to automate them). Extra donation providers (rejected: ask first).
- **Consequences:** Child repo is `edwardlthompson/aetherfeed`. Sprint 1 implementation of Room/Hilt is still an AGENT row.

### 2026-08-18 — Seed AetherFeed from agent-project-bootstrap
- **Status:** Accepted
- **Context:** Empty workspace needed a FOSS local-first reader for Android and Windows without telemetry or third-party client branding.
- **Decision:** Bootstrap with `--stack multi`, prune Python/Node/Go/Lightroom, keep Android + web + Rust, add a Tauri 2 desktop wrapper. Shared TypeScript models are the schema lock; Android mirrors them in Kotlin. SQLCipher contract and `SyncProvider` (Drive appdata, WebDAV, local-only) are defined before feature work. MIT license; no AGPL vendored.
- **Alternatives considered:** Electron (rejected: larger binary; log in this file if Tauri blocks a required feature). Kotlin Multiplatform desktop (rejected: product brief prefers Tauri). Extra cloud providers or accounts (rejected: ask first).
- **Consequences:** Sprint 0 identity is in place. Room/Hilt production wiring, feed parsers, and real provider OAuth remain open BUILD_PLAN rows. Child GitHub repo + CI on `main` are HUMAN.

### 2026-08-18 — Ship v0.21.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M38+M39. Pre-release green on `f54927e`; feat `8eab392` then `df322af` after `rp_merge_status` import failed once `PYTHONPATH` was stripped.
- **Decision:** Push feat + cwd-relative import fix; wait Ubuntu + Windows upgrade-sim; admin-merge Release Please #69 to **v0.21.0**. Fold leftover Unreleased onto the PR as comments. Archive M39 and empty Unreleased after merge (fold does not rewrite the RP branch). Codex skipped (no key/CLI).
- **Alternatives considered:** Leave leftover Unreleased under `[0.21.0]` (rejected: next `/ship` fails first+empty gates). `pull_request_target` for RP checks (rejected).
- **Consequences:** Template at 0.21.0. Next `/ship` should empty Unreleased in the prepare commit so RP does not carry bullets. SBOM attaches via `release` published workflow.

### 2026-08-17 — M39 /ideas Windows PATH + ship hygiene
- **Status:** Accepted
- **Context:** Fifth `/ideas` pass. Git Bash still missed `gh`; inherited `PYTHONPATH=scripts/lib` broke validate-bootstrap; leftover Unreleased blocked RP merge; Q&A REST create often SKIP'd.
- **Decision:** Shared `resolve-tools.sh` prepends Windows tool dirs and unsets `PYTHONPATH`. `agent-run` uses `child_env()`. Fold Unreleased onto the RP PR comment then empty. No `environment:` on CI/Security/CodeQL. GraphQL list + REST/GraphQL create for Q&A with a one-line HUMAN fallback. Archive M38; name Windows check on child Sprint 0 AUTO.
- **Alternatives considered:** Document PATH-only (rejected: every `gh` script still fails). `pull_request_target` for RP checks (rejected: untrusted checkout). Fail setup when Q&A API 422s (rejected: Settings fallback is enough).
- **Consequences:** Source `resolve-tools.sh` before `command -v gh`. Never export `PYTHONPATH=scripts/lib`. `/ship` comments leftover notes then empties Unreleased locally.

### 2026-08-17 — M38 /ideas ship-hardening
- **Status:** Accepted
- **Context:** Fourth `/ideas` pass after v0.20.0. Live `main` still required only five checks; Windows `jq` and leftover Unreleased still bit `/ship`.
- **Decision:** Fail `pre-release-gate` on missing Windows upgrade-sim; apply that check via `setup-github-repo`. Python-only `TEMPLATE_INDEX`. Skip RP wait on `ACTION_REQUIRED`. Split allowlisted `scripts/lib` to ≤150. Empty-Unreleased gate before RP merge. Archive Coach/M37/M36. Token on upgrade-sim jobs.
- **Alternatives considered:** Keep jq + CR strip (rejected: two paths). Leave lib allowlist (rejected: token-economy lie).
- **Consequences:** `/ship` now fails until protection matches the script. New `scripts/lib` files stay ≤150 with an empty allowlist.

### 2026-08-17 — Ship v0.20.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after three `/ideas` implement-all rounds. First pre-release gate was green on `01e21fc`; feat `14811be` then failed upgrade-sim (stamped purpose, pruned Android link, Windows `jq` CRLF).
- **Decision:** Keep portable-purpose assert on the template repo only; ignore doc links into pruned `modules/`/`examples/`; strip CR from `jq` paths in `validate-template-index`. Push fixes, wait for Ubuntu + Windows upgrade-sim, admin-merge Release Please #68 to **v0.20.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Skip full validate after prune (rejected: hides real child-repo doc breaks). Drop Windows upgrade-sim from required checks (rejected: that was the point of the ideas pass).
- **Consequences:** Template at 0.20.0; `/ship` must treat Windows `jq.exe` CRLF and post-prune links as first-class gates. SBOM attaches via `release` published workflow.

### 2026-08-17 — Implement /ideas backlog (required Windows check, coach twin)
- **Status:** Accepted
- **Context:** Third `/ideas` pass. Windows upgrade-sim existed but `/ship` and branch protection did not name it.
- **Decision:** Add the Windows job to required checks; ship `docs/help/COACH.md`; health notes dirty Unreleased; skip weekly AUTO rows after a green weekly-health run; Codespaces `verify.sh`; citation `date-released`; pin setup-python SHA; split `build_sprint` and gate new `scripts/lib` files at 150 lines (allowlist pre-existing oversize modules).
- **Alternatives considered:** Split every lib file in one pass (rejected: too much risk for `/build`). Fail file-limits on allowlisted modules (rejected: would block unrelated work).
- **Consequences:** `/ship` waits on Windows upgrade-sim once the job has run on HEAD. New `scripts/lib` modules must stay ≤150 lines.

### 2026-08-17 — Implement /ideas backlog (health, Windows CI, links)
- **Status:** Accepted
- **Context:** Second `/ideas` pass after the first eight items shipped locally. Health still pointed at child Sprint 0 on this template.
- **Decision:** Auto lane uses maintainer board when `bootstrap.config.json` still describes this template. Skip `pwsh` when missing; add `windows-latest` upgrade-sim. Split gate hints to JSON. Extend doc-link gate to root `*.md` + pre-commit. Best-effort Q&A category after Discussions enable.
- **Alternatives considered:** Keep auto=child on the template (rejected: wrong next step). Fail upgrade-sim without `pwsh` (rejected: bash path already proved). Require Q&A API success (rejected: Settings fallback).
- **Consequences:** `/coach` and `/ideas` on this repo name Ongoing Maintenance, not init-project. Child repos with their own purpose stay on the child playbook.

### 2026-08-17 — Implement /ideas backlog (8 items)
- **Status:** Accepted
- **Context:** `/ideas` ranked eight in-scope template items after v0.19.0. User asked to implement all.
- **Decision:** Ship Windows pyrepl env, CITATION.cff version sync, glossary, portable stamp copy, verify.sh hints, opt-in welcome issue, docs link gate, and Discussions enablement from `setup-github-repo`.
- **Alternatives considered:** Cursor-only purpose (rejected: portability). Welcome issue on by default (rejected: matches other post hooks). Fail setup if Discussions API cannot toggle (rejected: [HUMAN] Settings fallback).
- **Consequences:** `post_welcome_issue` stays false; `/ship` regress should finish on This Computer; glossary is REQUIRED.

### 2026-08-16 — Ship v0.19.0 (/ship)
- **Status:** Accepted
- **Context:** Coach layer (`2f77fb9`) plus portable first-run polish were unpushed; first pre-release CI wait failed because HEAD had no Actions run.
- **Decision:** Push feat commits, wait for CI/Security/CodeQL, re-run `pre-release-gate`, merge Release Please #67 to **v0.19.0**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold for Codex (rejected: skip is allowed). Invent per-tool rulebooks (rejected: AGENTS.md SoT).
- **Consequences:** Template at 0.19.0; SBOM attaches via `release` published workflow; batch commands are 24 atomic + 5 super.

### 2026-08-16 — Portable first-run (any agent IDE)
- **Status:** Accepted
- **Context:** First-time users needed a scripted tour and readable gate failures; the template was Cursor-weighted while Windsurf, Antigravity, and others already read `AGENTS.md`.
- **Decision:** Keep `AGENTS.md` Sacred. Generate thin pointers (`GEMINI.md` pointer-only, Windsurf, Cline, Aider, Continue). Ship `/tour` plus `docs/help/TOUR.md`. Add adapter drift gate, VS Code tasks, SUPPORT.md, CITATION.cff, good-first-issue, live badges, Codespaces link.
- **Alternatives considered:** Duplicate full rules into `.windsurfrules` / `.agents/agents.md` (rejected: drift and a second SoT). Register `/why` (rejected: `/coach` synonym only).
- **Consequences:** Edit `AGENTS.md` then `--sync-adapters`. Never put real rules in `GEMINI.md`.

### 2026-08-16 — Template Excellence / Coach Layer
- **Status:** Accepted
- **Context:** The template already had gates, memory, and Golden Paths; new users still got files without the industry *why*.
- **Decision:** Add `docs/BEST_PRACTICES.md` + `docs/FIRST_30_DAYS.md`, `/coach` + Welcome Tour, init what/why summary, optional FUNDING.yml/topics, and optional `justfile`s. Do not require `just` in CI. Do not register a second `/why` command.
- **Alternatives considered:** Fold the 30-day list into BEST_PRACTICES (rejected: token bloat); husky instead of just (rejected: pre-commit already covers hooks).
- **Consequences:** Batch-command count is 23 atomic + 5 super. `/bootstrap` ends with a tour. Child product READMEs gain For humans / For agents sections.

### 2026-08-16 — M37 gap close (verify, env, hooks)
- **Status:** Accepted
- **Context:** Checklist audit found core governance/CI present; remaining gaps were docker preflight, unimplemented post-hook flags, no root verify command, no env schema, no commit-msg enforcement, no Dockerfile, and no `.agent/memory` indexes.
- **Decision:** Extend the existing engine. `scripts/verify.sh` is the harness. Env validation is stack-agnostic JSON schema. Skills/memory under `.agent/` are indexes to `.cursor/skills/` and `DECISION_LOG.md` / `KNOWLEDGE_BASE.md`. Post install/test/git-init stay opt-in. Conventional Commits via pre-commit `commit-msg`, not Node-only commitlint.
- **Alternatives considered:** Duplicate skills into `.github/skills/` (rejected); husky + lint-staged (rejected: pre-commit already covers all stacks); auto-commit after init (rejected: destructive-ops).
- **Consequences:** `validate-bootstrap` requires env schema, Dockerfile, `.agent/` indexes, and `verify.sh`. Feature-gate fails if `.env.example` drifts from `env.schema.json`.

### 2026-08-16 — M36 bootstrap standards (AGENTS.md + lifecycle)
- **Status:** Accepted
- **Context:** Audit asked for a generator-style AGENTS.md engine, SDD stubs, security-by-default, manifest, and pre/post hooks. The repo already shipped SECURITY.md, CONTRIBUTING.md, CI, Dependabot, issue/PR templates, and `init-project`.
- **Decision:** Keep the GitHub Template + `init-project` model. Expand `AGENTS.md` as the canonical spec; generate thin Cursor/Claude/Copilot adapters; add `docs/spec.md` / `docs/plan.md`; add `bootstrap.config.json` plus preflight/post hooks and `PROJECT_CHECKLIST.md`. MIT remains default; Apache-2.0 is an init option for child repos. Do not auto-commit or auto-install deps.
- **Alternatives considered:** Separate yeoman-style generator CLI (rejected: would fork the template model); GitHub `- [ ]` checkboxes on the new checklist (rejected: repo-wide 🔲/✅/❌ convention).
- **Consequences:** `validate-bootstrap` requires SDD stubs, adapters, and engine unit tests. Child `AGENTS.md` stays Sacred on upgrade; adapters are Canon via `--sync-adapters`.

### 2026-08-16 — Ship v0.18.3 (/ship)
- **Status:** Accepted
- **Context:** Dependabot #64 Compose BOM bump on main; RP #66 already open
- **Decision:** `/ship` autofix + pre-release gate, then merge Release Please #66 to **v0.18.3**. Codex skipped (no key/CLI).
- **Alternatives considered:** Hold BOM for a later patch (rejected: CI including instrumented Android already green)
- **Consequences:** Template at 0.18.3; SBOM attaches via `release` published workflow

### 2026-08-16 — Ship v0.18.2 (/push)
- **Status:** Accepted
- **Context:** M35 HUMAN Scorecard/Dependabot/radar already on `main` @ `23254e8`; CI/Security/CodeQL green; RP #63 open
- **Decision:** Merge Release Please #63 to **v0.18.2** after local maintainer + pre-release gates (no extra prepare commit)
- **Alternatives considered:** Wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.2; next quarterly radar 2026-11-15; Dependabot #64 left open

### 2026-08-15 — M35 Scorecard SARIF + Dependabot + radar
- **Status:** Accepted
- **Context:** Open HUMAN items after v0.18.1: Scorecard PinnedDependencies / TokenPermissions / VulnerabilitiesID; Dependabot #58–#61; quarterly radar (last report 2026-06-30)
- **Decision:** Job-scope write tokens (`permissions: read-all` at workflow level). Keep `@vX.Y.Z` for GitHub-owned actions. Treat VulnerabilitiesID as stale (hono/nanoid/postcss already patched in 0.18.0). Merge green Dependabot PRs after rebase; rebase #61 (stale web lockfile). Radar max new score is 6 — no BUILD_PLAN row.
- **Alternatives considered:** SHA-pin every `actions/*` (rejected: conflicts with `validate-workflow-actions` + existing policy); add Design Mode / Canvas now (rejected: score 6, below ≥9 suggest threshold)
- **Consequences:** TokenPermissions should clear on next Scorecard run; PinnedDependencies remain accepted; next quarterly radar due 2026-11-15

### 2026-08-15 — Ship v0.18.1 (/push)
- **Status:** Accepted
- **Context:** M35 Windows Store `python3` hang + About-gate restore ready; first `PY="py -3"` broke `"$PY"` in Dependabot count
- **Decision:** Resolve `PY` to `sys.executable` from `py -3`; merge Release Please #62 to **v0.18.1** after CI green on `b4fca9c`
- **Alternatives considered:** Leave `PY="py -3"` and unquote all callers (rejected: `"$PY"` is the safe pattern); wait for RP auto-merge (blocked: no checks on release-please branch)
- **Consequences:** Template at 0.18.1; Scorecard SARIF and Dependabot PRs #58–#61 stay HUMAN

### 2026-08-15 — Audit M35 Windows Python resolver
- **Status:** Accepted
- **Context:** `/ship` autofix hung because `command -v python3` resolved to the Microsoft Store stub under `WindowsApps`
- **Decision:** Add `scripts/lib/resolve-python.sh` (skip Store stub; prefer `py -3`) and source it from gate/autofix scripts; restore About slice from `git checkout HEAD` if the verify-about backup is missing
- **Alternatives considered:** Document-only workaround (rejected: every Windows gate still hangs); require a `python3` symlink in PATH (rejected: Store alias still wins)
- **Consequences:** Local gates on This Computer no longer stall on the stub; HUMAN still owns Scorecard SARIF and Dependabot PRs #58–#61

### 2026-08-15 — Ship v0.18.0 (/ship)
- **Status:** Accepted
- **Context:** M34 prior-art steals ready; pre-release gate blocked on High `extract-zip` (no upstream patch) via LHCI → puppeteer-core
- **Decision:** Override `@puppeteer/browsers` >=3.2.0 (uses `modern-tar`); lock optional peer `proxy-agent` >=8.0.2 so CI `npm ci` matches; bump `hono`/`postcss`/`nanoid`; merge Release Please #56 to **v0.18.0**
- **Alternatives considered:** Dismiss extract-zip as dev-only (rejected: gate requires zero High); vendor a patched fork (rejected: no patch exists)
- **Consequences:** Template at 0.18.0; honesty labels + scratchpad/handoff ship; Codex skipped (no key/CLI)

### 2026-08-14 — Prior-art thin steals (M34)
- **Status:** Accepted
- **Context:** Compared CopperDogma, Barony, Sciensoft, and wshobson/agents against this Cursor-first template. Need mechanisms without vendoring those trees or an 80k playbook.
- **Decision:** Ship honesty labels, parallel handoff stub, Canon/Mixed/Sacred upgrade column, OWASP LLM walk, scratchpad reset, optional marketplace pointer, and bootstrap-doctor alias. Number as **M34** (plan draft said M30; that sprint is already archived).
- **Alternatives considered:** Vendor Barony/`baron` (rejected: second product + PyPI dep); install wshobson catalog by default (rejected: token bloat); Sciensoft one-file playbook (rejected: 300/150 caps).
- **Consequences:** Hooks stay fail-open and labeled; child `AGENTS.md` / init prompt stay Sacred; no new CI scanner or marketplace install on the FOSS default path.

### 2026-08-12 — Ship v0.17.0 branding kit (/ship)
- **Status:** Accepted
- **Context:** Child repos need replaceable logos/colors and pitch-quality READMEs without overwriting the template README
- **Decision:** Ship `branding/` pack + mode-gated `generate-project-readme.py` (`template` preview only; `product` writes root README); extend token sync for official-colors and asset distribution; merge Release Please #55 to **v0.17.0**
- **Alternatives considered:** Generate logos from tokens only (rejected: humans replace art files); always overwrite root README (rejected: clobbers template guide)
- **Consequences:** Sprint 0 fills `product.json` then generate; upstream keeps `mode: template`; store PNGs remain human/ADB exports

_Seed template ADR: `docs/adr/0000-template-baseline.md`. Child repos use `docs/adr/0001-core-architecture.md`._

### 2026-08-10 — Ship v0.16.0 (/ship)
- **Status:** Accepted
- **Context:** Need third-party review + broader autofix before release; `/ship` should stay one command
- **Decision:** Codex read-only reviewer (opt-in CI + `/codex-review`) feeds `CODE_REVIEW.md` → Cursor `/fix`; expand `/prerelease` with multi-stack autofix; merge Release Please #51 to **v0.16.0**
- **Alternatives considered:** Codex writes patches in CI (rejected: destructive-ops / FOSS spend control); chain Codex into every `/maintain` (rejected: API cost)
- **Consequences:** `/ship` runs autofix + optional Codex + hard gate; enable Codex CI by copying workflow example + `OPENAI_API_KEY`

### 2026-08-01 — Ship v0.15.2 (/ship)
- **Status:** Accepted
- **Context:** Plan Mode left risks as open questions; Dependabot High blocked pre-release (js-yaml, then postcss)
- **Decision:** Require Issue→Resolution Critique in always-applied rules + `/plan`; override patched npm transitive CVEs; merge Release Please #50 to **v0.15.2**
- **Alternatives considered:** Soft "list risks" Critique (rejected: humans still had to chase resolutions); defer brace-expansion/postcss (rejected: pre-release gate requires zero Critical/High)
- **Consequences:** Agents must bake mitigations into plan todos; template at 0.15.2 with SBOM release assets

### 2026-07-22 — Ship v0.15.0 (/ship)
- **Status:** Accepted
- **Context:** `/ship` after M33 + local-first compute; first CI failed on duplicate `## [Unreleased]`; web tests failed on Node 25+ localStorage stub
- **Decision:** Polyfill Storage in vitest setup (KB-011); collapse stale Unreleased; merge Release Please #37 to **v0.15.0**
- **Alternatives considered:** `--no-webstorage` only (rejected: may break older Node CI); leave duplicate Unreleased (rejected: gate hard-fail)
- **Consequences:** Template at 0.15.0 with Cursor worktrees/permissions/skills/plugin pack and local-first parallelism

### 2026-07-21 — Local-first compute on This Computer
- **Status:** Accepted
- **Context:** Agents defaulted toward serial work or Cloud handoff even when the desktop has many cores
- **Decision:** Ship `local-compute.mdc` + sessionStart CPU reminder; parallelize independent `validate-bootstrap` checks via `run_checks_parallel.py` (`BOOTSTRAP_CHECK_JOBS`); pytest-xdist `-n auto`; Gradle `--parallel`; document `/scope` + worktrees/`/best-of-n` as the local default over Cloud Agents
- **Alternatives considered:** Always Cloud Agents for parallelism (rejected: wastes local hardware and costs credits); unbounded bash `&` in validate-bootstrap (rejected: harder error aggregation on Windows)
- **Consequences:** Quick bootstrap checks use all cores (e.g. jobs=CPU count); agents are steered to concurrent Task/worktrees when local

### 2026-07-21 — Cursor 3.9–3.11 FOSS integration (M33)
- **Status:** Accepted
- **Context:** Cursor added native worktrees setup, Auto-review `permissions.json`, Skills direction, CLI/GHA, side chats, Design Mode, cloud conversation hooks, Automations, and plugin packaging; registry lagged at 2026-06-30
- **Decision:** Ship FOSS live `worktrees.json` + fail-soft OS setup, committed `permissions.json` (dual layer with hooks), four new skills + checker atomic update, CLI workflow under `.github/workflow-examples/` (never auto-run), plugin via pack-to-`dist/cursor-plugin` (no repo-root symlink); keep commercial as examples (cloud hooks, Automations recipes, Bugbot Autofix map)
- **Alternatives considered:** Custom plugin paths into `.cursor/` (rejected: discovery risk); whole-repo plugin symlink (rejected: double-load); `.example.yml` under `workflows/` (rejected: GHA may load it); weaken shell hook for Auto-review (rejected: hooks stay hard FOSS enforcement)
- **Consequences:** `check-cursor-integrations` requires seven skills + worktrees/permissions; `/best-of-n` documented beside parallel-lock worktrees; Cloud Agents still ignore Run Modes

### 2026-07-12 — Pre-release gate Dependabot counter + FOSS MCP check
- **Status:** Accepted
- **Context:** `/push` pre-release `--strict` failed: Dependabot alerts API used unsupported `page=` form; FOSS integrations check failed whenever gitignored `.cursor/mcp.json` existed locally
- **Decision:** Count alerts via `gh api --paginate` query string; treat live `mcp.json` as OK unless `git ls-files` shows it tracked; multi-stack `--strict` skips missing optional toolchains
- **Alternatives considered:** Require `security_events` refresh always (rejected: false failures blocked release); ban local MCP (rejected: contradicts CURSOR_INTEGRATIONS activation)
- **Consequences:** Maintainer gates pass with local MCP enabled; Release Please #36 published v0.14.1

### 2026-07-12 — Dependabot automerge CI gap (M32)
- **Status:** Accepted
- **Context:** Merges via `GITHUB_TOKEN` (`app/github-actions`) do not start `push` workflows; `main` tip after Dependabot merges had zero CI runs; weekly health failed waiting for missing runs
- **Decision:** Prefer optional `AUTOMERGE_TOKEN` PAT for Dependabot/Release Please merge; add `workflow_dispatch` to CodeQL + Security Scan; `check-github-ci.sh --dispatch-if-missing` (weekly health uses it with `actions: write`); prefer Git Bash in `agent-run.py` on Windows
- **Alternatives considered:** Require PAT only (rejected: blocks FOSS template without secrets); SHA-pin all actions for Scorecard (deferred: conflicts with documented `@vX.Y.Z` policy)
- **Consequences:** Weekly health can self-heal missing runs; post-merge CI still needs HUMAN required-status-checks + optional PAT for true push triggers

### 2026-07-02 — Quiet agent shell (hooks Python + agent-run)
- **Status:** Accepted
- **Context:** Cursor Agent shell execution opened `.sh` hook and script tabs, stealing editor focus while users typed
- **Decision:** Migrate hooks to Python; add `scripts/agent-run.py` for agent gate invocations; ship `.vscode/settings.json` anti-reveal defaults; document KB-010
- **Alternatives considered:** Disable hooks globally (rejected: loses destructive-op guard); rewrite all scripts to PowerShell (rejected: scope); `pythonw.exe` for hooks (rejected: breaks stdout JSON)
- **Consequences:** Agent-facing commands no longer contain `.sh` paths; underlying bash scripts unchanged for CI/humans

### 2026-07-01 — Cursor hook smoke isolation (M31)
- **Status:** Accepted
- **Context:** M31 audit found `check-cursor-hooks.sh --smoke` false-pass when `.cursor-session-state.json` already listed `git push` in `destructive_ops_approved`
- **Decision:** Smoke test clears session approvals before deny assertion; validate hook scripts require shebang on line 1
- **Alternatives considered:** Ignore local session state in smoke (rejected: hides real deny-path bugs); require empty session file (rejected: breaks dev workflow)
- **Consequences:** `--smoke` is deterministic in CI and locally; invalid hook scripts fail validate-bootstrap early

### 2026-06-30 — Cursor hooks as enforcement layer (M30)
- **Status:** Accepted
- **Context:** M27 rejected `beforeSubmitPrompt` hooks; rules alone cannot block destructive shell commands at runtime
- **Decision:** Ship FOSS-safe project hooks (`beforeShellExecution`, `afterFileEdit`, `subagentStart`, `sessionStart`, `beforeMCPExecution`); fail-open guards; session `destructive_ops_approved` for `/push`/`/ship`; opt-out via `<!-- cursor-hooks: off -->`
- **Alternatives considered:** Prompt-rewrite hooks (rejected per M27); broad shell blocklists (rejected: blocks legitimate agent work)
- **Consequences:** `check-cursor-hooks.sh --smoke` in validate-bootstrap; complements `destructive-ops.mdc` without token bloat

### 2026-06-20 — Repo-wide checklist status markers
- **Status:** Accepted
- **Context:** BUILD_PLAN and scattered checklists used mixed ⬜ / `- [ ]` / ✅ formats; inconsistent in Markdown Preview vs source
- **Decision:** Standardize on 🔲 open · ✅ done · ❌ blocked emoji markers repo-wide; document in `BUILD_PLAN.md` legend and agent read order
- **Alternatives considered:** GitHub `- [ ]` task lists (rejected: poor Preview readability and agent parsing); keep ⬜ white square (rejected: visually similar to ✅ in some fonts)
- **Consequences:** All new checklist rows use emoji; `agent-progress.sh` accepts legacy ⬜ for child repos during transition

### 2026-06-18 — Release automation hardening (M29)
- **Status:** Accepted
- **Context:** v0.11.0 release lacked SBOM assets (GITHUB_TOKEN cannot chain `release` → `release.yml`); Release Please skipped `extra-files`; `health-check.yml` registered as path name caused 0-job push failures
- **Decision:** `release-please.yml` runs `sync-template-version.sh` on release PR branches and dispatches `release.yml` on `release_created`; rename workflow to `weekly-health-check.yml`; fix sync script for Windows Git Bash
- **Alternatives considered:** PAT with workflow scope for release chaining (rejected: secrets management); manual SBOM backfill only (rejected: repeated human step each release)
- **Consequences:** Release Please needs `actions: write`; future releases should ship SBOM assets without manual dispatch

### 2026-06-17 — Batch instruction templates (M27)
- **Status:** Accepted
- **Context:** Agents and child-repo owners needed repeatable shortcuts for bootstrap, verify, build, ship, and maintenance workflows without re-pasting long prompts
- **Decision:** Ship 25 slash commands in `.cursor/commands/` (20 atomic + 5 super), bare-word expansion via `batch-commands.mdc`, human cheat sheet at `docs/help/BATCH_COMMANDS.md`, registry at `docs/BATCH_COMMANDS.md`; `/push` and `/ship` grant explicit push approval
- **Alternatives considered:** `beforeSubmitPrompt` hook for bare words (rejected: Cursor API cannot rewrite prompts); single mega-doc for humans and agents (rejected: overwhelms first-time users)
- **Consequences:** `alwaysApply` rule adds ~25 lines per session; `check-batch-commands.sh` prevents registry drift; child repos cherry-pick via `UPGRADING_FROM_TEMPLATE.md`

### 2026-06-30 — Autonomous /build with grouped human section
- **Status:** Accepted
- **Context:** `/build` halted on HUMAN/ADB rows; humans needed a single review block after automation; child repos need scripted attempts before manual follow-up
- **Decision:** Add `build-sprint-status.sh`, `attempt-build-plan-row.sh`, and `HUMAN_BACKLOG.md` (failure-only); restructure BUILD_PLAN with `#### Human & device (after automation)`; AGENT/AUTO runs first, then automation attempts on grouped human rows
- **Alternatives considered:** Skip human rows entirely during /build (rejected: loses automation catalog value); keep human rows interleaved in Sequential (rejected: hard to review after automation)
- **Consequences:** Child repos must place HUMAN/ADB rows in the grouped section; `<!-- no-auto-approve -->` disables autonomous ADR ack

### 2026-06-13 — @lhci/cli npm overrides for transitive CVEs
- **Status:** Accepted
- **Context:** Lighthouse CI (`@lhci/cli`) bundles transitive dependencies (`tmp`, `uuid`) with known CVEs; no patched `@lhci/cli` release available at triage time
- **Decision:** Add npm `overrides` in `examples/web/package.json` forcing `tmp >= 0.2.6` and `uuid >= 11.1.1`; document in KB-007
- **Alternatives considered:** Dismiss Dependabot alert (rejected: hides real risk); remove Lighthouse CI job (rejected: loses performance gate)
- **Consequences:** Lockfile must be regenerated after override changes; overrides should be removed when `@lhci/cli` ships fixed dependencies

### 2026-06-13 — Ship all optional ecosystem modules (M3)
- **Status:** Accepted
- **Context:** Sprint M3 asked whether to ship Lightroom, Rust, and Go optional modules in the template maintainer repo
- **Decision:** Ship all three with Golden Path stubs, MODULE.md guides, and path-gated CI jobs (`lightroom`, `rust`, `go`) that skip when child repos remove the directories
- **Alternatives considered:** Lightroom-only (rejected: Rust/Go stubs are low-cost and popular); defer all optional modules (rejected: COMPLETED_TASKS M3 work already landed)
- **Consequences:** Template CI runs more jobs on `main`; child repos can delete unused `examples/` folders to skip jobs via `hashFiles` guards
## Autonomous /build approval (2026-08-18T15:40:25+00:00)

- Confirmed file-only reader import for v0.1: OPML/JSON/Takeout zip only; no Google or Inoreader OAuth.
