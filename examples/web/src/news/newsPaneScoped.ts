import type { Feed } from "@aetherfeed/domain";
import { groupNewsByFolder } from "./folders";

export function scopedIds(feeds: Feed[], feedId: string | null, folder: string | null): string[] {
  if (feedId) return [feedId];
  if (!folder) return feeds.map((feed) => feed.id);
  return (
    groupNewsByFolder(feeds)
      .find(([name]) => name === folder)?.[1]
      .map((feed) => feed.id) ?? []
  );
}
