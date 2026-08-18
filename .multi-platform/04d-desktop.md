# Windows desktop platform notes

**Executable / app id:** `AetherFeed`  
**Shell:** Tauri 2.0  
**UI:** shared web layer in `examples/web`  
**Identifier:** `org.aetherfeed.app`

## OS features

| Feature | Notes |
|---------|--------|
| Tray icon | Tooltip shows total unread |
| Desktop notifications | Same channel split as Android |
| Background refresh | Tauri command + OS scheduler later |
| Storage | App-data directory only |
| Updates | `tauri-plugin-updater` + GitHub Releases |

## Decision

Tauri over Electron for a smaller FOSS binary. Electron is allowed only if a
required feature is blocked; record that in `DECISION_LOG.md` first.
