import type { Article } from "@aetherfeed/domain";

export type ParsedFeed = { articles: Article[] };

function decode(raw: string): string {
  return raw
    .replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g, "$1")
    .replace(/<[^>]+>/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/\s+/g, " ")
    .trim();
}

function tagText(block: string, tag: string): string | undefined {
  const m = block.match(new RegExp(`<${tag}\\b[^>]*>([\\s\\S]*?)</${tag}>`, "i"));
  return m ? decode(m[1]) : undefined;
}

function itemImage(block: string): string | undefined {
  const media =
    block.match(/<media:thumbnail\b[^>]*url=["']([^"']+)["']/i) ||
    block.match(/<media:content\b[^>]*url=["']([^"']+\.(?:jpe?g|png|webp|gif)[^"']*)["']/i) ||
    block.match(/<enclosure\b[^>]*url=["']([^"']+\.(?:jpe?g|png|webp|gif)[^"']*)["']/i) ||
    block.match(/<img\b[^>]*src=["']([^"']+)["']/i);
  const url = media?.[1]?.trim();
  if (!url) return undefined;
  if (url.startsWith("//")) return `https:${url}`;
  return url.startsWith("http") ? url : undefined;
}

function linkHref(block: string): string | undefined {
  const href = block.match(/<link\b[^>]*href=["']([^"']+)["'][^>]*\/?>/i);
  if (href?.[1]) return href[1].trim();
  return tagText(block, "link");
}

function parseRss(xml: string, baseUrl: string): ParsedFeed {
  const channel = xml.match(/<channel\b[^>]*>([\s\S]*?)<\/channel>/i)?.[1] ?? xml;
  const feedId = tagText(channel, "link") || baseUrl;
  const articles: Article[] = [];
  const itemRe = /<item\b[^>]*>([\s\S]*?)<\/item>/gi;
  let match: RegExpExecArray | null;
  while ((match = itemRe.exec(channel))) {
    const block = match[1];
    const title = tagText(block, "title");
    if (!title) continue;
    const link = tagText(block, "link") || "";
    const guid = tagText(block, "guid") || link || title;
    const url = link || guid;
    articles.push({
      id: guid,
      feedId,
      title,
      url,
      summary: tagText(block, "description"),
      imageUrl: itemImage(block),
    });
  }
  return { articles };
}

function parseAtom(xml: string, baseUrl: string): ParsedFeed {
  const feedId = tagText(xml, "id") || linkHref(xml) || baseUrl;
  const articles: Article[] = [];
  const entryRe = /<entry\b[^>]*>([\s\S]*?)<\/entry>/gi;
  let match: RegExpExecArray | null;
  while ((match = entryRe.exec(xml))) {
    const block = match[1];
    const id = tagText(block, "id");
    if (!id) continue;
    articles.push({
      id,
      feedId,
      title: tagText(block, "title") || id,
      url: linkHref(block) || id,
      summary: tagText(block, "summary") || tagText(block, "content"),
      imageUrl: itemImage(block),
    });
  }
  return { articles };
}

export function parseFeed(xml: string, baseUrl: string): ParsedFeed {
  const lower = xml.slice(0, 400).toLowerCase();
  if (lower.includes("<rss") || lower.includes("<channel")) {
    return parseRss(xml, baseUrl);
  }
  if (lower.includes("<feed") || xml.toLowerCase().includes("<entry")) {
    return parseAtom(xml, baseUrl);
  }
  return { articles: [] };
}
