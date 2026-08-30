import { feedsToOpml, libraryExport } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";

describe("library export API", () => {
  it("maps feeds to OPML outlines", () => {
    const outlines = feedsToOpml([
      {
        id: "1",
        title: "Local",
        url: "https://example.invalid/rss.xml",
        kind: "news",
        updatedAt: 1,
      },
    ]);
    expect(outlines[0]?.xmlUrl).toBe("https://example.invalid/rss.xml");
    expect(libraryExport([], 10).version).toBe(1);
  });
});
