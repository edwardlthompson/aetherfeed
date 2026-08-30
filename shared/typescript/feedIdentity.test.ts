import { describe, expect, it } from "vitest";
import { classifyFeedKind, normalizeFeedUrl } from "./feedIdentity";

describe("feed identity", () => {
  it("normalizes scheme, www, slash, and feed/ prefix to one key", () => {
    const a = normalizeFeedUrl("HTTP://WWW.Example.invalid/rss.xml/");
    const b = normalizeFeedUrl("https://example.invalid/rss.xml");
    const c = normalizeFeedUrl("feed/https://www.example.invalid/rss.xml");
    expect(a).toBe(b);
    expect(b).toBe(c);
  });

  it("classifies podcast folder and libsyn host", () => {
    expect(
      classifyFeedKind({
        title: "Show",
        xmlUrl: "https://feeds.libsyn.com/123/rss",
        folder: "Podcasts",
      }),
    ).toBe("podcast");
    expect(classifyFeedKind({ title: "Local", xmlUrl: "https://example.invalid/rss.xml" })).toBe("news");
    expect(
      classifyFeedKind({
        title: "How I Built This",
        xmlUrl: "https://rss.art19.com/how-i-built-this",
      }),
    ).toBe("podcast");
  });
});
