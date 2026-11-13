import type { Article, Episode } from "@aetherfeed/domain";
import { loadOfflineLibrary } from "../domain/library";
import { t } from "../i18n";
import { loadLocalPool } from "../sync/lanPoolClient";
import { pruneEmptyTree, type TreeNode } from "./contentFilter";
import { folderKeyForUrl, newsUnreadMaps } from "./wireFilterHelpers";

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
    { id: "starred", label: t("filter.starred"), unreadCount: opts.starredUnread ?? 0 },
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
