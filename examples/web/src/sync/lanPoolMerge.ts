import { normalizeUrl } from "./lanPoolArticles";
import type { LanPoolDocument } from "./lanPoolTypes";

function mergeByUrl<T extends { url: string; updatedAt: number }>(
  local: T[],
  remote: T[],
  prefer?: (a: T, b: T) => T,
): T[] {
  const map = new Map<string, T>();
  for (const row of local) map.set(normalizeUrl(row.url), { ...row, url: normalizeUrl(row.url) });
  for (const row of remote) {
    const url = normalizeUrl(row.url);
    const next = { ...row, url };
    const prior = map.get(url);
    if (!prior || next.updatedAt > prior.updatedAt) map.set(url, next);
    else if (prefer && next.updatedAt === prior.updatedAt) map.set(url, prefer(prior, next));
  }
  return [...map.values()];
}

export function mergePoolDocuments(
  local: LanPoolDocument,
  remote: LanPoolDocument,
): LanPoolDocument {
  return {
    version: 2,
    updatedAt: Math.max(local.updatedAt || 0, remote.updatedAt || 0),
    subs: mergeByUrl(local.subs, remote.subs),
    reads: mergeByUrl(local.reads, remote.reads, (a, b) => (b.read ? b : a)),
    stars: mergeByUrl(local.stars, remote.stars, (a, b) => (b.starred ? b : a)),
    playback: mergeByUrl(local.playback, remote.playback, (a, b) =>
      b.positionMs > a.positionMs ? b : a,
    ),
  };
}
