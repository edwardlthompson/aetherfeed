import type { Article } from "@aetherfeed/domain";
import { loadArticleCache, saveArticleCache } from "../lock";
import { createReaderView, extractReadable } from "../reader";
import { aroundArticles, readerPlaceholder } from "./articleNav";
import { stampCachedAt } from "./cacheRetain";
import { fetchFeedBody } from "./fetchBody";
import { usableBody } from "./fullText";
import { canFetchNews } from "./newsNetwork";

export async function resolveArticleHtml(
  article: Article,
  options: { fetch?: typeof fetch; signal?: AbortSignal; allowNetwork?: boolean } = {},
): Promise<string> {
  const vault = await loadArticleCache(article.id);
  const cached = usableBody(vault?.bodyHtml);
  if (cached) return cached;
  if (options.allowNetwork === false || !canFetchNews()) return "";
  if (!article.url.trim()) return "";
  const result = await fetchFeedBody(article.url, {
    fetch: options.fetch,
    timeoutMs: 15_000,
    signal: options.signal,
  });
  if (!result.ok) return "";
  const readable = extractReadable(result.body);
  return usableBody(readable.bodyHtml);
}

export async function paintReader(
  host: HTMLElement,
  article: Article,
  signal?: AbortSignal,
): Promise<boolean> {
  const vault = await loadArticleCache(article.id);
  const cached = usableBody(vault?.bodyHtml);
  const allowNetwork = canFetchNews();
  createReaderView(host, cached || readerPlaceholder(), {
    articleId: article.id,
    signal,
    allowNetwork: Boolean(cached) && allowNetwork,
  });
  if (cached || signal?.aborted) {
    if (cached) {
      await saveArticleCache(article.id, cached, vault?.images ?? {});
      stampCachedAt(article.id);
    }
    return Boolean(cached);
  }
  const html = await resolveArticleHtml(article, { signal, allowNetwork });
  if (signal?.aborted) return false;
  const handle = createReaderView(host, html || readerPlaceholder(), {
    articleId: article.id,
    signal,
    allowNetwork,
  });
  await handle.ready.catch(() => undefined);
  const prior = await loadArticleCache(article.id);
  if (html) {
    await saveArticleCache(article.id, html, prior?.images ?? {});
    stampCachedAt(article.id);
  }
  return false;
}

export function warmNeighbors(articles: Article[], currentId: string): void {
  for (const row of aroundArticles(articles, currentId)) {
    if (row.id === currentId) continue;
    void resolveArticleHtml(row).then((body) => {
      if (body) void saveArticleCache(row.id, body, {});
    });
  }
}
