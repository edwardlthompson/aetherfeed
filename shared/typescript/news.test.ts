import { describe, expect, it } from "vitest";
import { flattenOpml, isFeedFormat } from "./news";

describe("news API lock", () => {
  it("accepts the three on-the-wire feed formats", () => {
    expect(isFeedFormat("rss")).toBe(true);
    expect(isFeedFormat("atom")).toBe(true);
    expect(isFeedFormat("jsonfeed")).toBe(true);
    expect(isFeedFormat("rdf")).toBe(false);
  });

  it("flattens nested OPML outlines that have xmlUrl", () => {
    const flat = flattenOpml([
      {
        title: "News",
        children: [
          { title: "Local", xmlUrl: "https://example.invalid/rss.xml" },
          { title: "Empty folder", children: [] },
        ],
      },
    ]);
    expect(flat).toHaveLength(1);
    expect(flat[0]?.xmlUrl).toBe("https://example.invalid/rss.xml");
  });
});
