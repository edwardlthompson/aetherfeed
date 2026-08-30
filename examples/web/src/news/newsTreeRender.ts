import type { Article, Feed } from "@aetherfeed/domain";
import { groupNewsByFolder } from "./folders";
import { renderSourceTree } from "./newsPaneView";
import { unreadByFeed, unreadByFolder } from "./unreadCounts";

export function renderNewsTree(
  feeds: Feed[],
  selectedFolder: string | null,
  selectedFeedId: string | null,
  expanded: readonly string[],
  headlines: Article[],
  unread: (id: string) => boolean,
): string {
  const groups = groupNewsByFolder(feeds);
  const byFeed = unreadByFeed(headlines, unread);
  return renderSourceTree(
    groups,
    selectedFolder,
    selectedFeedId,
    expanded,
    unreadByFolder(byFeed, groups),
    byFeed,
  );
}
