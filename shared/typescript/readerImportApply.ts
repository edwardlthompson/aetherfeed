/** Vault apply for a parsed reader import. */

import { classifyFeedKind, normalizeFeedUrl } from "./feedIdentity";
import type { Feed, ReadState, Star } from "./models";
import { flattenOpml } from "./news";
import {
  isHardParseFailure,
  type ReaderImportError,
  type ReaderImportParsed,
  type ReaderImportResult,
} from "./readerImport";

export type LibraryWriter = {
  feeds(): Promise<Feed[]>;
  upsertFeed(feed: Feed): Promise<void>;
  upsertStar(star: Star): Promise<void>;
  upsertReadState(state: ReadState): Promise<void>;
};

export type ReaderImportRepository = {
  apply(parsed: ReaderImportParsed, now?: number): Promise<ReaderImportResult>;
};

export function feedIdFromUrl(url: string): string {
  return `feed:${normalizeFeedUrl(url)}`;
}

function emptyResult(
  vendor: ReaderImportParsed["vendor"],
  errors: ReaderImportError[],
): ReaderImportResult {
  return {
    vendor,
    feedsAdded: 0,
    feedsSkipped: 0,
    starsApplied: 0,
    newsAdded: 0,
    podcastsAdded: 0,
    errors,
  };
}

export function planReaderImport(
  existingUrls: Set<string>,
  parsed: ReaderImportParsed,
  now: number,
): { feeds: Feed[]; stars: Star[]; reads: ReadState[]; result: ReaderImportResult } {
  const errors: ReaderImportError[] = [...parsed.errors];
  if (isHardParseFailure(parsed)) {
    return {
      feeds: [],
      stars: [],
      reads: [],
      result: emptyResult(
        parsed.vendor,
        errors.length ? errors : [{ code: "malformed", message: "No subscriptions found" }],
      ),
    };
  }
  const seen = new Set(existingUrls);
  const feeds: Feed[] = [];
  let skipped = 0;
  for (const outline of flattenOpml(parsed.outlines)) {
    const url = outline.xmlUrl?.trim();
    if (!url) {
      errors.push({ code: "skipped", message: "Outline missing xmlUrl", outline: outline.title });
      continue;
    }
    const key = normalizeFeedUrl(url);
    if (seen.has(key)) {
      skipped += 1;
      continue;
    }
    seen.add(key);
    const kind = classifyFeedKind(outline);
    feeds.push({
      id: feedIdFromUrl(url),
      title: outline.title || url,
      url,
      kind,
      siteUrl: outline.htmlUrl,
      folder: outline.folder,
      updatedAt: now,
    });
  }
  const stars: Star[] = parsed.stars.map((star) => ({
    targetId: star.url,
    module: "news",
    createdAt: now,
  }));
  const reads: ReadState[] = parsed.reads.map((read) => ({
    targetId: read.url,
    module: "news",
    status: read.status,
    updatedAt: now,
  }));
  return {
    feeds,
    stars,
    reads,
    result: {
      vendor: parsed.vendor,
      feedsAdded: feeds.length,
      feedsSkipped: skipped,
      starsApplied: stars.length,
      newsAdded: feeds.filter((feed) => feed.kind === "news").length,
      podcastsAdded: feeds.filter((feed) => feed.kind === "podcast").length,
      errors,
    },
  };
}

export function createReaderImportRepository(library: LibraryWriter): ReaderImportRepository {
  return {
    async apply(parsed, now = Date.now()) {
      const existing = new Set((await library.feeds()).map((feed) => normalizeFeedUrl(feed.url)));
      const planned = planReaderImport(existing, parsed, now);
      for (const feed of planned.feeds) await library.upsertFeed(feed);
      for (const star of planned.stars) await library.upsertStar(star);
      for (const read of planned.reads) await library.upsertReadState(read);
      return planned.result;
    },
  };
}
