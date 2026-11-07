"""Child AGENT.md.example must stay the AetherFeed brief, not Golden Path."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from agent_brief import parse_brief  # noqa: E402
from check_agent_brief import check  # noqa: E402


class AgentBriefExampleTests(unittest.TestCase):
    def test_example_matches_sacred_brief(self) -> None:
        live = parse_brief((ROOT / "AGENT.md").read_text(encoding="utf-8"))
        example = parse_brief((ROOT / "AGENT.md.example").read_text(encoding="utf-8"))
        self.assertEqual(example["one_liner"], live["one_liner"])
        self.assertEqual(example["keywords"], live["keywords"])
        self.assertNotIn("camera", example["keywords"])
        self.assertNotIn("neon", example["keywords"])

    def test_live_repo_passes_agent_brief_gate(self) -> None:
        self.assertEqual(check(ROOT), [])


if __name__ == "__main__":
    unittest.main()
