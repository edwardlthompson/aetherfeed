# Release artifact matrix (AetherFeed)

Canonical list of **installable binaries** for GitHub Releases. **Do not commit build outputs** — artifacts are uploaded to Releases only.

Related: [`RELEASE_TOKENS.md`](RELEASE_TOKENS.md) · [`RUNBOOK.md`](RUNBOOK.md) · [`PACKAGE_ATTESTATION.md`](PACKAGE_ATTESTATION.md) · parity [`migration/PARITY_CHECKLIST.md`](migration/PARITY_CHECKLIST.md) §11.

## Versioning (read this first)

| Layer | Value | Where |
|-------|-------|--------|
| **Product / About (migration)** | `0.1.0` | `schemas/golden-path/app-version.json`, Android `versionName`, in-app About |
| **Template / Release Please tag** | e.g. `1.13.4` | `.template-version`, tags `v*` |
| **jpackage desktop `packageVersion`** | `1.0.0` | `examples/desktop/compose/composeApp/build.gradle.kts` (jpackage rejects `0.x` majors) |

Shipped **user-facing** version stays **`0.1.0`** until migration decision 14 closes. Release tags follow **template** semver for changelog automation, not the About string.

## CI policy (no new triggers)

Release packaging automation uses **only**:

| Trigger | Workflow | Jobs |
|---------|----------|------|
| Push tag `v*` | [`.github/workflows/release.yml`](../.github/workflows/release.yml) | `tag-gate` → (Release Please publishes) → `sbom-assets` on `release: published` |
| `workflow_dispatch` | same | `sbom-assets` (+ pre-release gate when `tag` input empty) |
| `release: published` | same | `sbom-assets` uploads SBOM + Winget stub |

**Forbidden for new packaging jobs:** `push` to branches, `pull_request`, `schedule`. Other workflows may keep their existing `workflow_dispatch` (CodeQL, Pages, etc.) — do not add packaging there.

## Platform matrix (`0.1.0` product)

| Platform | Extensions | Build command (local) | Release asset names (target) | CI / Release today |
|----------|------------|----------------------|-------------------------------|---------------------|
| **Android** | `.apk`, `.aab` | `cd examples/android && SOURCE_DATE_EPOCH=1700000000 ./gradlew assembleRelease bundleRelease` | `app-release.apk`, `app-release.aab` (or signed equivalents) | **Shipped** on `v1.13.4` (manual upload). CI `android-release` builds APK for reproducibility only — see [`ANDROID_SIGNING.md`](ANDROID_SIGNING.md). `release.yml` uploads **SBOM**, not APK/AAB yet. |
| **Windows** | `.msi`, `.exe` | `cd examples/desktop/compose && ./gradlew :composeApp:packageDistributionForCurrentOS` | `AetherFeed-1.0.0.msi`, `AetherFeed-1.0.0.exe` (jpackage naming under `composeApp/build/compose/binaries/main/`) | **Local** on Windows host; **Pending** GitHub Release attach. |
| **macOS** | `.dmg`, `.app` | same Gradle task on macOS | `AetherFeed-1.0.0.dmg`, `AetherFeed.app` | **Pending** — needs Mac builder + `[HUMAN]` signing/notarization optional. |
| **Linux** | `.deb`, `.AppImage` | same Gradle task on Linux | `aetherfeed_1.0.0_amd64.deb`, `AetherFeed-1.0.0-x86_64.AppImage` (exact names vary by arch) | **Pending** GitHub Release attach. |
| **iOS (sideload)** | `.ipa`, `.zip` (simulator) | macOS + Xcode — see [`examples/ios/PACKAGING.md`](../examples/ios/PACKAGING.md) | `AetherFeed-0.1.0-unsigned.ipa`, `AetherFeed-0.1.0-ios-simulator.zip` | **Partial** — docs + naming; **`[HUMAN]`** archive/export on Mac. |
| **Attestation** | `.json`, `.yaml` | `release.yml` / Anchore SBOM | `sbom*.cyclonedx.json`, `packaging/winget/manifest.stub.yaml` | **Automated** on `workflow_dispatch` / `release: published`. |

Update checker in-app uses **installed format** (apk, msi, exe, deb, appimage, ipa) — never switch formats on update. See [`docs/features/donations-updates.md`](features/donations-updates.md).

## Pointers by example tree

| Path | Doc |
|------|-----|
| `examples/android/` | [`ANDROID_SIGNING.md`](ANDROID_SIGNING.md), reproducible APK: `scripts/verify-reproducible-apk.sh` |
| `examples/desktop/compose/` | [`PACKAGING.md`](../examples/desktop/compose/PACKAGING.md) |
| `examples/ios/` | [`PACKAGING.md`](../examples/ios/PACKAGING.md), [`IOS_SIDELoad.md`](IOS_SIDELoad.md) |

## Maintainer dry-run (local)

**What “dry-run” means:** `workflow_dispatch` with an **empty** `tag` input runs `scripts/pre-release-gate.sh` and `scripts/check-release-artifact-matrix.sh --dry-run` inside [`.github/workflows/release.yml`](../.github/workflows/release.yml). It **does not** build or upload platform installers; it validates gates + documents expected extensions. A full `sbom-assets` upload still requires a published GitHub Release for the effective tag.

```bash
# Local equivalent (no GitHub API required)
bash scripts/check-release-artifact-matrix.sh --dry-run
bash scripts/pre-release-gate.sh   # heavier: feature-gate, security triage, licenses
```

**Remote dry-run (maintainer):**

1. GitHub → **Actions** → **Release** → **Run workflow**
2. Leave **tag** blank → pre-release gate + artifact matrix check
3. Optional: set **tag** to an existing release (e.g. `v1.13.4`) to SBOM-backfill only — still **no** APK/desktop/ipa from this workflow today

```bash
gh workflow run release.yml
# or with explicit ref (see RELEASE_TOKENS.md):
gh workflow run release.yml --ref main -f tag=
```

## Deferred `[HUMAN]`

| Item | Notes |
|------|--------|
| F-Droid application id rebrand (`dev.foss.goldenpath` → product id) | Decision 15 — see `HUMAN_BACKLOG.md` |
| Apple Developer Program, TestFlight, App Store | Decision 19 — unsigned ipa only for now |
| Paid APNs | N/A until enrolled developer account |
| Windows/macOS code signing | Optional when certs exist |

## Evidence (Sprint 62 AUTO)

- Workflow: `.github/workflows/release.yml` (`on.push.tags: v*`, `workflow_dispatch`, `release: published`)
- Dry-run steps: `Pre-release gate (workflow_dispatch dry-run)` + `Release artifact matrix (dry-run)`
- Extension list enforced by: `scripts/check-release-artifact-matrix.sh`