# ADR-0012: Keep LAN encrypted pool; do not adopt Jazz/Subduction now

- **Status:** Accepted
- **Date:** 2026-11-13
- **Deciders:** Agent completing NEWS_INBOX 2026-11-13 sync-engine review

## Context

Local-First News (2026-11-12) highlighted Jazz 2.0 alpha (SQLite-backed) and Subduction (encrypted P2P CRDT sync) as new sync engines. AetherFeed already ships home-LAN ciphertext pool sync for subscriptions, reads, stars, and playback (ADR-0011).

## Decision

1. Keep ADR-0011: desktop LAN hub + SyncEnvelope (Argon2id + XChaCha20-Poly1305) + pool v2 LWW.
2. Do **not** replace or add Jazz 2.0 or Subduction for offline star/playback sync in this milestone.
3. Revisit only if LAN pool LWW proves insufficient for a concrete multi-writer conflict that CRDTs uniquely solve.

## Alternatives considered

- Adopt Jazz 2.0 SQLite sync — rejected: alpha surface, new runtime/stack, overlaps vault already on-device.
- Adopt Subduction P2P CRDT — rejected: mesh complexity vs hub model; ciphertext pool already meets privacy bar.
- Hybrid (LAN hub + CRDT merge) — deferred: no product gap for stars/playback LWW today.

## Consequences

- NEWS_INBOX “review sync engines” task closes with this ADR.
- Future sync work stays within SyncEnvelope / pool v2 unless a new ADR supersedes this.

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | N/A — no new APIs or wire formats |
| Network timeout | N/A — documentation-only decision |
| Race conditions | Existing pool LWW remains the merge rule (ADR-0011) |
| Unhandled exceptions | N/A — no code path change |
