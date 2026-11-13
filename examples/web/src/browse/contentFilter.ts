export type SortOrder = "newest" | "oldest";

export type TreeNode = {
  id: string;
  label: string;
  unreadCount?: number;
  children?: TreeNode[];
};

export function applyBrowseFilter<T>(
  items: T[],
  opts: {
    unreadOnly: boolean;
    starredOnly: boolean;
    inCategory: (item: T) => boolean;
    isUnread: (item: T) => boolean;
    isStarred: (item: T) => boolean;
  },
): T[] {
  return items.filter((item) => {
    if (!opts.inCategory(item)) return false;
    if (opts.unreadOnly && !opts.isUnread(item)) return false;
    if (opts.starredOnly && !opts.isStarred(item)) return false;
    return true;
  });
}

export function sortByDate<T>(items: T[], order: SortOrder, at: (item: T) => number): T[] {
  const copy = [...items].sort((a, b) => at(b) - at(a));
  return order === "oldest" ? copy.reverse() : copy;
}

export function pruneEmptyTree(
  nodes: TreeNode[],
  keepIds: Set<string> = new Set([""]),
): TreeNode[] {
  const out: TreeNode[] = [];
  for (const node of nodes) {
    const children = node.children ? pruneEmptyTree(node.children, keepIds) : undefined;
    const unread = node.unreadCount ?? 0;
    const keep = keepIds.has(node.id) || unread > 0 || (children != null && children.length > 0);
    if (!keep) continue;
    out.push(children ? { ...node, children } : node);
  }
  return out;
}

export function uniqueCategories(
  rows: Array<{ id: string; label: string }>,
): Array<{ id: string; label: string }> {
  const seen = new Map<string, string>();
  for (const row of rows) {
    const id = row.id.trim();
    if (!id || seen.has(id)) continue;
    seen.set(id, row.label.trim() || id);
  }
  return [...seen.entries()].map(([id, label]) => ({ id, label }));
}

export function topTags(tagLists: string[][], limit = 12): Array<{ id: string; label: string }> {
  const counts = new Map<string, number>();
  for (const tags of tagLists) {
    for (const raw of tags) {
      const tag = raw.trim();
      if (!tag) continue;
      counts.set(tag, (counts.get(tag) || 0) + 1);
    }
  }
  return [...counts.entries()]
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .slice(0, limit)
    .map(([id]) => ({ id, label: id }));
}
