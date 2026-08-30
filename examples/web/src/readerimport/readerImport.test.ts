import {
  classifyFeedKind,
  createReaderImportRepository,
  detectReaderVendor,
  type Feed,
  normalizeFeedUrl,
  parseReaderImport,
  type ReadState,
  type Star,
} from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";

const OPML = `<opml><body><outline title="Local" xmlUrl="https://example.invalid/rss.xml"/></body></opml>`;

describe("reader import API", () => {
  it("detects vendors and parses OPML", () => {
    expect(
      detectReaderVendor({ name: "takeout.zip", members: { "subscriptions.xml": OPML } }),
    ).toBe("google-reader");
    expect(detectReaderVendor({ name: "inoreader-backup.json", text: '{"feeds":[]}' })).toBe(
      "inoreader",
    );
    const parsed = parseReaderImport({ name: "feedly.opml", text: OPML });
    expect(parsed.vendor).toBe("opml");
    expect(parsed.outlines[0]?.xmlUrl).toBe("https://example.invalid/rss.xml");
  });

  it("rejects empty files and skips duplicate URLs", async () => {
    expect(parseReaderImport({ name: "empty.opml", text: "" }).errors[0]?.code).toBe("empty");
    const feeds: Feed[] = [
      { id: "x", title: "Old", url: "https://example.invalid/rss.xml", kind: "news", updatedAt: 1 },
    ];
    const library = {
      feeds: async () => feeds,
      upsertFeed: async (feed: Feed) => {
        feeds.push(feed);
      },
      upsertStar: async (_star: Star) => undefined,
      upsertReadState: async (_state: ReadState) => undefined,
    };
    const result = await createReaderImportRepository(library).apply(
      parseReaderImport({ name: "feedly.opml", text: OPML }),
      10,
    );
    expect(result.feedsSkipped).toBe(1);
    expect(result.feedsAdded).toBe(0);
  });

  it("normalizes URLs and sorts podcast folders", async () => {
    expect(normalizeFeedUrl("HTTP://WWW.Example.invalid/rss.xml/")).toBe(
      normalizeFeedUrl("https://example.invalid/rss.xml"),
    );
    expect(normalizeFeedUrl("feed/https://www.example.invalid:443/rss.xml")).toBe(
      "https://example.invalid/rss.xml",
    );
    expect(
      classifyFeedKind({
        title: "Cast",
        xmlUrl: "https://feeds.libsyn.com/9/rss",
        folder: "Podcasts",
      }),
    ).toBe("podcast");
    const feeds: Feed[] = [];
    const library = {
      feeds: async () => feeds,
      upsertFeed: async (feed: Feed) => {
        feeds.push(feed);
      },
      upsertStar: async (_star: Star) => undefined,
      upsertReadState: async (_state: ReadState) => undefined,
    };
    const result = await createReaderImportRepository(library).apply(
      parseReaderImport({
        name: "inoreader.opml",
        text: `<opml><body>
          <outline text="Podcasts"><outline title="Cast" xmlUrl="https://feeds.libsyn.com/9/rss"/></outline>
          <outline title="A" xmlUrl="https://www.example.invalid/rss.xml/"/>
          <outline title="B" xmlUrl="http://example.invalid/rss.xml"/>
        </body></opml>`,
      }),
      10,
    );
    expect(result.feedsAdded).toBe(2);
    expect(result.feedsSkipped).toBe(1);
    expect(result.podcastsAdded).toBe(1);
    expect(result.newsAdded).toBe(1);
    expect(feeds.find((feed) => feed.kind === "podcast")?.folder).toBe("Podcasts");
  });
});
