# Feature: sync

> Status: 🔲 open · Public API locked 2026-08-18

## Acceptance criteria

- 🔲 Optional; local-only / airplane mode is first-class
- 🔲 Passphrase never uploaded
- 🔲 Argon2id + XChaCha20-Poly1305 (or AES-256-GCM) envelopes
- ✅ Providers: Google Drive `drive.appdata` for feed sources (WebDAV still stub)
- ✅ Opaque `.enc` blobs only for the feed-source document
- 🔲 LWW-with-clocks first; CRDT upgrade if dual-offline drops data
- 🔲 Sync on launch, debounced change, background, and Sync now

## Smoke scenario

1. _Given_ two devices sharing a passphrase
2. _When_ one stars an article offline and later syncs
3. _Then_ the other device sees the star and the host never sees plaintext
