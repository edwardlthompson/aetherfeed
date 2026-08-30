"""Parse a local Inoreader OPML and write imported-feeds.json for the desktop app."""

from __future__ import annotations

import json
import re
import sys
import time
from pathlib import Path
from urllib.parse import urlparse

PODCAST_HOSTS = (
    "libsyn.com",
    "megaphone.fm",
    "buzzsprout.com",
    "spreaker.com",
    "anchor.fm",
    "podbean.com",
    "simplecast.com",
    "transistor.fm",
    "captivate.fm",
    "omny.fm",
    "podcasts.apple.com",
    "pinecast.com",
    "art19.com",
)


def strip_feed_prefix(url: str) -> str:
    trimmed = url.strip()
    return trimmed[5:] if trimmed.lower().startswith("feed/") else trimmed


def normalize_feed_url(url: str) -> str:
    raw = strip_feed_prefix(url)
    try:
        parsed = urlparse(raw if "://" in raw else f"https://{raw}")
        host = (parsed.hostname or "").lower()
        if host.startswith("www."):
            host = host[4:]
        path = (parsed.path or "").rstrip("/")
        protocol = "https" if parsed.scheme in ("http", "https") else parsed.scheme or "https"
        return f"{protocol}://{host}{path}".lower()
    except Exception:
        return raw.lower().rstrip("/")


def classify(title: str, url: str, folder: str) -> str:
    if re.search(r"podcast|audio", folder, re.I) or re.search(r"podcast", title, re.I):
        return "podcast"
    host = urlparse(strip_feed_prefix(url) if "://" in strip_feed_prefix(url) else f"https://{url}").hostname or ""
    host = host.lower()
    if host.startswith("www."):
        host = host[4:]
    if any(host == known or host.endswith(f".{known}") for known in PODCAST_HOSTS):
        return "podcast"
    if "/podcast" in (urlparse(url).path or "").lower():
        return "podcast"
    return "news"


def parse_opml(xml: str) -> list[dict]:
    tokens = re.findall(r"</outline>|<outline\b[^>]*/?>", xml, flags=re.I)
    outlines: list[dict] = []
    stack: list[str] = []
    for token in tokens:
        if re.match(r"</outline>", token, flags=re.I):
            if stack:
                stack.pop()
            continue
        xml_url = attr(token, "xmlUrl")
        title = attr(token, "title") or attr(token, "text") or xml_url or "Untitled"
        folder = next((name for name in reversed(stack) if name), "")
        if xml_url:
            outlines.append({"title": title, "url": xml_url, "folder": folder})
        if not token.strip().endswith("/>"):
            stack.append("" if xml_url else title)
    return outlines


def attr(tag: str, name: str) -> str | None:
    match = re.search(rf"""{name}\s*=\s*["']([^"']*)["']""", tag, flags=re.I)
    return match.group(1) if match else None


def main() -> int:
    src = Path(sys.argv[1] if len(sys.argv) > 1 else r"C:\Users\edwar\Downloads\Inoreader Feeds 20260819.xml")
    dest = Path(sys.argv[2] if len(sys.argv) > 2 else Path.home() / "AppData/Roaming/org.aetherfeed.app/imported-feeds.json")
    now = int(time.time() * 1000)
    feeds = []
    seen: set[str] = set()
    for item in parse_opml(src.read_text(encoding="utf-8")):
        key = normalize_feed_url(item["url"])
        if key in seen:
            continue
        seen.add(key)
        kind = classify(item["title"], item["url"], item["folder"])
        record = {
            "id": f"feed:{key}",
            "title": item["title"],
            "url": item["url"],
            "kind": kind,
            "updatedAt": now,
        }
        if item["folder"]:
            record["folder"] = item["folder"]
        feeds.append(record)
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(json.dumps(feeds, ensure_ascii=False), encoding="utf-8")
    news = sum(1 for feed in feeds if feed["kind"] == "news")
    podcasts = sum(1 for feed in feeds if feed["kind"] == "podcast")
    print(f"wrote {len(feeds)} feeds ({news} news, {podcasts} podcasts) -> {dest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
