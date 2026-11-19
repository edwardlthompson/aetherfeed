# Shared KMP / JVM modules (Sprint 56+)

Layout from [`docs/migration/TECH_DECISION.md`](../docs/migration/TECH_DECISION.md):

| Module | Role |
|--------|------|
| `domain/` | Models, news HTTP contract, SyncEnvelope scaffold |
| `vault/` | Table-name SoT + keychain interface (SQLDelight next) |
| `ui/` | Shared Compose theme (Sprint 57+); Android library first, CMP multi-target next |
Wired into `examples/android` via `settings.gradle.kts` project dirs. Full `android`/`ios`/`desktop` KMP targets land as cutover progresses; JVM libraries unblock shared tests now.
