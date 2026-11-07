"""Android versionName must follow Golden Path app-version.json."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from golden_path_app_version import check  # noqa: E402


class GoldenPathAppVersionTests(unittest.TestCase):
    def test_repo_gradle_reads_app_version_json(self) -> None:
        gradle_path = ROOT / "examples/android/app/build.gradle.kts"
        if not gradle_path.is_file():
            self.skipTest("android example pruned")
        gradle = gradle_path.read_text(encoding="utf-8")
        self.assertIn("readGoldenPathAppVersion", gradle)
        self.assertIn("app-version.json", gradle)
        self.assertIn("org.aetherfeed.app", gradle)

    def test_check_passes_on_repo(self) -> None:
        self.assertEqual(check(ROOT), [])


if __name__ == "__main__":
    unittest.main()
