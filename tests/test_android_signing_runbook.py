"""Android signing runbook must stay env-only and documented."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from android_signing_runbook import check  # noqa: E402


class AndroidSigningRunbookTests(unittest.TestCase):
    def test_repo_passes_signing_runbook(self) -> None:
        self.assertEqual(check(ROOT), [])


if __name__ == "__main__":
    unittest.main()
