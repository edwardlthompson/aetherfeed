import type { Article } from "@aetherfeed/domain";
import { loadArticleCache, saveArticleCache } from "../lock";
import { isFeedExcerpt } from "./fullText";

export function alreadyFetched(row: { bodyHtml?: string } | null | undefined): boolean {
  return row != null && typeof row.bodyHtml === "string";
}

export async function pendingUnread(
  articles: Article[],
  unread: (id: string) => boolean,
): Promise<Article[]> {
  const out: Article[] = [];
  for (const article of articles) {
    if (!unread(article.id)) continue;
    const row = await loadArticleCache(article.id);
    if (alreadyFetched(row) && !isFeedExcerpt(row?.bodyHtml)) continue;
    out.push(article);
  }
  return out;
}

export async function markFetched(articleId: string): Promise<void> {
  const prior = await loadArticleCache(articleId);
  if (alreadyFetched(prior)) return;
  await saveArticleCache(articleId, " ", {});
}
