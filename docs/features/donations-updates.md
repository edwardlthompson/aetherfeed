# Feature: donations-updates

> Quiet Venmo donate and a non-blocking daily installer check, reused from Continuum Calendar.

## Acceptance criteria

- ✅ About (and Settings) always offer **Donate via Venmo**. The update/install dialog never includes donate.
- ✅ First run records the installed version and does not show a donate popup.
- ✅ After a later version change, one optional note appears: title **Development is still going**; buttons **Donate via Venmo** | **Not now**. Either button records “seen this version.”
- ✅ Once per 24 hours the app fetches GitHub `releases/latest` with a `User-Agent` and a 10s timeout, compares **installer filenames** (not git/template tags), and prompts **Install** | **Later** only for a newer undismissed asset.
- ✅ Failed fetch, timeout, empty assets, or same version stay silent. The app never blocks.
- ✅ Donate prefs and last-check timestamps stay device-local (not peer-synced).
- ✅ i18n: `about.donate.*` / `about.update.*` (web) and `about_*` (Android).

## Smoke scenario

1. Given a fresh install at version `0.1.0`
2. When the user unlocks the app
3. Then no donate dialog appears, and a GitHub miss stays silent
4. When the installed version later becomes `0.2.0`
5. Then one donate note appears; **Not now** prevents it until the next version change

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/about/productUpdate.ts` | `examples/android/.../about/ProductUpdate.kt` |
| View | `AboutPanel.ts`, `AppUpdateDialogs.ts`, Settings donate row | `ui/about/`, `LaunchPromptDialogs.kt` |
| Tests | `productUpdate.test.ts`, `runAppUpdates.test.ts` | `ProductUpdateTest.kt` |
| Wiring | `appBootstrap.ts` | `AetherFeedApp.kt` + `AppLaunchGate.kt` |

## Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Empty tag, empty assets, or blank version → no prompt (`shouldPromptUpdate` / `parseAssetVersion`) |
| Network timeout | 10s abort; catch → `null`; stay silent |
| Race conditions | One launch decision; donate note wins and skips the update prompt that launch |
| Unhandled exceptions | Fetch/parse wrapped; failures return `null` |

## Notes

- Desktop asset: `AetherFeed-X.Y.Z-x64-setup.exe`. Android asset: `aetherfeed-X.Y.Z-foss.apk`.
- Repo: `edwardlthompson/aetherfeed`. Venmo URL is public, not a secret.
- Settings “Check for updates” remains an opt-out of the daily GitHub fetch. Donate is always available.
