/** Locked public API for file-only reader subscription import. */

import type { OpmlOutline } from "./news";
import { parseOpmlOutlines, parseReaderJson } from "./readerImportParse";

export type ReaderVendor = "google-reader" | "inoreader" | "opml" | "unknown";

export type ReaderImportErrorCode = "empty" | "malformed" | "skipped";

export type ReaderImportError = {
  code: ReaderImportErrorCode;
  message: string;
  outline?: string;
};

export type ReaderImportFile = {
  name: string;
  text?: string;
  members?: Record<string, string>;
};

export type ReaderImportStar = {
  url: string;
  title?: string;
};

export type ReaderImportRead = {
  url: string;
  status: "unread" | "in_progress" | "read";
};

export type ReaderImportParsed = {
  vendor: ReaderVendor;
  outlines: OpmlOutline[];
  stars: ReaderImportStar[];
  reads: ReaderImportRead[];
  errors: ReaderImportError[];
};

export type ReaderImportResult = {
  vendor: ReaderVendor;
  feedsAdded: number;
  feedsSkipped: number;
  starsApplied: number;
  newsAdded: number;
  podcastsAdded: number;
  errors: ReaderImportError[];
};

export function detectReaderVendor(file: ReaderImportFile): ReaderVendor {
  const name = file.name.toLowerCase();
  const members = Object.keys(file.members ?? {}).join(" ").toLowerCase();
  const blob = (file.text ?? Object.values(file.members ?? {}).join("\n")).slice(0, 4000).toLowerCase();
  const hay = `${name} ${members} ${blob}`;
  if (
    name.includes("subscriptions.xml") ||
    name.includes("starred.json") ||
    members.includes("subscriptions.xml") ||
    /takeout|greader|google.?reader/.test(hay)
  ) {
    return "google-reader";
  }
  if (/inoreader/.test(hay)) return "inoreader";
  if (/<opml\b/.test(blob) || name.endsWith(".opml") || /\.opml/.test(members)) return "opml";
  return "unknown";
}

function collectTexts(file: ReaderImportFile): string[] {
  const members = file.members ?? {};
  const keys = Object.keys(members);
  if (keys.length) {
    const picked = keys.filter((k) => /\.(xml|opml|json)$/i.test(k) || /subscriptions|starred|inoreader/i.test(k));
    return (picked.length ? picked : keys).map((k) => members[k] ?? "");
  }
  return file.text != null ? [file.text] : [];
}

export function isHardParseFailure(parsed: ReaderImportParsed): boolean {
  return parsed.outlines.length === 0 && parsed.stars.length === 0;
}

export function parseReaderImport(file: ReaderImportFile): ReaderImportParsed {
  const vendor = detectReaderVendor(file);
  const texts = collectTexts(file);
  if (!texts.length || texts.every((t) => !t.trim())) {
    return { vendor, outlines: [], stars: [], reads: [], errors: [{ code: "empty", message: "Import file is empty" }] };
  }
  const outlines: OpmlOutline[] = [];
  const stars: ReaderImportStar[] = [];
  const reads: ReaderImportRead[] = [];
  const errors: ReaderImportError[] = [];
  for (const text of texts) {
    const trimmed = text.trim();
    if (!trimmed) continue;
    if (trimmed.startsWith("<") || /<opml\b/i.test(trimmed)) {
      try {
        outlines.push(...parseOpmlOutlines(trimmed));
      } catch {
        errors.push({ code: "malformed", message: "Malformed OPML" });
      }
      continue;
    }
    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
      try {
        const extra = parseReaderJson(trimmed);
        outlines.push(...extra.outlines);
        stars.push(...extra.stars);
        reads.push(...extra.reads);
      } catch {
        errors.push({ code: "malformed", message: "Malformed JSON backup" });
      }
    }
  }
  if (isHardParseFailure({ vendor, outlines, stars, reads, errors }) && !errors.length) {
    errors.push({ code: "malformed", message: "No subscriptions found" });
  }
  return { vendor, outlines, stars, reads, errors };
}
