import { openUtf8, wrapUtf8 } from "./crypto";
import { readItem, writeItem } from "./rawCache";
import { sessionVaultKey, vaultKeyB64 } from "./session";
import { ART_PREFIX, THUMB_ITEM_PREFIX } from "./sessionMem";

export const ARTICLES_KEY = "af-lock-articles";
export const FEEDS_KEY = "af-lock-feeds";
export const THUMBS_KEY = "af-lock-thumbs";

type ArticleRow = { bodyHtml: string; images: Record<string, string> };

function secret(): string | null {
  return vaultKeyB64();
}

export async function saveEncryptedJson(key: string, value: unknown): Promise<void> {
  const vault = secret();
  if (!vault) return;
  localStorage.setItem(key, await wrapUtf8(JSON.stringify(value), vault));
}

export async function loadEncryptedJson<T>(key: string, fallback: T): Promise<T> {
  const vault = secret();
  const raw = localStorage.getItem(key);
  if (!vault || !raw) return fallback;
  try {
    return JSON.parse(await openUtf8(raw, vault)) as T;
  } catch {
    return fallback;
  }
}

function firstStoredThumb(images: Record<string, string>): string | undefined {
  return Object.values(images).find((blob) => typeof blob === "string" && blob.startsWith("data:"));
}

async function migrateLegacy(): Promise<void> {
  if (!sessionVaultKey()) return;
  if (localStorage.getItem(ARTICLES_KEY)) {
    const map = await loadEncryptedJson<Record<string, ArticleRow>>(ARTICLES_KEY, {});
    for (const [id, row] of Object.entries(map)) {
      if (!id.trim() || typeof row?.bodyHtml !== "string") continue;
      await writeItem(ART_PREFIX + id, JSON.stringify(row));
    }
    localStorage.removeItem(ARTICLES_KEY);
  }
  if (localStorage.getItem(THUMBS_KEY)) {
    const map = await loadEncryptedJson<Record<string, string>>(THUMBS_KEY, {});
    for (const [id, thumb] of Object.entries(map)) {
      if (typeof thumb === "string" && thumb.startsWith("data:")) {
        await writeItem(THUMB_ITEM_PREFIX + id, thumb);
      }
    }
    localStorage.removeItem(THUMBS_KEY);
  }
}

export async function saveArticleCache(
  articleId: string,
  bodyHtml: string,
  images: Record<string, string>,
): Promise<void> {
  const id = articleId.trim();
  if (!id || !sessionVaultKey()) return;
  await migrateLegacy();
  await writeItem(ART_PREFIX + id, JSON.stringify({ bodyHtml, images }));
  const thumb = firstStoredThumb(images);
  if (thumb) await saveThumb(id, thumb);
}

export async function loadArticleCache(articleId: string): Promise<ArticleRow | null> {
  const id = articleId.trim();
  if (!id) return null;
  await migrateLegacy();
  const raw = await readItem(ART_PREFIX + id);
  if (!raw) return null;
  try {
    const row = JSON.parse(raw) as ArticleRow;
    return typeof row.bodyHtml === "string" ? row : null;
  } catch {
    return null;
  }
}

export async function saveThumb(articleId: string, dataUrl: string): Promise<void> {
  const id = articleId.trim();
  if (!id || !dataUrl.startsWith("data:") || !sessionVaultKey()) return;
  await writeItem(THUMB_ITEM_PREFIX + id, dataUrl);
}

export async function saveThumbs(thumbs: Record<string, string>): Promise<void> {
  if (!sessionVaultKey()) return;
  for (const [id, thumb] of Object.entries(thumbs)) {
    if (id.trim() && thumb.startsWith("data:")) await saveThumb(id, thumb);
  }
}

export async function loadThumbs(ids: string[]): Promise<Record<string, string>> {
  await migrateLegacy();
  const out: Record<string, string> = {};
  const missing: string[] = [];
  for (const id of ids) {
    const thumb = await readItem(THUMB_ITEM_PREFIX + id);
    if (thumb?.startsWith("data:")) out[id] = thumb;
    else missing.push(id);
  }
  if (!missing.length) return out;
  const extras = await thumbsFromVaultImages(missing);
  if (Object.keys(extras).length) {
    await saveThumbs(extras);
    Object.assign(out, extras);
  }
  return out;
}

export async function thumbsFromVaultImages(ids: string[]): Promise<Record<string, string>> {
  const out: Record<string, string> = {};
  for (const id of ids) {
    const row = await loadArticleCache(id);
    const thumb = row?.images ? firstStoredThumb(row.images) : undefined;
    if (thumb) out[id] = thumb;
  }
  return out;
}
