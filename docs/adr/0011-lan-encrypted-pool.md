# ADR-0011: Home-LAN encrypted subscription pool hub

- **Status:** Accepted
- **Date:** 2026-11-12
- **Deciders:** Agent implementation of locked plan 1B+2B (Hermes + AetherFeed Android + desktop; subs/reads/stars/playback)

## Context

Same-device Hermes ContentProvider sync works on one phone. Couch→desktop reading needs a shared pool across devices on home Wi-Fi without uploading plaintext to a cloud host.

## Decision

1. AetherFeed desktop hosts a LAN-only HTTP hub (`GET/PUT /v1/pool`, `GET /v1/meta`) on port 8787 by default.
2. Payloads are `SyncEnvelope` ciphertext (Argon2id + XChaCha20-Poly1305). PUT requires HMAC bearer derived from the shared passphrase and hub auth salt.
3. Hub refuses non-private peer IPs. Discovery is manual URL/QR from Settings (mDNS deferred).
4. Pool document v2 carries subscriptions, reads, stars, and playback positions with LWW merge.
5. Same-device ContentProvider remains the offline/local fast path; LAN sync refreshes the provider blob when successful.
6. This is a **local SyncProvider**, not a new cloud account system (see ADR-0003).

## Consequences

- Desktop Tauri gains `lan_hub_*` / `seal_pool` / `open_pool` commands.
- Android clients (AetherFeed + Hermes) store hub URL + passphrase and sync on resume / Sync now.
- Firewall must allow inbound TCP 8787 on the desktop LAN interface.

## Alternatives Considered

- Mutual peer mesh without a hub (rejected for v1 complexity)
- TLS with self-signed certs (deferred; ciphertext payload is the primary confidentiality control)
- Folding into Drive/WebDAV only (rejected: home Wi-Fi latency and local-first UX)
