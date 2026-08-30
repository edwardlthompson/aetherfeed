/** OPML and vendor JSON extractors for reader import. */

import type { OpmlOutline } from "./news";
import type { ReaderImportRead, ReaderImportStar } from "./readerImport";

function outlineAttr(tag: string, name: string): string | undefined {
  const match = new RegExp(`${name}\\s*=\\s*["']([^"']*)["']`, "i").exec(tag);
  return match?.[1];
}

export function parseOpmlOutlines(xml: string): OpmlOutline[] {
  const tokens = xml.match(/<\/outline>|<outline\b[^>]*\/?>/gi) ?? [];
  const outlines: OpmlOutline[] = [];
  const stack: string[] = [];
  for (const token of tokens) {
    if (/^<\/outline>/i.test(token)) {
      stack.pop();
      continue;
    }
    const xmlUrl = outlineAttr(token, "xmlUrl");
    const title = outlineAttr(token, "title") ?? outlineAttr(token, "text") ?? xmlUrl ?? "Untitled";
    const folder = [...stack].reverse().find((name) => name) || undefined;
    if (xmlUrl) {
      outlines.push({
        title,
        xmlUrl,
        htmlUrl: outlineAttr(token, "htmlUrl"),
        folder,
        type: outlineAttr(token, "type"),
      });
    }
    if (!/\/>$/.test(token.trim())) stack.push(xmlUrl ? "" : title);
  }
  return outlines;
}

function hrefOf(item: Record<string, unknown>): string | undefined {
  for (const key of ["canonical", "alternate"] as const) {
    const list = item[key];
    if (Array.isArray(list) && list[0] && typeof list[0] === "object") {
      const href = (list[0] as { href?: unknown }).href;
      if (typeof href === "string" && href) return href;
    }
  }
  for (const key of ["feedUrl", "xmlUrl", "url"]) {
    const value = item[key];
    if (typeof value === "string" && value) return value;
  }
  return undefined;
}

function asRecord(value: unknown): Record<string, unknown> | undefined {
  return value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : undefined;
}

export function parseReaderJson(text: string): {
  outlines: OpmlOutline[];
  stars: ReaderImportStar[];
  reads: ReaderImportRead[];
} {
  const data: unknown = JSON.parse(text);
  const root = asRecord(data) ?? {};
  const outlines: OpmlOutline[] = [];
  const stars: ReaderImportStar[] = [];
  const reads: ReaderImportRead[] = [];
  const starred =
    (typeof root.id === "string" && root.id.includes("starred")) ||
    (typeof root.title === "string" && /starred/i.test(root.title));

  const pushFeed = (item: Record<string, unknown>) => {
    const url = hrefOf(item);
    if (!url) return;
    const title = typeof item.title === "string" ? item.title : url;
    const htmlUrl = typeof item.htmlUrl === "string" ? item.htmlUrl : undefined;
    if (starred || item.canonical) {
      stars.push({ url, title });
      return;
    }
    outlines.push({ title, xmlUrl: url, htmlUrl });
  };

  const arrays: unknown[] = Array.isArray(data) ? data : [];
  for (const key of ["feeds", "subscriptions", "items"]) {
    const value = root[key];
    if (Array.isArray(value)) arrays.push(...value);
  }
  for (const raw of arrays) {
    const item = asRecord(raw);
    if (item) pushFeed(item);
  }
  const unread = root.unread;
  if (Array.isArray(unread)) {
    for (const raw of unread) {
      const item = asRecord(raw);
      const url = item ? hrefOf(item) : undefined;
      if (url) reads.push({ url, status: "unread" });
    }
  }
  return { outlines, stars, reads };
}
