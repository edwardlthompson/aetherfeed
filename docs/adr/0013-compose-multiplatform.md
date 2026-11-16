# ADR-0013: Compose Multiplatform shared UI and Kotlin domain

- **Status:** Accepted
- **Date:** 2026-11-16
- **Deciders:** Edward (go with agent picks on the 18 Phase 0 questions); recorded in [`docs/migration/INVENTORY.md`](../migration/INVENTORY.md)
- **Supersedes:** [ADR-0001](0001-core-architecture.md) (KMP rejected vs Tauri; TS models as the cross-language contract)

## Context

Phase 0 inventory: Android Compose is the real product UI; desktop is a second Vite/Tauri UI; iOS is absent. Edward locked feature and design parity across Android, iOS, Windows, macOS, and Linux, with one shared UI, encrypted vault + OS keychain on every native shell, podcast lockscreen/background audio, and no public PWA (KB-018). ADR-0001’s Tauri+TS split cannot meet that brief.

## Decision

1. **Shared UI:** Jetpack Compose Multiplatform (Material 3), cloned from today’s Android look. No lockstep Compose + web product UI.
2. **Shared logic:** Kotlin Multiplatform domain (feeds, vault, news ingest, LAN pool client, envelope crypto). Clean/MVI stays; Hilt stays Android-only at the application edge.
3. **Desktop:** Compose for Desktop (JVM) packages Windows, macOS, and Linux. **Replace** Tauri as the shipping desktop shell (see [`TECH_DECISION.md`](../migration/TECH_DECISION.md)).
4. **Web:** `examples/web` is not a product surface. KB-018 stands. No hosted reader on aetherfeed.org.
5. **Contracts that remain:** SQLCipher-class encrypted DB (ADR-0002), LAN pool v2 + Hermes-on-Android (ADR-0011), Drive/WebDAV still later (ADR-0003, not v1), no Jazz/Subduction (ADR-0012).

## Alternatives considered

| Option | Rejected because |
|--------|------------------|
| Keep ADR-0001 Tauri + web UI | Two UIs; no iOS; vault/keystore/lockscreen/widgets are WebView-weak |
| SwiftUI iOS + Compose Android + Tauri desktop | Three UIs; violates “one shared UI” |
| Flutter | Throws away working Compose screens and Kotlin domain |
| KMP domain + separate SwiftUI/desktop UI | Logic shared, look not shared |

## Consequences

- Move `examples/android` UI/domain into `shared` + thin platform apps (Phase 3+ after Edward reviews the briefs).
- TypeScript models become a freeze/test oracle for the LAN envelope, not the SoT.
- `examples/desktop` Tauri and `examples/web` product features stop growing.
- iOS widgets, Now Playing, Keychain, and Android UnifiedPush stay `expect`/`actual` (not forced into commonMain if fragile).

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Shared parsers already reject empty feeds (`NewsParseError`); keep typed errors in KMP |
| Network timeout | Keep 12s feed timeouts from `NewsFeedRefresh`; Ktor client inherits them |
| Race conditions | LAN LWW (ADR-0011) stays the merge rule; no new cloud writer in v1 |
| Unhandled exceptions | Platform composition roots catch UI failures; vault open failures stay local-only |
