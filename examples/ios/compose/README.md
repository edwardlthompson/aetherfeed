# AetherFeed iOS — Gradle / KMP

## Modules

| Gradle project | Path |
|----------------|------|
| `:composeApp` | CMP shell stubs + platform vault |
| `:shared-domain` | `shared/domain` (widget JSON contract) |
| `:shared-vault` | `shared/vault` |

## Compile (Windows-friendly)

```powershell
.\gradlew.bat :composeApp:compileKotlinJvm :composeApp:jvmTest
```

## iOS frameworks (macOS only)

```bash
./gradlew -Paetherfeed.enableIos=true :composeApp:linkDebugFrameworkIosSimulatorArm64
```

Property `aetherfeed.enableIos` defaults to `false` in `gradle.properties` so Windows agents do not configure the iOS SDK.