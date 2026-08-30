import type { Feed } from "@aetherfeed/domain";
import { t } from "../i18n";

export function newsFeedsOnly(feeds: Feed[]): Feed[] {
  return feeds.filter((feed) => feed.kind === "news");
}

export function defaultNewsFolder(): string {
  return t("readerimport.library.news");
}

export function groupNewsByFolder(feeds: Feed[]): [string, Feed[]][] {
  const buckets = new Map<string, Feed[]>();
  for (const feed of newsFeedsOnly(feeds)) {
    const heading = feed.folder?.trim() || defaultNewsFolder();
    const list = buckets.get(heading) ?? [];
    list.push(feed);
    buckets.set(heading, list);
  }
  return [...buckets.entries()].sort(([left], [right]) => left.localeCompare(right));
}
