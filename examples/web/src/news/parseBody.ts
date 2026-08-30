import {
  type ParsedArticle,
  type ParsedFeed,
  parseJsonFeed,
  sniffFeedFormat,
} from "@aetherfeed/domain";

function epoch(value: string): number | undefined {
  if (!value) return undefined;
  const n = Date.parse(value);
  return Number.isFinite(n) ? n : undefined;
}

function firstText(parent: Element, names: string[]): string {
  for (const name of names) {
    const el = parent.getElementsByTagName(name)[0];
    const text = el?.textContent?.trim();
    if (text) return text;
  }
  return "";
}

function linkOf(el: Element): string {
  for (const link of Array.from(el.getElementsByTagName("link"))) {
    const href = link.getAttribute("href")?.trim();
    if (href) return href;
    const text = link.textContent?.trim();
    if (text) return text;
  }
  return "";
}

function parseXmlFeed(body: string, format: "rss" | "atom"): ParsedFeed | null {
  const doc = new DOMParser().parseFromString(body, "application/xml");
  if (doc.querySelector("parsererror")) return null;
  const root = doc.documentElement;
  const itemTag = format === "rss" ? "item" : "entry";
  const items: ParsedArticle[] = Array.from(doc.getElementsByTagName(itemTag)).map((el, index) => {
    const id = firstText(el, ["guid", "id"]) || linkOf(el) || `item-${index}`;
    const encoded = firstText(el, ["content:encoded", "encoded"]);
    const summary = firstText(el, ["description", "summary", "content"]);
    return {
      id,
      title: firstText(el, ["title"]) || "Untitled",
      url: linkOf(el) || firstText(el, ["link"]),
      summary,
      contentHtml: encoded || undefined,
      publishedAt: epoch(firstText(el, ["pubDate", "published", "updated"])),
    };
  });
  return {
    format,
    title: firstText(root, ["title"]) || "Untitled feed",
    siteUrl: linkOf(root) || undefined,
    items,
  };
}

export function parseFeedBody(body: string): ParsedFeed | null {
  const format = sniffFeedFormat(body);
  if (format === "jsonfeed") return parseJsonFeed(body);
  if (format === "rss" || format === "atom") return parseXmlFeed(body, format);
  try {
    return parseJsonFeed(body);
  } catch {
    return parseXmlFeed(body, "rss") ?? parseXmlFeed(body, "atom");
  }
}
