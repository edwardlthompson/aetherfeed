import type { Feed } from "./models";
import type { OpmlOutline } from "./news";

export type LibraryExport = {
  version: 1;
  exportedAt: number;
  feeds: Feed[];
};

export function feedsToOpml(feeds: Feed[]): OpmlOutline[] {
  return feeds.map((feed) => ({
    title: feed.title,
    xmlUrl: feed.url,
    htmlUrl: feed.siteUrl,
  }));
}

export function libraryExport(feeds: Feed[], now: number): LibraryExport {
  return { version: 1, exportedAt: now, feeds };
}
