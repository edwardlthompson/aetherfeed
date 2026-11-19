#!/usr/bin/env bash
# Validate docs/RELEASE_ARTIFACTS.md and release.yml triggers; optional dry-run extension list.
# Usage: scripts/check-release-artifact-matrix.sh [--dry-run]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

DRY_RUN=0
if [ "${1:-}" = "--dry-run" ]; then
  DRY_RUN=1
fi

DOC="docs/RELEASE_ARTIFACTS.md"
WF=".github/workflows/release.yml"
ERRORS=0

fail() {
  echo "FAIL: $*"
  ERRORS=$((ERRORS + 1))
}

echo "=== Release artifact matrix check ==="

if [ ! -f "$DOC" ]; then
  fail "missing $DOC"
else
  echo "OK   $DOC present"
  for ext in apk aab msi exe dmg ipa AppImage deb; do
    if ! grep -qi "$ext" "$DOC"; then
      fail "$DOC must document $ext artifacts"
    fi
  done
fi

if [ ! -f "$WF" ]; then
  fail "missing $WF"
else
  grep -q 'tags:' "$WF" && grep -q '"v\*"' "$WF" && echo "OK   release.yml has v* tag trigger" || fail "release.yml missing v* tag trigger"
  grep -q 'workflow_dispatch:' "$WF" && echo "OK   release.yml has workflow_dispatch" || fail "release.yml missing workflow_dispatch"
  if grep -qE '^\s+schedule:' "$WF"; then
    fail "release.yml must not define schedule (packaging policy)"
  else
    echo "OK   release.yml has no schedule trigger"
  fi
  if grep -q 'check-release-artifact-matrix.sh' "$WF"; then
    echo "OK   release.yml wires matrix dry-run step"
  else
    fail "release.yml should run check-release-artifact-matrix.sh on workflow_dispatch dry-run"
  fi
fi

APP_VER="$(python3 -c "import json; print(json.load(open('schemas/golden-path/app-version.json'))['version'])" 2>/dev/null || true)"
if [ "$APP_VER" != "0.1.0" ]; then
  fail "schemas/golden-path/app-version.json must stay 0.1.0 for migration (got ${APP_VER:-missing})"
else
  echo "OK   product version 0.1.0"
fi

if [ "$DRY_RUN" -eq 1 ]; then
  echo ""
  echo "--- Dry-run: expected release extensions (product 0.1.0) ---"
  cat <<'TABLE'
platform   | extensions        | target filenames (examples)
-----------|-------------------|------------------------------------------
android    | .apk .aab         | app-release.apk app-release.aab
windows    | .msi .exe         | AetherFeed-1.0.0.msi AetherFeed-1.0.0.exe
macos      | .dmg .app         | AetherFeed-1.0.0.dmg AetherFeed.app
linux      | .deb .AppImage    | aetherfeed_1.0.0_amd64.deb *.AppImage
ios        | .ipa .zip         | AetherFeed-0.1.0-unsigned.ipa simulator zip
attestation| .json .yaml       | sbom*.cyclonedx.json manifest.stub.yaml
TABLE
  echo ""
  echo "NOTE: release.yml currently uploads attestation/SBOM assets only; platform binaries are manual or a follow-up job on the same triggers."
fi

if command -v gh >/dev/null 2>&1 && [ "$DRY_RUN" -eq 1 ]; then
  TAG="v$(tr -d '[:space:]' < .template-version 2>/dev/null || echo "")"
  if [ -n "$TAG" ] && [ "$TAG" != "v" ] && gh release view "$TAG" >/dev/null 2>&1; then
    echo ""
    echo "--- gh release view $TAG (asset extensions) ---"
    gh release view "$TAG" --json assets -q '.assets[].name' | while read -r name; do
      ext="${name##*.}"
      echo "  asset: $name (.$ext)"
    done
  else
    echo "SKIP gh release asset scan (no gh auth or release $TAG not found)"
  fi
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS check(s) failed"
  exit 1
fi

echo "Release artifact matrix check passed"