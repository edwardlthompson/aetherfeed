import {
  ARTICLES_KEY,
  loadArticleCache,
  loadEncryptedJson,
  saveArticleCache,
  webAppLock,
} from "../lock";
import type { VaultEntry } from "./types";

export const VAULT_KEY = "af-reader-vault";

type VaultMap = Record<string, { bodyHtml: string; images: Record<string, string> }>;

let memory: VaultMap = {};

function readMap(): VaultMap {
  try {
    const raw = localStorage.getItem(VAULT_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as VaultMap;
    return parsed && typeof parsed === "object" && !Array.isArray(parsed) ? parsed : {};
  } catch {
    return {};
  }
}

function writeMap(map: VaultMap): void {
  try {
    localStorage.setItem(VAULT_KEY, JSON.stringify(map));
  } catch {
    /* quota — memory only */
  }
}

export function saveArticle(
  articleId: string,
  bodyHtml: string,
  images: Record<string, string> = {},
): void {
  const id = articleId?.trim();
  if (!id) return;
  memory[id] = { bodyHtml: bodyHtml ?? "", images: images ?? {} };
  if (webAppLock.state() === "unlocked") {
    void saveArticleCache(id, bodyHtml ?? "", images ?? {});
    localStorage.removeItem(VAULT_KEY);
    return;
  }
  if (webAppLock.state() === "locked") return;
  const map = readMap();
  map[id] = memory[id];
  writeMap(map);
}

export function loadArticle(articleId: string): VaultEntry | null {
  const id = articleId?.trim();
  if (!id) return null;
  const row = memory[id] ?? (webAppLock.state() === "unlocked" ? undefined : readMap()[id]);
  if (!row || typeof row.bodyHtml !== "string") return null;
  const images =
    row.images && typeof row.images === "object" && !Array.isArray(row.images) ? row.images : {};
  return { articleId: id, bodyHtml: row.bodyHtml, images };
}

export async function hydrateVault(): Promise<void> {
  if (webAppLock.state() !== "unlocked") {
    memory = {};
    return;
  }
  const enc = await loadEncryptedJson<VaultMap>(ARTICLES_KEY, {});
  const plain = readMap();
  memory = { ...plain, ...enc };
  if (Object.keys(plain).length) {
    for (const [id, row] of Object.entries(plain)) {
      await saveArticleCache(id, row.bodyHtml, row.images ?? {});
    }
    localStorage.removeItem(VAULT_KEY);
  }
}

export function clearVault(): void {
  memory = {};
  localStorage.removeItem(VAULT_KEY);
}

export { loadArticleCache };
