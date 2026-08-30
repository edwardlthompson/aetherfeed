import type { Article, Feed, NewsRepository } from "@aetherfeed/domain";
import { fetchFeedBody } from "./fetchBody";
import type { NewsFetchError } from "./fetchError";
import { parseFeedBody } from "./parseBody";

export type MemoryNewsOptions = {
  feeds?: Feed[];
  fetch?: typeof fetch;
  timeoutMs?: number;
};

function toArticles(
  feedId: string,
  items: {
    id: string;
    title: string;
    url: string;
    publishedAt?: number;
    summary?: string;
    contentHtml?: string;
  }[],
): Article[] {
  return items.map((item) => ({
    id: `${feedId}:${item.id}`,
    feedId,
    title: item.title,
    url: item.url,
    publishedAt: item.publishedAt,
    summary: item.summary,
    contentHtml: item.contentHtml,
  }));
}

export class MemoryNewsRepository implements NewsRepository {
  lastError: NewsFetchError | null = null;
  private feedRows: Feed[];
  private articleRows: Article[] = [];
  private readonly fetchImpl?: typeof fetch;
  private readonly timeoutMs: number;

  constructor(options: MemoryNewsOptions = {}) {
    this.feedRows = [...(options.feeds ?? [])];
    this.fetchImpl = options.fetch;
    this.timeoutMs = options.timeoutMs ?? 15_000;
  }

  async subscribe(url: string): Promise<Feed> {
    const existing = this.feedRows.find((feed) => feed.url === url);
    if (existing) return existing;
    const feed: Feed = { id: `feed:${url}`, title: url, url, kind: "news", updatedAt: Date.now() };
    this.feedRows.push(feed);
    return feed;
  }

  async updateFeed(feed: Feed): Promise<void> {
    const index = this.feedRows.findIndex((row) => row.id === feed.id);
    if (index >= 0) this.feedRows[index] = feed;
  }

  async unsubscribe(feedId: string): Promise<void> {
    this.feedRows = this.feedRows.filter((feed) => feed.id !== feedId);
    this.articleRows = this.articleRows.filter((article) => article.feedId !== feedId);
  }

  async importOpml(_xml: string): Promise<Feed[]> {
    return [];
  }

  async exportOpml(): Promise<string> {
    return '<?xml version="1.0"?><opml version="2.0"><head/><body/></opml>';
  }

  async refresh(feedId: string): Promise<Article[]> {
    this.lastError = null;
    const feed = this.feedRows.find((row) => row.id === feedId);
    if (!feed) {
      this.lastError = "missing";
      return [];
    }
    const result = await fetchFeedBody(feed.url, {
      fetch: this.fetchImpl,
      timeoutMs: this.timeoutMs,
    });
    if (!result.ok) {
      this.lastError = result.error;
      return this.articleRows.filter((article) => article.feedId === feedId);
    }
    const parsed = parseFeedBody(result.body);
    if (!parsed) {
      this.lastError = "parse";
      return this.articleRows.filter((article) => article.feedId === feedId);
    }
    const next = toArticles(feedId, parsed.items);
    this.articleRows = [
      ...this.articleRows.filter((article) => article.feedId !== feedId),
      ...next,
    ];
    feed.updatedAt = Date.now();
    return next;
  }

  feedTitle(feedId: string): string {
    return this.feedRows.find((feed) => feed.id === feedId)?.title ?? feedId;
  }

  hydrate(rows: Article[]): void {
    this.articleRows = [...rows];
  }

  async articles(feedId?: string): Promise<Article[]> {
    if (!feedId) return [...this.articleRows];
    return this.articleRows.filter((article) => article.feedId === feedId);
  }
}
