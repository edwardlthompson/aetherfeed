import type { Episode } from "@aetherfeed/domain";
import { loadOfflineLibrary, saveOfflineLibrary } from "../domain/library";
import { fetchFeedText } from "../news/refreshNews";
import { loadLocalPool } from "../sync/lanPoolClient";
import { parsePodcastFeed } from "./parsePodcast";

const FEED_LIMIT = 12;
const EPISODE_LIMIT = 80;

export async function refreshPodcastEpisodes(
  getText: (url: string) => Promise<string> = fetchFeedText,
): Promise<Episode[]> {
  const pool = loadLocalPool();
  const feeds = [...pool.subs]
    .filter((s) => s.kind.toLowerCase() === "podcast")
    .sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0))
    .slice(0, FEED_LIMIT);
  const incoming: Episode[] = (
    await Promise.all(
      feeds.map(async (feed) => {
        try {
          const xml = await getText(feed.url);
          return parsePodcastFeed(xml, feed.url).episodes;
        } catch {
          return [] as Episode[];
        }
      }),
    )
  ).flat();
  const prior = loadOfflineLibrary();
  const byId = new Map<string, Episode>();
  for (const row of [...prior.episodes, ...incoming]) {
    byId.set(row.id, row);
  }
  const episodes = [...byId.values()].slice(0, EPISODE_LIMIT);
  saveOfflineLibrary({ ...prior, episodes });
  return episodes;
}

export function podcastShowsFromPool(): Array<{ url: string; title: string }> {
  return loadLocalPool()
    .subs.filter((s) => s.kind.toLowerCase() === "podcast")
    .map((s) => ({ url: s.url, title: s.title || s.url }));
}
