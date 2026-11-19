# Desktop packaging — Compose Desktop (shipping)

Tauri under `examples/desktop/src-tauri/` is **frozen**. Installers come from this Gradle module only.

## Local package (current OS)

```bash
cd examples/desktop/compose
./gradlew :composeApp:packageDistributionForCurrentOS
```

Artifacts: `composeApp/build/compose/binaries/main/`.

Cross-OS formats are declared in `composeApp/build.gradle.kts` (`Msi`, `Exe`, `Dmg`, `Deb`, `AppImage`); jpackage only emits formats for the host OS.

## Matrix

| OS | Formats | Notes |
|----|---------|-------|
| Windows | MSI, exe | WiX for MSI; code signing `[HUMAN]` |
| macOS | dmg / app | notarization `[HUMAN]` when signing identity exists |
| Linux | deb, AppImage | AppImage needs `appimagetool` on PATH for some JDK setups |

Marketing version in About/UI stays **0.1.0**. `packageVersion` is `1.0.0` because jpackage rejects `0.x` majors.

## Icons / branding (follow-up)

Point `nativeDistributions` icon at multi-res assets under `branding/assets/` (`.ico` / `.icns` / `.png`). Do not invent new CI push/PR/schedule jobs for packaging — use existing `v*` / `workflow_dispatch` release path ([`docs/RELEASE_ARTIFACTS.md`](../../../docs/RELEASE_ARTIFACTS.md)).

## Freeze Tauri product path

Do not ship Tauri NSIS/MSI alongside Compose jpackage. Winget stubs that still name Tauri bundles should be updated when the first Compose GitHub Release asset exists.