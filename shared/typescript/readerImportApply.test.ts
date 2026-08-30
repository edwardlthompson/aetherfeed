import { describe, expect, it } from "vitest";
import type { Feed, ReadState, Star } from "./models";
import { createReaderImportRepository, planReaderImport } from "./readerImportApply";
import { parseReaderImport } from "./readerImport";

const OPML = `<opml><body><outline title="Local" xmlUrl="https://example.invalid/rss.xml"/></body></opml>`;

function memoryLibrary(seed: Feed[] = []) {
  const feeds = [...seed];
  const stars: Star[] = [];
  const reads: ReadState[] = [];
  return {
    feeds: async () => feeds,
    upsertFeed: async (feed: Feed) => {
      feeds.push(feed);
    },
    upsertStar: async (star: Star) => {
      stars.push(star);
    },
    upsertReadState: async (state: ReadState) => {
      reads.push(state);
    },
    stars,
    reads,
  };
}

describe("reader import apply", () => {
  it("upserts feeds and skips duplicate URLs", async () => {
    const parsed = parseReaderImport({ name: "feedly.opml", text: OPML });
    const library = memoryLibrary([
      {
        id: "existing",
        title: "Old",
        url: "https://example.invalid/rss.xml",
        kind: "news",
        updatedAt: 1,
      },
    ]);
    const result = await createReaderImportRepository(library).apply(parsed, 10);
    expect(result.feedsAdded).toBe(0);
    expect(result.feedsSkipped).toBe(1);
    expect(library.feeds()).resolves.toHaveLength(1);
  });

  it("leaves the vault unchanged on hard parse failure", async () => {
    const parsed = parseReaderImport({ name: "bad.json", text: "{nope" });
    const library = memoryLibrary();
    const result = await createReaderImportRepository(library).apply(parsed, 10);
    expect(result.feedsAdded).toBe(0);
    expect(result.errors.some((e) => e.code === "malformed")).toBe(true);
    expect(await library.feeds()).toHaveLength(0);
  });

  it("skips the same feed when only www or slash differs", async () => {
    const parsed = parseReaderImport({
      name: "inoreader.opml",
      text: `<opml><body>
        <outline title="A" xmlUrl="https://www.example.invalid/rss.xml/"/>
        <outline title="B" xmlUrl="http://example.invalid/rss.xml"/>
      </body></opml>`,
    });
    const library = memoryLibrary();
    const result = await createReaderImportRepository(library).apply(parsed, 10);
    expect(result.feedsAdded).toBe(1);
    expect(result.feedsSkipped).toBe(1);
    expect(result.newsAdded).toBe(1);
  });

  it("sorts a Podcasts folder onto podcast kind", async () => {
    const parsed = parseReaderImport({
      name: "greader.opml",
      text: `<opml><body>
        <outline text="Podcasts">
          <outline title="Cast" xmlUrl="https://feeds.libsyn.com/9/rss"/>
        </outline>
      </body></opml>`,
    });
    const library = memoryLibrary();
    const result = await createReaderImportRepository(library).apply(parsed, 10);
    expect(result.podcastsAdded).toBe(1);
    expect((await library.feeds())[0]?.kind).toBe("podcast");
    expect((await library.feeds())[0]?.folder).toBe("Podcasts");
  });

  it("plans stars from Takeout extras", () => {
    const parsed = parseReaderImport({
      name: "starred.json",
      text: JSON.stringify({
        title: "Starred items",
        items: [{ title: "Saved", canonical: [{ href: "https://example.invalid/a" }] }],
      }),
    });
    const planned = planReaderImport(new Set(), parsed, 5);
    expect(planned.stars).toHaveLength(1);
    expect(planned.result.starsApplied).toBe(1);
  });
});
