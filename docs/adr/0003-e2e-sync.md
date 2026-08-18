# ADR-0003: Zero-knowledge E2E sync

- **Status:** Accepted
- **Date:** 2026-08-18
- **Deciders:** AetherFeed seed; HUMAN approved 2026-08-18 (requested automation)

## Context

Users want Reader-style state on more than one device without giving a host
plaintext feeds, tags, or stars.

## Decision

`SyncProvider` implementations:

1. Google Drive with `https://www.googleapis.com/auth/drive.appdata` only
2. WebDAV as a self-host fallback
3. `local-only` as a first-class setting

Protocol:

1. User creates a master passphrase that is never uploaded
2. Derive a key with Argon2id
3. Version, compress, and encrypt the document with XChaCha20-Poly1305
   (AES-256-GCM is an allowed alternate AEAD)
4. Upload only opaque `.enc` blobs
5. Conflicts: LWW with per-device clocks first; CRDT/delta merge later if
   dual-offline use drops unread/stars/positions
6. Sync on launch, on debounced change, in background, and via Sync now

No extra cloud providers or account systems without a HUMAN decision.

## Consequences

- Seed Rust crate `aetherfeed-crypto` owns KDF + AEAD helpers
- Drive must never write the user's visible My Drive root
- Airplane / local-only mode is a product setting, not an error

## Alternatives Considered

| Option | Rejected because |
|--------|------------------|
| Full Drive scope | Over-broad and unnecessary |
| Plain JSON on WebDAV | Host would see plaintext state |
| Required account | Breaks local-first |
