"""Fail when README hero/stack/owner badges drift from repo truth."""
from __future__ import annotations

import json
import re
from pathlib import Path

TEMPLATE_BADGE_RE = re.compile(r"(badge/template-)[\d.]+")


def rewrite_template_badge(text: str, version: str) -> str:
    """Update markdown or HTML template version badges to ``version``."""
    return TEMPLATE_BADGE_RE.sub(lambda match: f"{match.group(1)}{version}", text)


OWNER_COLORS = {
    "AGENT": "2ea043",
    "HUMAN": "0969da",
    "ADB": "bf8700",
    "AUTO": "656d76",
}
STACK_COLORS = {
    "web": "646cff",
    "python": "3776AB",
    "android": "3DDC84",
}


def product_stacks(root: Path) -> list[str]:
    path = root / "branding" / "product.json"
    if not path.is_file():
        return []
    data = json.loads(path.read_text(encoding="utf-8"))
    stacks = data.get("stacks") or []
    return [str(stack) for stack in stacks]


def check_repo(root: Path) -> list[str]:
    readme = (root / "README.md").read_text(encoding="utf-8")
    version = (root / ".template-version").read_text(encoding="utf-8").strip()
    errors: list[str] = []
    if f"badge/template-{version}" not in readme:
        errors.append(f"hero template badge must be template-{version}")
    if "badge/license-MIT" not in readme:
        errors.append("hero license badge must be MIT")
    if "FOSS-no_tracking" not in readme:
        errors.append("hero FOSS badge must say no_tracking")
    if "actions/workflows/ci.yml" not in readme:
        errors.append("CI badge must link ci.yml")
    for label, color in OWNER_COLORS.items():
        needle = f"badge/{label}-"
        if needle not in readme or color not in readme:
            errors.append(f"owner badge {label} / {color} missing")
        elif f"{label}-" not in readme or color.lower() not in readme.lower():
            errors.append(f"owner badge {label} color {color} missing")
    stacks = product_stacks(root)
    if stacks:
        for stack in stacks:
            if f"badge/{stack}-stack-" not in readme:
                errors.append(f"stack badge {stack} missing")
        if "python" not in stacks and "badge/python-stack-" in readme:
            errors.append("python stack badge must not appear when product.json omits python")
    else:
        for stack, color in STACK_COLORS.items():
            if f"badge/{stack}-stack-{color}" not in readme:
                errors.append(f"stack badge {stack} must use {color}")
    return errors


def main() -> int:
    errors = check_repo(Path.cwd())
    if errors:
        print("README badge accuracy check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    print("README badge accuracy check passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
