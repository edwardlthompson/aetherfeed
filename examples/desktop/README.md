# AetherFeed desktop (Windows)

Tauri 2.0 wrapper around the shared web UI. The desktop binary stays small
and sandboxed. Downloads and the encrypted vault live in the OS app-data
directory, never a world-readable dump.

## Why Tauri

Electron would ship a full Chromium. Tauri uses the OS webview so the FOSS
binary stays closer to the "quiet, lightweight" product brief. If a required
feature is blocked, log the fallback in `DECISION_LOG.md` before adopting
Electron.

## Commands

```bash
npm install
npm run tauri dev
npm run tauri build
```

The frontend is `examples/web`. Tray tooltip and badge should show the same
total unread count as the Android widget.

## Auto-update

GitHub Releases via `tauri-plugin-updater`. `[HUMAN]` fills the updater
public key before the first Windows release.
