# AetherFeed — iOS (Compose Multiplatform)

Sprint 60 skeleton for the shared Compose iOS app. **Edward has no Apple device (decision 19)** — local work is **simulator-only on macOS**; on-device sideload checks are `[HUMAN]`.

## Layout

| Path | Role |
|------|------|
| [`compose/`](compose/) | KMP module — JVM stub compiles on Windows; iOS targets link on macOS only |
| [`native/`](native/) | WidgetKit / AVFoundation / UserNotifications contracts (Swift placeholders) |
| [`PACKAGING.md`](PACKAGING.md) | Unsigned `.ipa` for AltStore/SideStore (GitHub Releases) |
| [`docs/RELEASE_ARTIFACTS.md`](../../docs/RELEASE_ARTIFACTS.md) | Cross-platform release matrix + dry-run |
| [`XCODEGEN.md`](XCODEGEN.md) | Optional XcodeGen host when a Mac is available |

Product UI rules: [`docs/migration/DESIGN_BRIEF.md`](../../docs/migration/DESIGN_BRIEF.md) — **do not** replace the bottom bar with a top `TabView`.

## Windows / Linux (this machine)

```powershell
cd examples\ios\compose
.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:jvmTest
.\gradlew.bat :shared-domain:test
```

Optional JVM window preview (Compose Desktop host):

```powershell
.\gradlew.bat :composeApp:run
```

## macOS + Xcode (simulator)

1. Install Xcode and CocoaPods (if a future pod is added).
2. Enable iOS targets: `./gradlew -Paetherfeed.enableIos=true :composeApp:embedAndSignAppleFrameworkForXcode` *or* link simulator framework:

   ```bash
   ./gradlew -Paetherfeed.enableIos=true :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

3. Open the generated Xcode workspace/project (see [`XCODEGEN.md`](XCODEGEN.md)) and run **iOS Simulator** — no physical device required for CI.

**Not available on Windows:** Xcode, Simulator, Keychain Services integration tests, WidgetKit on a home screen, BGAppRefresh scheduling proof, AVAudioSession on device.

## Vault / refresh / Add-feed

- **Keychain:** `expect/actual` in `compose/composeApp` — JVM + ios `InMemory` until Darwin `SecItem*` lands on a Mac.
- **Add-feed:** `IosAddFeedDialog` — same URL/title/kind chips as Android (decision 13).
- **Refresh:** `BackgroundRefresh.kt` — foreground refresh always; `BGAppRefreshTask` best-effort (decision 18); no APNs (decision 19).

## Shared modules

Gradle includes `shared/domain` and `shared/vault` (same pattern as `examples/desktop/compose/`). `shared/ui` stays Android-library until CMP cutover.