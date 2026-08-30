export type Chapter = {
  title: string;
  startMs: number;
};

function asRecord(value: unknown): Record<string, unknown> | undefined {
  return value && typeof value === "object" ? (value as Record<string, unknown>) : undefined;
}

function parseClock(raw: string): number | undefined {
  const parts = raw.trim().split(":").map(Number);
  if (!parts.length || parts.some((part) => !Number.isFinite(part))) return undefined;
  if (parts.length === 1) return Math.floor(parts[0] * 1000);
  if (parts.length === 2) return Math.floor((parts[0] * 60 + parts[1]) * 1000);
  if (parts.length === 3) return Math.floor((parts[0] * 3600 + parts[1] * 60 + parts[2]) * 1000);
  return undefined;
}

function resolveStartMs(row: Record<string, unknown>): number | undefined {
  if (typeof row.startMs === "number" && Number.isFinite(row.startMs) && row.startMs >= 0) {
    return Math.floor(row.startMs);
  }
  if (typeof row.startTime === "number" && Number.isFinite(row.startTime) && row.startTime >= 0) {
    return Math.floor(row.startTime * 1000);
  }
  if (typeof row.startTime === "string") return parseClock(row.startTime);
  return undefined;
}

function parseOne(row: unknown): Chapter | undefined {
  const rec = asRecord(row);
  if (!rec) return undefined;
  const title = typeof rec.title === "string" ? rec.title.trim() : "";
  if (!title) return undefined;
  const startMs = resolveStartMs(rec);
  if (startMs === undefined) return undefined;
  return { title, startMs };
}

function rowsOf(input: unknown): unknown[] {
  if (Array.isArray(input)) return input;
  const rec = asRecord(input);
  if (Array.isArray(rec?.chapters)) return rec.chapters;
  if (
    rec &&
    (typeof rec.title === "string" || rec.startMs !== undefined || rec.startTime !== undefined)
  ) {
    return [rec];
  }
  return [];
}

export function parseChapters(input: unknown): Chapter[] {
  let value = input;
  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed) return [];
    try {
      value = JSON.parse(trimmed) as unknown;
    } catch {
      return [];
    }
  }
  if (value == null || typeof value === "boolean" || typeof value === "number") return [];
  const chapters: Chapter[] = [];
  for (const row of rowsOf(value)) {
    const chapter = parseOne(row);
    if (chapter) chapters.push(chapter);
  }
  return chapters.sort((a, b) => a.startMs - b.startMs);
}
