# iOS sideload distribution (pointer)

Unsigned **`.ipa`** artifacts for **AltStore / SideStore** are documented under:

- [`examples/ios/PACKAGING.md`](../examples/ios/PACKAGING.md) — build + release asset names
- [`examples/ios/README.md`](../examples/ios/README.md) — simulator vs device matrix

**Decision 19:** no Apple Developer account / no physical Apple device for Edward — TestFlight, App Store, and **APNs are N/A** until `[HUMAN]` enrolls.

Do not add new CI workflows in early sprints; attach iOS assets to the existing `v*` / `workflow_dispatch` release path — see [`RELEASE_ARTIFACTS.md`](RELEASE_ARTIFACTS.md).