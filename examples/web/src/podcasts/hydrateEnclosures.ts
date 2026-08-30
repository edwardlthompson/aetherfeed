import { fetchFeedBody } from "../news/fetchBody";
import { loadImportedFeeds } from "../readerimport/importedStore";
import { enclosureFromRss, rememberEnclosure } from "./enclosures";

export async function hydrateEnclosures(fetchImpl?: typeof fetch): Promise<number> {
  const feeds = loadImportedFeeds().filter((feed) => feed.kind === "podcast");
  let count = 0;
  for (const feed of feeds) {
    const result = await fetchFeedBody(feed.url, { fetch: fetchImpl, timeoutMs: 8_000 });
    if (!result.ok) continue;
    const url = enclosureFromRss(result.body);
    if (!url) continue;
    rememberEnclosure(feed.id, url);
    count += 1;
  }
  return count;
}

export function isAudioEnclosure(url: string): boolean {
  return /\.(mp3|m4a|ogg|opus|aac|flac)(\?|$)/i.test(url.trim());
}
