"""README stack badges follow branding/product.json, not template defaults."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from readme_badges import check_repo, rewrite_template_badge  # noqa: E402


def _write_product(root: Path, stacks: list[str]) -> None:
    branding = root / "branding"
    branding.mkdir()
    (branding / "product.json").write_text(
        json.dumps({"stacks": stacks}),
        encoding="utf-8",
    )


class ReadmeBadgeTests(unittest.TestCase):
    def test_product_stacks_required_python_forbidden(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            _write_product(root, ["android", "desktop", "web"])
            (root / ".template-version").write_text("1.12.0\n", encoding="utf-8")
            (root / "README.md").write_text(
                "\n".join(
                    [
                        "https://img.shields.io/badge/template-1.12.0-0969da",
                        "https://img.shields.io/badge/license-MIT-2ea043",
                        "https://img.shields.io/badge/FOSS-no_tracking-656d76",
                        "https://github.com/ex/repo/actions/workflows/ci.yml",
                        "https://img.shields.io/badge/AGENT-2ea043",
                        "https://img.shields.io/badge/HUMAN-0969da",
                        "https://img.shields.io/badge/ADB-bf8700",
                        "https://img.shields.io/badge/AUTO-656d76",
                        "https://img.shields.io/badge/android-stack-3D5A6C",
                        "https://img.shields.io/badge/desktop-stack-5C6B5A",
                        "https://img.shields.io/badge/web-stack-3DDC84",
                        "https://img.shields.io/badge/python-stack-3776AB",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )
            errors = check_repo(root)
            self.assertTrue(any("python stack badge must not appear" in e for e in errors))

    def test_product_stacks_pass_without_python(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            _write_product(root, ["android", "desktop", "web"])
            (root / ".template-version").write_text("1.12.0\n", encoding="utf-8")
            (root / "README.md").write_text(
                "\n".join(
                    [
                        "https://img.shields.io/badge/template-1.12.0-0969da",
                        "https://img.shields.io/badge/license-MIT-2ea043",
                        "https://img.shields.io/badge/FOSS-no_tracking-656d76",
                        "https://github.com/ex/repo/actions/workflows/ci.yml",
                        "https://img.shields.io/badge/AGENT-2ea043",
                        "https://img.shields.io/badge/HUMAN-0969da",
                        "https://img.shields.io/badge/ADB-bf8700",
                        "https://img.shields.io/badge/AUTO-656d76",
                        "https://img.shields.io/badge/android-stack-3D5A6C",
                        "https://img.shields.io/badge/desktop-stack-5C6B5A",
                        "https://img.shields.io/badge/web-stack-3DDC84",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )
            self.assertEqual(check_repo(root), [])

    def test_rewrite_html_and_markdown_template_badges(self) -> None:
        html = '<img src="https://img.shields.io/badge/template-1.12.0-0969da?style=flat-square" alt="template" />'
        md = "![Template](https://img.shields.io/badge/template-1.12.0-0969da)"
        self.assertIn("template-1.13.0", rewrite_template_badge(html, "1.13.0"))
        self.assertIn("template-1.13.0", rewrite_template_badge(md, "1.13.0"))
        self.assertNotIn("template-1.12.0", rewrite_template_badge(html, "1.13.0"))

    def test_check_repo_requires_html_template_badge_version(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            _write_product(root, ["android", "desktop", "web"])
            (root / ".template-version").write_text("1.13.0\n", encoding="utf-8")
            (root / "README.md").write_text(
                "\n".join(
                    [
                        '<img src="https://img.shields.io/badge/template-1.12.0-0969da?style=flat-square" alt="template" />',
                        "https://img.shields.io/badge/license-MIT-2ea043",
                        "https://img.shields.io/badge/FOSS-no_tracking-656d76",
                        "https://github.com/ex/repo/actions/workflows/ci.yml",
                        "https://img.shields.io/badge/AGENT-2ea043",
                        "https://img.shields.io/badge/HUMAN-0969da",
                        "https://img.shields.io/badge/ADB-bf8700",
                        "https://img.shields.io/badge/AUTO-656d76",
                        "https://img.shields.io/badge/android-stack-3D5A6C",
                        "https://img.shields.io/badge/desktop-stack-5C6B5A",
                        "https://img.shields.io/badge/web-stack-3DDC84",
                    ]
                )
                + "\n",
                encoding="utf-8",
            )
            errors = check_repo(root)
            self.assertTrue(any("hero template badge must be template-1.13.0" in e for e in errors))


if __name__ == "__main__":
    unittest.main()
