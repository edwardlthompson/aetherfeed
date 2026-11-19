# XcodeGen / Xcode host (placeholder)

Kotlin Multiplatform emits `AetherFeedCompose.framework` for Xcode embedding. Until a Mac wires the host:

1. Run `./gradlew -Paetherfeed.enableIos=true :composeApp:linkDebugFrameworkIosSimulatorArm64`.
2. Add a minimal **Xcode** iOS app target (`org.aetherfeed.app`) that loads CMP via `UIViewController` (see `TECH_DECISION.md` §5).
3. Optional: maintain `project.yml` here with:
   - App target + WidgetKit extension target
   - App Group `group.org.aetherfeed.app`
   - `BGTaskSchedulerPermittedIdentifiers` for refresh task id `org.aetherfeed.app.refresh`
   - **No** Push Notifications capability (APNs deferred, decision 19)

**Edward has no Apple hardware** — treat this file as the wiring checklist for a Mac CI runner or volunteer tester, not a required local build on Windows.

SwiftPM-only UI is **not** planned; product screens stay in shared Compose (`DESIGN_BRIEF.md` §1).