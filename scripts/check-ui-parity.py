#!/usr/bin/env python3
"""Fail if required News/lock chrome IDs are missing from web or Android sources."""
from __future__ import annotations

import sys
from pathlib import Path

WEB_MARKERS = (
    "data-news-folders",
    "data-news-feeds",
    "data-news-timeline",
    "data-news-reader",
    "data-news-article",
    "data-news-refresh",
    "data-news-sort",
    "data-nav-unread",
    "data-unified-unread",
    "data-cache-retain",
    "data-news-cache-progress",
    "data-news-article-progress",
    "data-news-thumb",
    "data-news-cached",
    "data-news-sources",
    "data-lock-form",
    "data-news-feed-status",
    "data-news-folder-unread",
    "data-news-feed-unread",
    "data-action-bar",
    "data-action-prev",
    "data-action-next",
    "data-action-star",
    "data-action-unread",
    "data-action-play",
    "data-action-skip-back",
    "data-action-skip-fwd",
    "data-action-favorite",
    "data-action-info",
    "data-action-share",
)
ANDROID_MARKERS = (
    '"news-folders"',
    '"news-feeds"',
    '"news-timeline"',
    '"news-reader"',
    '"news-article"',
    '"news-refresh"',
    '"news-sort"',
    '"news-nav-unread"',
    '"unified-nav-unread"',
    '"cache-retain"',
    '"news-cache-progress"',
    '"news-article-progress"',
    '"news-thumb"',
    '"news-cached"',
    '"news-sources"',
    '"unlock-pane"',
    '"news-feed-status"',
    '"news-folder-unread"',
    '"news-feed-unread"',
    '"action-bar"',
    '"action-prev"',
    '"action-next"',
    '"action-star"',
    '"action-unread"',
    '"action-play"',
    '"action-skip-back"',
    '"action-skip-fwd"',
    '"action-favorite"',
    '"action-info"',
    '"action-share"',
)
SKIP_DIRS = {"node_modules", "dist", "build", ".gradle", "target"}


def collect_text(root: Path) -> str:
    chunks: list[str] = []
    for path in root.rglob("*"):
        if not path.is_file() or any(part in SKIP_DIRS for part in path.parts):
            continue
        if path.suffix.lower() not in {".ts", ".kt", ".tsx"}:
            continue
        chunks.append(path.read_text(encoding="utf-8"))
    return "\n".join(chunks)


def missing(blob: str, markers: tuple[str, ...]) -> list[str]:
    return [marker for marker in markers if marker not in blob]


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    web = collect_text(root / "examples" / "web" / "src")
    android = collect_text(root / "examples" / "android" / "app" / "src" / "main")
    failed = []
    for label, blob, markers in (
        ("web", web, WEB_MARKERS),
        ("android", android, ANDROID_MARKERS),
    ):
        absent = missing(blob, markers)
        if absent:
            failed.append(f"{label} missing: {', '.join(absent)}")
    if failed:
        print("UI parity check failed")
        for line in failed:
            print(line)
        return 1
    print("UI parity IDs present on web and Android")
    return 0


if __name__ == "__main__":
    sys.exit(main())
