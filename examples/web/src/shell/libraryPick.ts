import type { AppMode } from "../AppShellTypes";

const SEP = "\u001f";

export type LibraryPick =
  | { kind: "unified" }
  | { kind: "all"; mode: AppMode }
  | { kind: "folder"; mode: AppMode; folder: string }
  | { kind: "source"; mode: AppMode; folder: string; sourceId: string };

export type LibraryItem = {
  mode: AppMode;
  id: string;
  title: string;
  publishedAt: number | null;
  url: string;
  thumb?: string;
};

export function pickMode(pick: LibraryPick): AppMode {
  return pick.kind === "unified" ? "news" : pick.mode;
}

export function parentPick(pick: LibraryPick): LibraryPick | null {
  if (pick.kind === "folder") return { kind: "all", mode: pick.mode };
  if (pick.kind === "source") return { kind: "folder", mode: pick.mode, folder: pick.folder };
  return null;
}

export function encodeLibraryPick(pick: LibraryPick): string {
  if (pick.kind === "unified") return "unified";
  if (pick.kind === "all") return `all${SEP}${pick.mode}`;
  if (pick.kind === "folder") return `folder${SEP}${pick.mode}${SEP}${pick.folder}`;
  return `source${SEP}${pick.mode}${SEP}${pick.folder}${SEP}${pick.sourceId}`;
}

export function parseLibraryPick(raw: string | null | undefined): LibraryPick {
  const text = raw?.trim() ?? "";
  if (!text || text === "unified") return { kind: "unified" };
  const parts = text.split(SEP);
  const mode = (parts[1] === "podcast" || parts[1] === "booru" ? parts[1] : "news") as AppMode;
  if (parts[0] === "all") return { kind: "all", mode };
  if (parts[0] === "folder" && parts[2]) return { kind: "folder", mode, folder: parts[2] };
  if (parts[0] === "source" && parts[3]) {
    return { kind: "source", mode, folder: parts[2] ?? "", sourceId: parts.slice(3).join(SEP) };
  }
  return { kind: "all", mode: "news" };
}

export function sortLibraryItems(items: LibraryItem[], oldestFirst: boolean): LibraryItem[] {
  const ordered = [...items].sort((left, right) => {
    const a = left.publishedAt ?? Number.MAX_SAFE_INTEGER;
    const b = right.publishedAt ?? Number.MAX_SAFE_INTEGER;
    return a === b ? left.id.localeCompare(right.id) : a - b;
  });
  return oldestFirst ? ordered : ordered.reverse();
}

export function mergeUnified(
  news: LibraryItem[],
  podcasts: LibraryItem[],
  boards: LibraryItem[],
  oldestFirst: boolean,
): LibraryItem[] {
  return sortLibraryItems([...news, ...podcasts, ...boards], oldestFirst);
}

export function shareUrl(item: LibraryItem | null | undefined): string | undefined {
  const url = item?.url.trim();
  return url || undefined;
}
