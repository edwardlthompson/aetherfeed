# AetherFeed desktop (Windows / macOS / Linux)

## Shipping shell: Compose Desktop

**Product path:** [`compose/`](compose/) — JVM Compose Desktop (1100×760, tray unread tooltip).

```bash
cd examples/desktop/compose
./gradlew :composeApp:run

```

Shared JVM modules: `shared/domain`, `shared/vault`. Packaging notes: `compose/PACKAGING.md` (jpackage row 2). Full release matrix: [`docs/RELEASE_ARTIFACTS.md`](../../docs/RELEASE_ARTIFACTS.md).

## Frozen reference: Tauri + web UI

The Tauri 2.0 wrapper under `src-tauri/` and the Vite frontend in `examples/web` are **frozen** — no new product features. Keep them for LAN hub (`lan_hub.rs`), tray contract, and migration parity notes until Compose Desktop reaches parity (Sprint 59 gate).

Historical dev commands (reference only):

```bash
npm install
npm run tauri dev
npm run tauri build

```

Tray tooltip pattern: `AetherFeed — {n} unread` (same as Android widget contract).

## Auto-update

GitHub Releases for the Compose build ships in a follow-up row. Tauri `tauri-plugin-updater` remains reference only.
