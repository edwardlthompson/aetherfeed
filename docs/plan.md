# Implementation Plan

> Active work lives in `BUILD_PLAN.md`.
> Status: 🔲 open · ✅ done · ❌ blocked.

## Milestone — Seed

| Task | Owner | Tests / fallback |
|------|-------|------------------|
| ✅ Init + AetherFeed identity | AGENT | `validate-bootstrap.sh --quick` |
| ✅ Shared models + unread tests | AGENT | Android `UnreadCountTest`, web `unread.test.ts` |
| ✅ Tauri desktop shell | AGENT | `examples/desktop` compile after icons |
| 🔲 Room + SQLCipher + Hilt | AGENT | Instrumented vault open |
| 🔲 GitHub repo + CI on main | HUMAN | `check-github-ci.sh --wait 300` |

## Next feature

1. News (`docs/features/news.md`) after the vault is wired
2. Podcasts, booru, notifications, then sync
3. After each AGENT step: `python scripts/agent-run.py watch-agent-gates --once --autofix`
