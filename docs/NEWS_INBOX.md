# News Inbox

Auto-collected inbox of news, upstream updates, security fixes, and ideas relevant to this repo, appended daily by News Bot.
A Cursor planning agent should review these items and fold the useful ones into BUILD_PLAN; entries are append-only, newest at the bottom.


## 2026-11-13

**News and updates**

- Media3 1.11.1 stable and 1.12.0-beta01 (lower buffer memory, JSpecify nullness, Compose test teardown crash fix). [source](https://developer.android.com/jetpack/androidx/releases/media3)
- Rust 1.100.0: Cargo gains `cargo::unused_dependencies` lint and dependency cooldowns (minimum publish age); Windows `CommandExt::show_window` stabilized. [source](https://blog.rust-lang.org/2026/11/12/Rust-1.100.0/)
- Local-First News roundup: Jazz 2.0 alpha on SQLite, Subduction encrypted P2P CRDT sync, new sync engines. [source](https://www.localfirstnews.com/2026-11-12/)

**Tasks for the planning agent**

- [ ] Upgrade Media3 from 1.6.0 toward 1.11.1 — The Android app is many releases behind on playback fixes. Label: `[AGENT][LOCAL]` · Priority: P2 · https://developer.android.com/jetpack/androidx/releases/media3
- [ ] Enable Cargo dependency cooldown for the desktop/crypto crates — Reduces risk from freshly published malicious crates. Label: `[AGENT][LOCAL]` · Priority: P3 · https://blog.rust-lang.org/2026/11/12/Rust-1.100.0/
- [ ] Review new sync engines for offline star/playback sync — Ideas for the LAN encrypted pool. Label: `[AGENT][LOCAL]` · Priority: P3 · https://www.localfirstnews.com/2026-11-12/
