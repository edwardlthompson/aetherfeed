import { BoardFetchError, type BoardPost, type TagQuery } from "./types";

function asString(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function asId(value: unknown): string {
  if (typeof value === "number" && Number.isFinite(value)) return String(value);
  return typeof value === "string" ? value.trim() : "";
}

function tagsOf(row: Record<string, unknown>): string[] {
  const raw = asString(row.tag_string) || asString(row.tags);
  return raw
    .split(/\s+/)
    .map((tag) => tag.toLowerCase())
    .filter(Boolean);
}

function parseOne(sourceId: string, row: unknown): BoardPost | undefined {
  if (!row || typeof row !== "object") return undefined;
  const rec = row as Record<string, unknown>;
  const remoteId = asId(rec.id);
  const fileUrl = asString(rec.file_url);
  if (!remoteId || !fileUrl) return undefined;
  const preview = asString(rec.preview_file_url) || asString(rec.preview_url);
  return {
    id: `${sourceId}:${remoteId}`,
    sourceId,
    remoteId,
    fileUrl,
    previewUrl: preview || undefined,
    tags: tagsOf(rec),
  };
}

export function parseBoardPosts(sourceId: string, payload: unknown): BoardPost[] {
  if (!Array.isArray(payload)) {
    throw new BoardFetchError("invalid", "Board payload must be a JSON array");
  }
  const posts: BoardPost[] = [];
  for (const row of payload) {
    const post = parseOne(sourceId, row);
    if (post) posts.push(post);
  }
  return posts;
}

export function parseTagQuery(raw: string, page = 1): TagQuery {
  const tags = raw
    .split(/\s+/)
    .map((tag) => tag.trim())
    .filter(Boolean);
  return { tags, page };
}
