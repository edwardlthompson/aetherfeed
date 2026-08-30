import type { Article } from "@aetherfeed/domain";
import { t } from "../i18n";
import { loadArticleCache, loadThumbs, saveArticleCache } from "../lock";
import { extractReadable, fetchImageBlobs } from "../reader";
import { prefetchAfter } from "./articleNav";
import { stampCachedAt } from "./cacheRetain";
import { alreadyFetched, markFetched, pendingUnread } from "./cacheSkip";
import { isFeedExcerpt, usableBody } from "./fullText";
import { canFetchNews } from "./newsNetwork";
import { resolveArticleHtml } from "./openArticle";

export type CacheProgress = { done: number; total: number };

export function cacheProgressLabel(done: number, total: number): string {
  return t("news.cache.progress").replace("{done}", String(done)).replace("{total}", String(total));
}

export function renderCacheProgress(
  done: number,
  total: number,
  label = cacheProgressLabel(done, total),
): string {
  if (total <= 0 || done >= total) return "";
  const pct = Math.round((done / total) * 100);
  return `<div class="af-progress" data-news-cache-progress role="progressbar" aria-valuemin="0" aria-valuemax="${total}" aria-valuenow="${done}"><span class="af-progress-fill" style="width:${pct}%"></span><span class="af-progress-label">${label}</span></div>`;
}

export async function cachedIdsFor(ids: string[]): Promise<string[]> {
  const out: string[] = [];
  for (const id of ids) {
    const row = await loadArticleCache(id);
    if (usableBody(row?.bodyHtml)) out.push(id);
  }
  return out;
}

export function paintCachedDot(root: HTMLElement, id: string): void {
  const meta = root.querySelector(`[data-article-id="${CSS.escape(id)}"] .af-news-row-meta`);
  if (!meta || meta.querySelector("[data-news-cached]")) return;
  meta.insertAdjacentHTML("afterbegin", `<span class="af-news-cached" data-news-cached></span>`);
}

export function paintThumb(root: HTMLElement, id: string, src: string): void {
  const btn = root.querySelector(`[data-article-id="${CSS.escape(id)}"]`);
  if (!btn || btn.querySelector("[data-news-thumb]")) return;
  btn.insertAdjacentHTML(
    "afterbegin",
    `<img class="af-news-thumb" data-news-thumb alt="" src="${src}">`,
  );
}

export function paintCacheProgress(root: HTMLElement, html: string): void {
  const host = root.querySelector("[data-news-timeline]");
  const prev = root.querySelector("[data-news-cache-progress]");
  if (!html) {
    prev?.remove();
    return;
  }
  if (prev) {
    prev.outerHTML = html;
    return;
  }
  host?.querySelector(".af-news-status")?.insertAdjacentHTML("afterend", html);
}

export async function thumbsFor(ids: string[]): Promise<Record<string, string>> {
  return loadThumbs(ids);
}

export async function cacheOneUnread(article: Article, signal?: AbortSignal): Promise<void> {
  const prior = await loadArticleCache(article.id);
  if (alreadyFetched(prior) && !isFeedExcerpt(prior?.bodyHtml)) return;
  try {
    const html = await resolveArticleHtml(article, { signal, allowNetwork: canFetchNews() });
    const readable = extractReadable(html || "");
    const missing = readable.imageSrcs;
    const fresh =
      missing.length > 0 && canFetchNews() ? await fetchImageBlobs(missing, { signal }) : {};
    await saveArticleCache(article.id, readable.bodyHtml || html || " ", fresh);
    stampCachedAt(article.id);
  } catch {
    await markFetched(article.id);
  }
}

export function watchUnreadPrefetch(
  root: HTMLElement,
  articles: Article[],
  unread: (id: string) => boolean,
  onThumbs: (thumbs: Record<string, string>, cached?: string[]) => void,
  previous?: AbortController,
  startId?: string | null,
): AbortController {
  previous?.abort();
  const ctrl = new AbortController();
  void prefetchUnread(
    prefetchAfter(articles, startId ?? null, unread),
    () => true,
    (progress) => {
      if (ctrl.signal.aborted) return;
      paintCacheProgress(root, renderCacheProgress(progress.done, progress.total));
      void Promise.all([
        thumbsFor(articles.map((article) => article.id)),
        cachedIdsFor(articles.map((article) => article.id)),
      ]).then(([next, ids]) => {
        onThumbs(next, ids);
        for (const [id, src] of Object.entries(next)) paintThumb(root, id, src);
        for (const id of ids) paintCachedDot(root, id);
      });
    },
    ctrl.signal,
  );
  return ctrl;
}

export async function prefetchUnread(
  articles: Article[],
  unread: (id: string) => boolean,
  onProgress: (progress: CacheProgress) => void,
  signal?: AbortSignal,
): Promise<void> {
  const queue = await pendingUnread(articles, unread);
  let done = 0;
  onProgress({ done, total: queue.length });
  for (const article of queue) {
    if (signal?.aborted) return;
    try {
      await cacheOneUnread(article, signal);
    } catch {
      await markFetched(article.id);
    }
    done += 1;
    onProgress({ done, total: queue.length });
  }
}
