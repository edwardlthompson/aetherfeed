import type { FeedFormat, ParsedArticle, ParsedFeed } from "./news";

export function sniffFeedFormat(body: string): FeedFormat | null {
  const trimmed = body.trim();
  if (trimmed.startsWith("{")) {
    try {
      const parsed = JSON.parse(trimmed) as { version?: string };
      if (typeof parsed.version === "string" && parsed.version.includes("jsonfeed")) {
        return "jsonfeed";
      }
    } catch {
      return null;
    }
  }
  if (trimmed.includes("<rss") || trimmed.includes("<RSS")) return "rss";
  if (trimmed.includes("<feed") && trimmed.includes("http://www.w3.org/2005/Atom")) return "atom";
  return null;
}

export function parseJsonFeed(body: string): ParsedFeed {
  const parsed = JSON.parse(body) as {
    title?: string;
    home_page_url?: string;
    items?: Array<{
      id?: string;
      url?: string;
      title?: string;
      summary?: string;
      content_html?: string;
      date_published?: string;
    }>;
  };
  const items: ParsedArticle[] = (parsed.items ?? []).map((item, index) => ({
    id: item.id || item.url || `item-${index}`,
    title: item.title || "Untitled",
    url: item.url || "",
    summary: item.summary,
    contentHtml: item.content_html,
    publishedAt: item.date_published ? Date.parse(item.date_published) : undefined,
  }));
  return {
    format: "jsonfeed",
    title: parsed.title || "Untitled feed",
    siteUrl: parsed.home_page_url,
    items,
  };
}
