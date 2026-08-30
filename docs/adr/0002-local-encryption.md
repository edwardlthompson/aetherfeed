# ADR-0002: Local encryption

- **Status:** Accepted
- **Date:** 2026-08-18
- **Deciders:** AetherFeed seed; HUMAN approved 2026-08-18 (requested automation)

## Context

Feeds, API keys, and user state must stay private on disk. The product is
usable without any cloud account.

## Decision

- Encrypt the on-device database with SQLCipher
- Keep credentials, booru keys, OAuth tokens, and sync key material in the
  platform keystore / OS credential store when available
- Store downloaded articles, images, and podcast files in app-private storage
- Never write world-readable download dumps by default

## Consequences

- Android seed exposes `SqlCipherVault` as the private-file contract
- Desktop uses the Tauri app-data directory
- Forgot-passphrase recovery is impossible for E2E backups; UI must say so
- **2026-08-19 addendum:** A user PIN (6+) or passphrase (8+) wraps the SQLCipher / AES-GCM vault key. Cold start and a 2-minute background timeout require unlock. Article HTML, images, desktop `imported-feeds`, and episode files are ciphertext at rest. Forgot secret wipes the local vault. The secret is never stored in Settings plaintext.

## Alternatives Considered

| Option | Rejected because |
|--------|------------------|
| Plain SQLite | On-device plaintext violates the product brief |
| Encrypt files only | Queryable state would still leak |
