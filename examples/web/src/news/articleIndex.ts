import type { Article } from "@aetherfeed/domain";
import { loadEncryptedJson, saveEncryptedJson } from "../lock/cache";

export const ARTICLE_INDEX_KEY = "af-lock-article-index";

export function parseArticleIndex(raw: unknown): Article[] {
  if (!Array.isArray(raw)) return [];
  return raw.flatMap((row) => {
    if (!row || typeof row !== "object") return [];
    const rec = row as Record<string, unknown>;
    if (typeof rec.id !== "string" || !rec.id.trim()) return [];
    if (typeof rec.feedId !== "string" || !rec.feedId.trim()) return [];
    if (typeof rec.title !== "string" || typeof rec.url !== "string") return [];
    return [
      {
        id: rec.id,
        feedId: rec.feedId,
        title: rec.title,
        url: rec.url,
        publishedAt: typeof rec.publishedAt === "number" ? rec.publishedAt : undefined,
        summary: typeof rec.summary === "string" ? rec.summary : undefined,
        contentHtml: typeof rec.contentHtml === "string" ? rec.contentHtml : undefined,
      },
    ];
  });
}

export async function loadArticleIndex(): Promise<Article[]> {
  return parseArticleIndex(await loadEncryptedJson<unknown>(ARTICLE_INDEX_KEY, []));
}

export async function saveArticleIndex(rows: Article[]): Promise<void> {
  await saveEncryptedJson(ARTICLE_INDEX_KEY, rows);
}
