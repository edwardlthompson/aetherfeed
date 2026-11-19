# AetherFeed — Compose Desktop (shipping shell)

JVM Compose Desktop app that replaces Tauri as the product path. Tauri under `examples/desktop/src-tauri/` is **frozen reference** only.

## Run

```bash
cd examples/desktop/compose
./gradlew :composeApp:run
```

## Compile (CI / gates)

```bash
./gradlew :composeApp:compileKotlin
./gradlew :composeApp:test
```

## Modules

| Gradle project | Path |
|----------------|------|
| `:composeApp` | This shell (UI + tray + stubs) |
| `:shared-domain` | `shared/domain` JVM models |
| `:shared-vault` | `shared/vault` keychain interface |

`shared/ui` stays Android-library-only until a safe CMP cutover — desktop UI is local under `composeApp`.

## Packaging

See [PACKAGING.md](PACKAGING.md) (jpackage stub for Sprint 59 row 2).