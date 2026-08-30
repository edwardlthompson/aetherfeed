import type { Episode, PodcastShow } from "@aetherfeed/domain";
import { loadImportedFeeds } from "../readerimport/importedStore";
import { rememberedEnclosure } from "./enclosures";

function podcastFeeds() {
  return loadImportedFeeds().filter((feed) => feed.kind === "podcast");
}

export function loadPodcastShows(): PodcastShow[] {
  return podcastFeeds().map((feed) => ({
    id: feed.id,
    feedId: feed.id,
    title: feed.title.trim() || "Untitled show",
  }));
}

export function loadPodcastEpisodes(): Episode[] {
  return podcastFeeds().map((feed) => ({
    id: feed.id,
    showId: feed.id,
    title: feed.title.trim() || "Untitled show",
    enclosureUrl: rememberedEnclosure(feed.id) || feed.url.trim(),
  }));
}
