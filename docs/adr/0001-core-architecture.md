# ADR-0001: Core Application Architecture

- **Status:** Superseded by [ADR-0013](0013-compose-multiplatform.md)
- **Date:** 2026-08-18
- **Deciders:** AetherFeed seed; HUMAN approved 2026-08-18 (requested automation)

> Kept as history. Do not delete. The 🔲 pattern-pick row below stays for the template architecture-pick gate. Product runtime is ADR-0013 (Compose Multiplatform / KMP).

## Context

AetherFeed ships on Android (Kotlin/Compose) and Windows (Tauri 2 + web UI).
Unread, stars, tags, and playback position must stay consistent across those
surfaces and across optional E2E sync.

## Decision

**Selected pattern:** 🔲 MVVM · 🔲 Clean Architecture · 🔲 Hexagonal

**Adopted for AetherFeed:** Clean Architecture with MVI on Android.

- **Entities:** `Feed`, `Article`, `PodcastShow`, `Episode`, `BooruPost`, `Tag`, `Star`, `Like`, `ReadState`, `PlaybackPosition`, `NotificationChannelPref`, `SyncEnvelope`
- **Use cases / repositories:** shared unread/star/tag/sync primitives
- **Adapters:** RSS, podcast enclosure, booru source, SQLCipher vault, `SyncProvider`
- **Frameworks:** Compose, Tauri, Room/SQLCipher, Ktor/Media3

TypeScript models in `shared/typescript/` are the cross-language contract.
Android mirrors them in Kotlin. Desktop consumes the TypeScript module.

## Consequences

- Schema changes are Sequential-only
- Local-first: every feature must work with the `local-only` provider
- Hilt and Room production wiring follow the locked types

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| MVVM only | Cross-platform sync needs a domain core, not screen state |
| Hexagonal-only naming | Clean + MVI matches the Android stack already chosen |
| Kotlin Multiplatform shared runtime | Conflicts with the Tauri desktop choice |
