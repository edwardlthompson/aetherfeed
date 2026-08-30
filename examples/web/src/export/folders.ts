import type { Feed } from "@aetherfeed/domain";

export function uniqueFolders(feeds: Feed[]): string[] {
  const names = new Set<string>();
  for (const feed of feeds) names.add(feed.folder?.trim() || "");
  return [...names].sort((left, right) => left.localeCompare(right));
}

export function applyFolderRenames(feeds: Feed[], renames: Record<string, string>): Feed[] {
  return feeds.map((feed) => {
    const key = feed.folder?.trim() || "";
    if (!(key in renames)) return feed;
    const next = renames[key]?.trim() ?? "";
    return { ...feed, folder: next || undefined };
  });
}

export function collectFolderRenames(root: ParentNode): Record<string, string> {
  const renames: Record<string, string> = {};
  for (const input of root.querySelectorAll<HTMLInputElement>("[data-folder-rename]")) {
    renames[input.dataset.folderFrom ?? ""] = input.value;
  }
  return renames;
}
