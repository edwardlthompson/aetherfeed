# Feature: sync

> Status: 🔶 partial — LAN encrypted pool hub + clients landed; Drive/WebDAV envelope orchestration still open

## Acceptance criteria

- ✅ Optional; local-only / airplane mode is first-class (`local-only` provider rejects uploads)
- ✅ Passphrase never uploaded (LAN hub + SyncEnvelope seal/open)
- ✅ Argon2id + XChaCha20-Poly1305 envelopes — Rust `aetherfeed-crypto` + Android `LanPoolCrypto` + desktop Tauri commands
- ✅ Providers: Google Drive `drive.appdata`, WebDAV (`DriveAppDataProvider`, `WebDavProvider`, desktop/web wrappers)
- ✅ Opaque `.enc` blob name contract (`aetherfeed-sync.enc`, `isOpaqueEncName`)
- ✅ Home-LAN hub (desktop host) for Hermes + AetherFeed Android + desktop — pool v2: subs, reads, stars, playback
- 🔲 Cloud LWW-with-clocks document orchestration; CRDT upgrade if dual-offline drops data
- ✅ LAN sync on launch/resume, Sync now; ContentProvider same-device path retained

## Locked public API

| Symbol | Role |
|--------|------|
| `SyncEnvelope` | `version=1`, `kdf=argon2id`, `aead`, `saltB64`, `nonceB64`, `ciphertextB64` |
| `SyncProvider` | `id`, `pull(): ByteArray?`, `push(blob)` — opaque bytes only |
| `SyncProviderId` | `drive-appdata`, `webdav`, `local-only` |
| `isOpaqueEncName(name)` | Require `.enc` suffix for remote objects |
| `mergeLww(local, remote)` | Shared TS / Kotlin clock merge for documents |
| Pool document v2 | `subs`, `reads`, `stars`, `playback` — LWW by normalized URL |
| LAN hub | `GET /v1/meta`, `GET/PUT /v1/pool` — private peers only; HMAC PUT bearer |
## Implementation map

| Area | Status | Location |
|------|--------|----------|
| Android Drive App Data | ✅ injectable HTTP + `FakeDriveSyncProvider` | `examples/android/.../sync/` |
| Android WebDAV | ✅ injectable HTTP | `examples/android/.../sync/WebDavProvider.kt` |
| Desktop pull/push | ✅ Tauri `sync_pull` / `sync_push` (memory backend) | `examples/desktop/src-tauri/src/sync.rs` |
| Desktop LAN hub | ✅ Tauri `lan_hub_*` + Settings panel | `examples/desktop/.../lan_hub.rs`, `examples/web/src/sync/lanHub.ts` |
| Android LAN client | ✅ prefs + HTTP + merge apply | `examples/android/.../sync/LanPool*.kt` |
| Hermes LAN client | ✅ mirrors AetherFeed crypto/HTTP | Hermes `feeds/pool/LanPool*.kt` |
| Web WebDAV wrapper | ✅ Vitest | `examples/web/src/sync/` |
| Crypto helpers | ✅ | `examples/rust/`, Android Tink + Argon2 |
Reference: ADR-0011, `shared/typescript/sync.ts`, Android `domain.SyncProvider` + `sync/`, Rust `aetherfeed-crypto`.

## Smoke scenario

1. Enable hub on desktop Settings (shared passphrase); note LAN URL.
2. Enter same hub URL + passphrase on Hermes Feeds settings and AetherFeed Settings.
3. Add/star/read/seek podcast on phone → Sync / resume → appear on desktop.
4. Continue episode on desktop → position updates on phone.
5. Booru sub present in AetherFeed, absent from Hermes UI.
6. With hub off, same-device ContentProvider path still works.

## Container map

- Feature container: `examples/*/sync/` (stack-specific); Hermes `feeds/pool/`
- Spec: `docs/features/sync.md`
- ADR: `docs/adr/0011-lan-encrypted-pool.md`

## Tests

Automated: yes

- Android: `./gradlew :app:test --tests "org.aetherfeed.app.sync.*"`
- Hermes: `./gradlew :app:test --tests "org.hermeslauncher.app.feeds.pool.*"`
- Web: `cd examples/web && npx vitest run --coverage.enabled=false src/sync/lanHub.test.ts`
- Desktop: `cd examples/desktop/src-tauri && cargo test`
- Crypto: `cd examples/rust && cargo test`

## Fallback validation

```bash
python3 scripts/agent-run.py feature-gate --stack multi

```
