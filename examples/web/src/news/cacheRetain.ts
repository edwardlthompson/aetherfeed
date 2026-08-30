export const CACHE_RETAIN_MS = 30 * 86_400_000;

export type CacheRetainMode = "days30" | "sync";

export function shouldDropCache(
  cachedAt: number | null | undefined,
  starred: boolean,
  mode: CacheRetainMode,
  now: number,
  sweepTrigger: boolean,
): boolean {
  if (starred || cachedAt == null) return false;
  if (mode === "days30") return now - cachedAt >= CACHE_RETAIN_MS;
  return sweepTrigger;
}

export function parseCacheRetainMode(raw: string | null | undefined): CacheRetainMode {
  return raw?.trim() === "sync" ? "sync" : "days30";
}

const RETAIN_KEY = "af-cache-retain";

export function loadCacheRetainMode(): CacheRetainMode {
  return parseCacheRetainMode(globalThis.localStorage?.getItem(RETAIN_KEY));
}

export function saveCacheRetainMode(mode: CacheRetainMode): void {
  globalThis.localStorage?.setItem(RETAIN_KEY, mode);
}

const ART = "af-lock-art:";
const THUMB = "af-lock-thumb:";
const AT = "af-cached-at:";
const STAR_KEY = "af-news-stars";

export function loadStarredIds(): Set<string> {
  try {
    const raw = JSON.parse(globalThis.localStorage?.getItem(STAR_KEY) ?? "[]") as unknown;
    return new Set(
      Array.isArray(raw) ? raw.filter((id): id is string => typeof id === "string") : [],
    );
  } catch {
    return new Set();
  }
}

export function saveStarredIds(ids: Iterable<string>): void {
  globalThis.localStorage?.setItem(STAR_KEY, JSON.stringify([...ids]));
}

export function stampCachedAt(id: string, now = Date.now()): void {
  if (!id.trim() || globalThis.localStorage?.getItem(AT + id)) return;
  globalThis.localStorage?.setItem(AT + id, String(now));
}

export function readCachedAt(id: string): number | null {
  const n = Number(globalThis.localStorage?.getItem(AT + id));
  return Number.isFinite(n) ? n : null;
}

export function dropArticleCache(id: string): void {
  if (!id.trim()) return;
  globalThis.localStorage?.removeItem(ART + id);
  globalThis.localStorage?.removeItem(THUMB + id);
  globalThis.localStorage?.removeItem(AT + id);
}

export function idsToDrop(
  rows: { id: string; cachedAt: number | null }[],
  starred: ReadonlySet<string>,
  mode: CacheRetainMode,
  now: number,
  sweepTrigger: boolean,
  skip: ReadonlySet<string> = new Set(),
): string[] {
  return rows
    .filter(
      (row) =>
        row.id &&
        !skip.has(row.id) &&
        shouldDropCache(row.cachedAt, starred.has(row.id), mode, now, sweepTrigger),
    )
    .map((row) => row.id);
}

export function sweepArticleCache(
  sweepTrigger: boolean,
  now = Date.now(),
  skip: ReadonlySet<string> = new Set(),
): number {
  const store = globalThis.localStorage;
  if (!store) return 0;
  const starred = loadStarredIds();
  const mode = loadCacheRetainMode();
  let gone = 0;
  for (const key of Object.keys(store)) {
    if (!key.startsWith(ART)) continue;
    const id = key.slice(ART.length);
    if (
      !id ||
      skip.has(id) ||
      !shouldDropCache(readCachedAt(id), starred.has(id), mode, now, sweepTrigger)
    ) {
      continue;
    }
    dropArticleCache(id);
    gone += 1;
  }
  return gone;
}

export function dropIfUnstarred(id: string): void {
  if (shouldDropCache(readCachedAt(id), false, loadCacheRetainMode(), Date.now(), false)) {
    dropArticleCache(id);
  }
}
