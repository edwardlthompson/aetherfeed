"""Init CLI must accept --profile used by upgrade simulation."""
from __future__ import annotations

import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


class InitProfileCliTests(unittest.TestCase):
    def test_bash_init_help_accepts_profile(self) -> None:
        proc = subprocess.run(
            ["bash", "scripts/init-project.sh", "--profile", "full", "--help"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        combined = (proc.stdout or "") + (proc.stderr or "")
        self.assertEqual(proc.returncode, 0, combined)
        self.assertIn("--profile", proc.stdout)
        self.assertNotIn("Unknown option", combined)

    def test_powershell_init_declares_profile(self) -> None:
        text = (ROOT / "scripts/init-project.ps1").read_text(encoding="utf-8")
        self.assertIn("[string]$Profile", text)
        self.assertIn("stamp_profile", text)


if __name__ == "__main__":
    unittest.main()
