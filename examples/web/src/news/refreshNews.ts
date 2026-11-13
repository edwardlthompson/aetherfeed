import type { Article } from "@aetherfeed/domain";
import { loadOfflineLibrary, saveOfflineLibrary } from "../domain/library";
import { loadLocalPool } from "../sync/lanPoolClient";
import { detectTauriInvoke } from "../sync/tauriInvoke";
import { parseFeed } from "./parseFeed";

const FEED_LIMIT = 40;
const ARTICLE_LIMIT = 120;

export async function fetchFeedText(url: string): Promise<string> {
  const invoke = detectTauriInvoke();
  if (invoke) {
    return invoke<string>("http_get_text", { url });
  }
  const resp = await fetch(url);
  if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
  return resp.text();
}

export async function refreshNewsArticles(
  getText: (url: string) => Promise<string> = fetchFeedText,
): Promise<Article[]> {
  const pool = loadLocalPool();
  const feeds = [...pool.subs]
    .filter((s) => s.kind.toLowerCase() === "news")
    .sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
    .slice(0, FEED_LIMIT);
  const incoming: Article[] = (
    await Promise.all(
      feeds.map(async (feed) => {
        try {
          const xml = await getText(feed.url);
          return parseFeed(xml, feed.url).articles.map((row) => ({ ...row, feedId: feed.url }));
        } catch {
          return [] as Article[];
        }
      }),
    )
  ).flat();
  const prior = loadOfflineLibrary();
  const byId = new Map<string, Article>();
  for (const row of [...prior.articles, ...incoming]) {
    byId.set(row.id, row);
  }
  const articles = [...byId.values()]
    .sort((a, b) => (b.publishedAt || 0) - (a.publishedAt || 0))
    .slice(0, ARTICLE_LIMIT);
  saveOfflineLibrary({ ...prior, articles });
  return articles;
}

export function unreadCountFor(articles: Article[]): number {
  const reads = new Map(loadLocalPool().reads.map((r) => [r.url, r.read]));
  return articles.filter((a) => reads.get(a.url) !== true && reads.get(a.id) !== true).length;
}
