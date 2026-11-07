"""TEMPLATE_INDEX path checker."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from validate_template_index import check, collect_missing  # noqa: E402


class TemplateIndexTests(unittest.TestCase):
    def test_missing_and_ok(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "scripts").mkdir()
            (root / ".github" / "workflows").mkdir(parents=True)
            (root / "keep.md").write_text("x\n", encoding="utf-8")
            data = {
                "entry_points": {"a": "keep.md"},
                "files": [{"path": "keep.md"}, {"path": "gone.md"}],
                "modules": {},
            }
            (root / "TEMPLATE_INDEX.json").write_text(
                json.dumps(data), encoding="utf-8"
            )
            self.assertEqual(collect_missing(root, data), ["gone.md"])
            errors = check(root)
            self.assertTrue(any("gone.md" in e for e in errors))

    def test_repo_index_omits_gitignored_cursor_runtime_files(self) -> None:
        root = Path(__file__).resolve().parent.parent
        data = json.loads((root / "TEMPLATE_INDEX.json").read_text(encoding="utf-8"))
        indexed = {item["path"] for item in data.get("files", [])}
        forbidden = {
            ".cursor/agent-progress.json",
            ".cursor/last-feature-gate.json",
            ".cursor/mcp-audit.log",
            ".cursor/parallel-scope-lock.json",
        }
        self.assertFalse(indexed & forbidden)
