/** Normalize subscription URLs and classify news vs podcast from an export. */

import type { ModuleKind } from "./models";
import type { OpmlOutline } from "./news";

const PODCAST_HOSTS = [
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
];

export function stripFeedPrefix(url: string): string {
  const trimmed = url.trim();
  return trimmed.toLowerCase().startsWith("feed/") ? trimmed.slice(5) : trimmed;
}

export function normalizeFeedUrl(url: string): string {
  const raw = stripFeedPrefix(url);
  try {
    const parsed = new URL(raw.includes("://") ? raw : `https://${raw}`);
    let host = parsed.hostname.toLowerCase();
    if (host.startsWith("www.")) host = host.slice(4);
    const path = parsed.pathname.replace(/\/+$/, "");
    const protocol = parsed.protocol === "http:" || parsed.protocol === "https:" ? "https:" : parsed.protocol;
    return `${protocol}//${host}${path}`.toLowerCase();
  } catch {
    return raw.toLowerCase().replace(/\/+$/, "");
  }
}

function hostOf(url: string): string | undefined {
  try {
    let host = new URL(stripFeedPrefix(url).includes("://") ? stripFeedPrefix(url) : `https://${url}`).hostname.toLowerCase();
    if (host.startsWith("www.")) host = host.slice(4);
    return host;
  } catch {
    return undefined;
  }
}

export function classifyFeedKind(outline: OpmlOutline): ModuleKind {
  const folder = outline.folder ?? "";
  const title = outline.title;
  if (/podcast|audio/i.test(folder) || /podcast/i.test(title)) return "podcast";
  const url = outline.xmlUrl ?? "";
  const host = hostOf(url);
  if (host && PODCAST_HOSTS.some((known) => host === known || host.endsWith(`.${known}`))) return "podcast";
  try {
    const path = new URL(stripFeedPrefix(url).includes("://") ? stripFeedPrefix(url) : `https://${url}`).pathname;
    if (/\/podcast/i.test(path)) return "podcast";
  } catch {
    /* keep news */
  }
  return "news";
}
