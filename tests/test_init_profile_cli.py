"""Init CLI must accept --profile used by upgrade simulation."""
from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


class InitProfileCliTests(unittest.TestCase):
    def test_bash_init_declares_profile(self) -> None:
        text = (ROOT / "scripts/init-project.sh").read_text(encoding="utf-8")
        self.assertIn("--profile)", text)
        self.assertIn("stamp_profile", text)
        self.assertNotIn('echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;\n    --profile)', text)

    def test_powershell_init_declares_profile(self) -> None:
        text = (ROOT / "scripts/init-project.ps1").read_text(encoding="utf-8")
        self.assertIn("[string]$Profile", text)
        self.assertIn("stamp_profile", text)


if __name__ == "__main__":
    unittest.main()

