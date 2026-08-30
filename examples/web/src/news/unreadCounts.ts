import type { Article, Feed } from "@aetherfeed/domain";

export function unreadByFeed(
  headlines: Article[],
  unread: (id: string) => boolean,
): Record<string, number> {
  const out: Record<string, number> = {};
  for (const row of headlines) {
    if (!unread(row.id)) continue;
    out[row.feedId] = (out[row.feedId] ?? 0) + 1;
  }
  return out;
}

export function unreadByFolder(
  byFeed: Record<string, number>,
  groups: [string, Feed[]][],
): Record<string, number> {
  const out: Record<string, number> = {};
  for (const [folder, feeds] of groups) {
    let n = 0;
    for (const feed of feeds) n += byFeed[feed.id] ?? 0;
    if (n > 0) out[folder] = n;
  }
  return out;
}

export function unreadBadge(count: number | undefined, kind: "folder" | "feed"): string {
  const n = count ?? 0;
  if (n <= 0) return "";
  const attr = kind === "folder" ? "data-news-folder-unread" : "data-news-feed-unread";
  return `<span class="af-news-unread" ${attr}>${n}</span>`;
}
