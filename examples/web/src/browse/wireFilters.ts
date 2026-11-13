import type { Article, Episode } from "@aetherfeed/domain";
import { applyBrowseFilter, type SortOrder, sortByDate } from "./contentFilter";
import {
  feedIds,
  newsStarsAndReads,
  podcastStarsAndPlayed,
  STARRED_NODE,
} from "./wireFilterHelpers";

export { folderKeyForUrl, STARRED_NODE } from "./wireFilterHelpers";
export { newsSubscriptionTree, podcastSubscriptionTree, subscriptionTree } from "./wireFilterTrees";

export function filterNews(
  articles: Article[],
  unreadOnly: boolean,
  nodeId: string,
  sort: SortOrder,
): Article[] {
  const { reads, stars } = newsStarsAndReads();
  const allowed = feedIds("news", nodeId);
  return sortByDate(
    applyBrowseFilter(articles, {
      unreadOnly,
      starredOnly: nodeId === STARRED_NODE,
      inCategory: (row) => !allowed || allowed.has(row.feedId),
      isUnread: (row) => reads.get(row.url) !== true && reads.get(row.id) !== true,
      isStarred: (row) => stars.has(row.id) || stars.has(row.url),
    }),
    sort,
    (row) => row.publishedAt || 0,
  );
}

export function filterEpisodes(
  episodes: Episode[],
  unreadOnly: boolean,
  nodeId: string,
  sort: SortOrder,
): Episode[] {
  const { stars, played } = podcastStarsAndPlayed();
  const allowed = feedIds("podcast", nodeId);
  return sortByDate(
    applyBrowseFilter(episodes, {
      unreadOnly,
      starredOnly: nodeId === STARRED_NODE,
      inCategory: (row) => !allowed || allowed.has(row.showId),
      isUnread: (row) => !played.has(row.id),
      isStarred: (row) => stars.has(row.id) || stars.has(row.enclosureUrl),
    }),
    sort,
    (row) => row.publishedAt || 0,
  );
}
