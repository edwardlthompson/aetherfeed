import type { Episode } from "@aetherfeed/domain";

export type ParsedPodcast = {
  showTitle: string;
  episodes: Episode[];
};

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

function enclosureUrl(block: string): string | undefined {
  const tagged = block.match(
    /<enclosure\b[^>]*url=["']([^"']+)["'][^>]*(?:type=["']([^"']+)["'])?[^>]*\/?>/i,
  );
  if (tagged?.[1]) {
    const type = (tagged[2] || "").toLowerCase();
    const url = tagged[1].trim();
    if (type.startsWith("audio") || /\.(mp3|m4a|aac|ogg|opus)(\?|$)/i.test(url)) return url;
  }
  const media = block.match(
    /<media:content\b[^>]*url=["']([^"']+)["'][^>]*(?:type=["']([^"']+)["'])?/i,
  );
  if (media?.[1] && (media[2] || "").toLowerCase().startsWith("audio")) return media[1].trim();
  return undefined;
}

export function parsePodcastFeed(xml: string, feedUrl: string): ParsedPodcast {
  const channel = xml.match(/<channel\b[^>]*>([\s\S]*?)<\/channel>/i)?.[1] ?? xml;
  const showTitle = tagText(channel, "title") || feedUrl;
  const episodes: Episode[] = [];
  const itemRe = /<item\b[^>]*>([\s\S]*?)<\/item>/gi;
  let match: RegExpExecArray | null;
  while ((match = itemRe.exec(channel))) {
    const block = match[1];
    const audio = enclosureUrl(block);
    if (!audio) continue;
    const title = tagText(block, "title") || audio;
    const guid = tagText(block, "guid") || tagText(block, "link") || audio;
    episodes.push({
      id: guid,
      showId: feedUrl,
      title,
      enclosureUrl: audio,
    });
  }
  return { showTitle, episodes };
}
