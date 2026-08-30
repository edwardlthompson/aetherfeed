import type { Feed, OpmlOutline } from "@aetherfeed/domain";
import { feedsToOpml, libraryExport } from "@aetherfeed/domain";
import { escapeXml } from "./escape";

function outlineXml(outline: OpmlOutline): string {
  const xmlUrl = outline.xmlUrl ? ` xmlUrl="${escapeXml(outline.xmlUrl)}"` : "";
  const htmlUrl = outline.htmlUrl ? ` htmlUrl="${escapeXml(outline.htmlUrl)}"` : "";
  const title = escapeXml(outline.title);
  return `<outline text="${title}" title="${title}"${xmlUrl}${htmlUrl}/>`;
}

export function buildOpmlXml(feeds: Feed[], now = Date.now()): string {
  const snapshot = libraryExport(feeds, now);
  const outlines = feedsToOpml(snapshot.feeds);
  const groups = new Map<string, OpmlOutline[]>();
  snapshot.feeds.forEach((feed, index) => {
    const outline = outlines[index];
    if (!outline) return;
    const folder = feed.folder?.trim() || "Unfiled";
    const rows = groups.get(folder) ?? [];
    rows.push(outline);
    groups.set(folder, rows);
  });
  const body = [...groups.entries()]
    .sort(([left], [right]) => left.localeCompare(right))
    .map(([folder, rows]) => {
      const kids = rows.map(outlineXml).join("");
      return `<outline text="${escapeXml(folder)}">${kids}</outline>`;
    })
    .join("");
  const created = new Date(snapshot.exportedAt).toUTCString();
  return `<?xml version="1.0" encoding="UTF-8"?><opml version="2.0"><head><title>AetherFeed</title><dateCreated>${created}</dateCreated></head><body>${body}</body></opml>`;
}
