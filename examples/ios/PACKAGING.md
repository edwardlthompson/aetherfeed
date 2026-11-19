# iOS packaging — unsigned `.ipa` (decision 19)

No paid Apple Developer account in v1. Ship an **unsigned** `.ipa` on **GitHub Releases** for sideload via **AltStore** or **SideStore**. TestFlight / App Store are deferred `[HUMAN]`.

## Preconditions (macOS only)

- Xcode installed; simulator build succeeds (see [`compose/README.md`](compose/README.md)).
- App Group `group.org.aetherfeed.app` enabled in the host app + widget extension entitlements.
- Bundle ID: `org.aetherfeed.app` (match `TECH_DECISION.md`).

## Build outline

1. Archive the iOS app target (Release, **Any iOS Simulator** for CI artifacts; **Any iOS Device (arm64)** for sideload payloads when signing is absent, use ad-hoc / export for development per AltStore docs).
2. Export **development** or **ad hoc** `.ipa` **without** App Store Connect upload.
3. Attach to the existing `v*` / `workflow_dispatch` release job per [`docs/RELEASE_ARTIFACTS.md`](../../docs/RELEASE_ARTIFACTS.md) (do **not** add new push/PR/schedule workflows).

## Release asset naming

| Asset | Purpose |
|-------|---------|
| `AetherFeed-0.1.0-ios-simulator.zip` | CI / Mac testers (Simulator `.app`) |
| `AetherFeed-0.1.0-unsigned.ipa` | AltStore / SideStore sideload |

Marketing version in UI stays **0.1.0**.

## Sideload notes

- Users install AltStore or SideStore on a physical iPhone/iPad (not available to Edward locally).
- Refresh apps before the 7-day free provisioning window expires (SideStore pairing recommended).
- **APNs and TestFlight are N/A** until `[HUMAN]` enrolls in the Apple Developer Program.

## Verification matrix

| Check | Simulator (Mac) | Physical device `[HUMAN]` |
|-------|-----------------|---------------------------|
| Launch CMP shell | Yes | Pending |
| Widget unread count | Partial (file written) | Pending |
| Now Playing / lockscreen | No (simulator limited) | Pending |
| Local notification permission | Partial | Pending |
| BGAppRefresh | Partial (scheduler registered) | Pending |

See also [`docs/IOS_SIDELoad.md`](../../docs/IOS_SIDELoad.md).