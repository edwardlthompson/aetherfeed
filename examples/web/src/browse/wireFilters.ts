import type { Article, Episode } from "@aetherfeed/domain";
import { loadOfflineLibrary } from "../domain/library";
import { t } from "../i18n";
import { loadLocalPool } from "../sync/lanPoolClient";
import {
  applyBrowseFilter,
  pruneEmptyTree,
  type SortOrder,
  sortByDate,
  type TreeNode,
} from "./contentFilter";

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

function feedIds(kind: string, categoryId: string): Set<string> | null {
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

function newsUnreadMaps(articles: Article[]): {
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

export function subscriptionTree(
  kind: string,
  opts: {
    unreadById?: Map<string, number>;
    totalUnread?: number;
    starredUnread?: number;
    hideEmpty?: boolean;
  } = {},
): TreeNode[] {
  const unreadById = opts.unreadById ?? new Map<string, number>();
  const folders = new Map<string, TreeNode[]>();
  for (const sub of loadLocalPool().subs.filter((row) => row.kind.toLowerCase() === kind)) {
    const leaf: TreeNode = {
      id: sub.url,
      label: sub.title || sub.url,
      unreadCount: unreadById.get(sub.url) || 0,
    };
    const folder = folderKeyForUrl(sub.url, sub.tag);
    const kids = folders.get(folder) ?? [];
    kids.push(leaf);
    folders.set(folder, kids);
  }
  const nodes: TreeNode[] = [
    { id: "", label: t("filter.all"), unreadCount: opts.totalUnread ?? 0 },
    { id: STARRED_NODE, label: t("filter.starred"), unreadCount: opts.starredUnread ?? 0 },
  ];
  for (const [folder, children] of [...folders.entries()].sort((a, b) =>
    a[0].localeCompare(b[0]),
  )) {
    const unreadCount = children.reduce((sum, child) => sum + (child.unreadCount || 0), 0);
    nodes.push({
      id: `folder:${folder}`,
      label: folder,
      unreadCount,
      children: children.sort((a, b) => a.label.localeCompare(b.label)),
    });
  }
  return opts.hideEmpty ? pruneEmptyTree(nodes) : nodes;
}

export function newsSubscriptionTree(articles: Article[], hideEmpty: boolean): TreeNode[] {
  const counts = newsUnreadMaps(articles);
  return subscriptionTree("news", {
    unreadById: counts.byFeed,
    totalUnread: counts.total,
    starredUnread: counts.starred,
    hideEmpty,
  });
}

export function filterNews(
  articles: Article[],
  unreadOnly: boolean,
  nodeId: string,
  sort: SortOrder,
): Article[] {
  const pool = loadLocalPool();
  const library = loadOfflineLibrary();
  const reads = new Map(pool.reads.map((row) => [row.url, row.read]));
  const stars = new Set([
    ...library.stars.filter((row) => row.module === "news").map((row) => row.targetId),
    ...pool.stars.filter((row) => row.starred).map((row) => row.url),
  ]);
  const allowed = feedIds("news", nodeId);
  return sortByDate(
    applyBrowseFilter(articles, {
      unreadOnly,
      starredOnly: nodeId === STARRED_NODE,
      inCategory: (row) => !allowed || allowed.has(row.feedId),
      isUnread: (row) => reads.get(row.url) !== true && reads.get(row.id) !== true,
      isStarred: (row) => stars.has(row.id) || stars.has(row.url),
    }),
    sort,
    (row) => row.publishedAt || 0,
  );
}

export function filterEpisodes(
  episodes: Episode[],
  unreadOnly: boolean,
  nodeId: string,
  sort: SortOrder,
): Episode[] {
  const library = loadOfflineLibrary();
  const stars = new Set([
    ...library.stars.filter((row) => row.module === "podcast").map((row) => row.targetId),
    ...loadLocalPool()
      .stars.filter((row) => row.starred)
      .map((row) => row.url),
  ]);
  const played = new Set(
    library.playback.filter((row) => row.positionMs > 0).map((row) => row.episodeId),
  );
  const allowed = feedIds("podcast", nodeId);
  return sortByDate(
    applyBrowseFilter(episodes, {
      unreadOnly,
      starredOnly: nodeId === STARRED_NODE,
      inCategory: (row) => !allowed || allowed.has(row.showId),
      isUnread: (row) => !played.has(row.id),
      isStarred: (row) => stars.has(row.id) || stars.has(row.enclosureUrl),
    }),
    sort,
    (row) => row.publishedAt || 0,
  );
}

export function podcastSubscriptionTree(episodes: Episode[], hideEmpty: boolean): TreeNode[] {
  const library = loadOfflineLibrary();
  const played = new Set(
    library.playback.filter((row) => row.positionMs > 0).map((row) => row.episodeId),
  );
  const unreadById = new Map<string, number>();
  let total = 0;
  for (const ep of episodes) {
    if (played.has(ep.id)) continue;
    total += 1;
    unreadById.set(ep.showId, (unreadById.get(ep.showId) || 0) + 1);
  }
  return subscriptionTree("podcast", {
    unreadById,
    totalUnread: total,
    starredUnread: 0,
    hideEmpty,
  });
}
