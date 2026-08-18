# Module E: Windows desktop (Tauri 2)

> Active AetherFeed delivery surface. See `.multi-platform/04d-desktop.md`.

## Requirements

- Tauri 2.0 binary named `AetherFeed`
- App data stays in the OS app-data directory
- Tray tooltip / badge shows the same total unread count as Android
- Auto-update from GitHub Releases only
- No Electron unless `DECISION_LOG.md` records a blocked Tauri feature

## Activation Checklist

- 🔲 Keep `examples/desktop/`
- 🔲 `npm run tauri build` produces NSIS/MSI
- 🔲 `[HUMAN]` fill updater pubkey before first release
- 🔲 Winget manifest stub under `packaging/winget/`

## Owner Labels

| Task type | Label |
|-----------|-------|
| Tauri commands, tray, updater wiring | `AGENT` |
| Code signing / updater pubkey | `HUMAN` |
| `cargo test` / clippy in CI | `AUTO` |
