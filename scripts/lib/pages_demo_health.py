"""Check README Pages demo badge/link — or confirm local-only products opted out."""
from __future__ import annotations

import json
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def load_product(root: Path | None = None) -> dict:
    base = root or ROOT
    product = base / "branding" / "product.json"
    if not product.is_file():
        return {}
    try:
        data = json.loads(product.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        return {}
    return data if isinstance(data, dict) else {}


def expected_pages_url(root: Path | None = None) -> str | None:
    data = load_product(root)
    repo = (data.get("urls") or {}).get("github_repo") or ""
    if "/" not in repo:
        return None
    owner, name = repo.split("/", 1)
    return f"https://{owner}.github.io/{name}/"


def pages_demo_enabled(root: Path | None = None) -> bool:
    data = load_product(root)
    # Default true for template children that still host a demo; AetherFeed sets false.
    return data.get("pages_demo", True) is not False


def check_readme(root: Path | None = None) -> list[str]:
    base = root or ROOT
    url = expected_pages_url(base)
    readme = (base / "README.md").read_text(encoding="utf-8")
    errors: list[str] = []
    if not pages_demo_enabled(base):
        if url and (url.rstrip("/") in readme or url in readme):
            errors.append(f"pages_demo is false but README still links {url}")
        if "pages-demo" in readme.lower():
            errors.append("pages_demo is false but README still has a Pages-demo badge")
        return errors
    if not url:
        return ["missing branding/product.json urls.github_repo"]
    if url.rstrip("/") not in readme and url not in readme:
        errors.append(f"README.md must link the Pages demo URL ({url})")
    if "pages-demo" not in readme.lower() and "GitHub Pages" not in readme:
        errors.append("README.md must mention GitHub Pages demo")
    return errors


def probe(url: str, timeout: float = 8.0) -> str:
    req = urllib.request.Request(url, method="HEAD", headers={"User-Agent": "agent-project-bootstrap-pages-check"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return f"HTTP {getattr(resp, 'status', 200)}"
    except urllib.error.HTTPError as exc:
        return f"HTTP {exc.code}"
    except Exception as exc:  # noqa: BLE001 — soft health probe
        return f"unreachable ({exc.__class__.__name__})"


def main() -> int:
    errors = check_readme()
    if errors:
        print("Pages demo link health failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    if not pages_demo_enabled():
        print("Pages demo disabled (pages_demo=false) — local-only product; no public hosted UI")
        return 0
    url = expected_pages_url()
    assert url is not None
    status = probe(url)
    print(f"Pages demo URL in README: {url}")
    print(f"Probe: {status}")
    if status.startswith("HTTP 4") or status.startswith("HTTP 5"):
        print("NOTE: Pages returned an error status — confirm workflow pages.yml has run on main")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
