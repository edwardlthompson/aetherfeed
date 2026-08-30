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

1. Sprint 8 — Reader source import (`docs/features/reader-import.md`)
2. After each AGENT step: `python scripts/agent-run.py watch-agent-gates --once --autofix`
