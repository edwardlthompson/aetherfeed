import { loadLocalPool, saveLocalPool } from "./lanPoolStorage";
import type { LanPoolDocument } from "./lanPoolTypes";

function articleKeys(article: { id: string; url: string }): string[] {
  return [article.url, article.id]
    .map((value) => normalizeUrl(value))
    .filter((value, index, all) => value.length > 0 && all.indexOf(value) === index);
}

export function normalizeUrl(url: string): string {
  let t = url.trim();
  while (t.length > 1 && t.endsWith("/")) t = t.slice(0, -1);
  return t;
}

export function markArticleRead(
  article: { id: string; url: string },
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  return setArticleRead(article, true, now, storage);
}

export function markArticleUnread(
  article: { id: string; url: string },
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  return setArticleRead(article, false, now, storage);
}

export function setArticleRead(
  article: { id: string; url: string },
  read: boolean,
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  const pool = loadLocalPool(storage);
  const byUrl = new Map(pool.reads.map((row) => [normalizeUrl(row.url), row]));
  for (const key of articleKeys(article)) {
    byUrl.set(key, { url: key, read, updatedAt: now });
  }
  const next = {
    ...pool,
    version: 2 as const,
    updatedAt: Math.max(pool.updatedAt || 0, now),
    reads: [...byUrl.values()],
  };
  saveLocalPool(next, storage);
  return next;
}

export function isArticleRead(
  article: { id: string; url: string },
  storage: Pick<Storage, "getItem"> = localStorage,
): boolean {
  const reads = new Map(
    loadLocalPool(storage).reads.map((row) => [normalizeUrl(row.url), row.read]),
  );
  return articleKeys(article).some((key) => reads.get(key) === true);
}

export function setArticleStarred(
  article: { id: string; url: string },
  starred: boolean,
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  const pool = loadLocalPool(storage);
  const byUrl = new Map(pool.stars.map((row) => [normalizeUrl(row.url), row]));
  for (const key of articleKeys(article)) {
    byUrl.set(key, { url: key, starred, updatedAt: now });
  }
  const next = {
    ...pool,
    version: 2 as const,
    updatedAt: Math.max(pool.updatedAt || 0, now),
    stars: [...byUrl.values()],
  };
  saveLocalPool(next, storage);
  return next;
}

export function isArticleStarred(
  article: { id: string; url: string },
  storage: Pick<Storage, "getItem"> = localStorage,
): boolean {
  const stars = new Map(
    loadLocalPool(storage).stars.map((row) => [normalizeUrl(row.url), row.starred]),
  );
  return articleKeys(article).some((key) => stars.get(key) === true);
}
