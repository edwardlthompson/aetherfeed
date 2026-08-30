/** Merge and serialize subscription lists for Drive appdata sync. */

import { normalizeFeedUrl } from "./feedIdentity";
import type { Feed, ModuleKind } from "./models";

export const DRIVE_FEEDS_NAME = "aetherfeed-feeds.enc";
export const DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata";
export const DRIVE_LOOPBACK_PORT = 17890;

export type FeedSyncDocument = {
  version: 1;
  updatedAt: number;
  feeds: Feed[];
};

export function canonicalFeedId(url: string): string {
  return `feed:${normalizeFeedUrl(url)}`;
}

export function canonicalizeFeed(feed: Feed): Feed {
  return { ...feed, id: canonicalFeedId(feed.url) };
}

export function mergeFeedSources(local: Feed[], remote: Feed[]): Feed[] {
  const byKey = new Map<string, Feed>();
  for (const feed of [...local, ...remote].map(canonicalizeFeed)) {
    const key = normalizeFeedUrl(feed.url);
    const prev = byKey.get(key);
    if (!prev || feed.updatedAt >= prev.updatedAt) byKey.set(key, feed);
  }
  return [...byKey.values()];
}

export function encodeFeedDocument(feeds: Feed[], now: number): string {
  const doc: FeedSyncDocument = {
    version: 1,
    updatedAt: now,
    feeds: feeds.map(canonicalizeFeed),
  };
  return JSON.stringify(doc);
}

export function decodeFeedDocument(json: string): FeedSyncDocument {
  const parsed = JSON.parse(json) as FeedSyncDocument;
  if (parsed.version !== 1 || !Array.isArray(parsed.feeds)) {
    throw new Error("invalid feed sync document");
  }
  return {
    version: 1,
    updatedAt: Number(parsed.updatedAt) || 0,
    feeds: parsed.feeds.map((feed) =>
      canonicalizeFeed({
        id: String(feed.id ?? ""),
        title: String(feed.title ?? ""),
        url: String(feed.url ?? ""),
        kind: (feed.kind as ModuleKind) || "news",
        siteUrl: feed.siteUrl,
        updatedAt: Number(feed.updatedAt) || 0,
      }),
    ),
  };
}
