import type { Article } from "@aetherfeed/domain";
import { loadOfflineLibrary } from "../domain/library";
import { loadLocalPool } from "../sync/lanPoolClient";

export const STARRED_NODE = "starred";

export function folderKeyForUrl(url: string, tag?: string): string {
  const named = (tag || "").trim();
  if (named) return named;
  try {
    return new URL(url).hostname.replace(/^www\./i, "") || "Other";
  } catch {
    return "Other";
  }
}

export function feedIds(kind: string, categoryId: string): Set<string> | null {
  if (!categoryId || categoryId === STARRED_NODE) return null;
  const folderId = categoryId.startsWith("folder:")
    ? categoryId.slice("folder:".length)
    : categoryId;
  const ids = new Set<string>();
  for (const sub of loadLocalPool().subs) {
    if (sub.kind.toLowerCase() !== kind) continue;
    if (sub.url === categoryId || folderKeyForUrl(sub.url, sub.tag) === folderId) {
      ids.add(sub.url);
    }
  }
  if (!ids.size) ids.add(categoryId);
  return ids;
}

export function newsUnreadMaps(articles: Article[]): {
  byFeed: Map<string, number>;
  total: number;
  starred: number;
} {
  const pool = loadLocalPool();
  const library = loadOfflineLibrary();
  const reads = new Map(pool.reads.map((row) => [row.url, row.read]));
  const stars = new Set([
    ...library.stars.filter((row) => row.module === "news").map((row) => row.targetId),
    ...pool.stars.filter((row) => row.starred).map((row) => row.url),
  ]);
  const isUnread = (row: Article) => reads.get(row.url) !== true && reads.get(row.id) !== true;
  const byFeed = new Map<string, number>();
  let total = 0;
  let starred = 0;
  for (const row of articles) {
    if (!isUnread(row)) continue;
    total += 1;
    byFeed.set(row.feedId, (byFeed.get(row.feedId) || 0) + 1);
    if (stars.has(row.id) || stars.has(row.url)) starred += 1;
  }
  return { byFeed, total, starred };
}

export function newsStarsAndReads(): {
  reads: Map<string, boolean>;
  stars: Set<string>;
} {
  const pool = loadLocalPool();
  const library = loadOfflineLibrary();
  return {
    reads: new Map(pool.reads.map((row) => [row.url, row.read])),
    stars: new Set([
      ...library.stars.filter((row) => row.module === "news").map((row) => row.targetId),
      ...pool.stars.filter((row) => row.starred).map((row) => row.url),
    ]),
  };
}

export function podcastStarsAndPlayed(): { stars: Set<string>; played: Set<string> } {
  const library = loadOfflineLibrary();
  return {
    stars: new Set([
      ...library.stars.filter((row) => row.module === "podcast").map((row) => row.targetId),
      ...loadLocalPool()
        .stars.filter((row) => row.starred)
        .map((row) => row.url),
    ]),
    played: new Set(
      library.playback.filter((row) => row.positionMs > 0).map((row) => row.episodeId),
    ),
  };
}
