import {
  decodeFeedDocument,
  encodeFeedDocument,
  type Feed,
  mergeFeedSources,
} from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";

const news = (url: string, updatedAt: number, title = "A"): Feed => ({
  id: "x",
  title,
  url,
  kind: "news",
  updatedAt,
});

describe("feed source merge", () => {
  it("dedupes normalized URLs and keeps the newer title", () => {
    const merged = mergeFeedSources(
      [news("HTTP://WWW.Example.invalid/rss.xml/", 1, "Old")],
      [news("https://example.invalid/rss.xml", 9, "New")],
    );
    expect(merged).toHaveLength(1);
    expect(merged[0]?.title).toBe("New");
    expect(merged[0]?.id).toBe("feed:https://example.invalid/rss.xml");
  });

  it("round-trips the document", () => {
    const feeds = [news("https://example.invalid/a.xml", 2)];
    const doc = decodeFeedDocument(encodeFeedDocument(feeds, 10));
    expect(doc.version).toBe(1);
    expect(doc.feeds[0]?.url).toBe("https://example.invalid/a.xml");
  });
});
