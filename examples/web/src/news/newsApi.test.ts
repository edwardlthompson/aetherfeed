import { flattenOpml, isFeedFormat } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";

describe("news API lock", () => {
  it("accepts rss, atom, and jsonfeed only", () => {
    expect(isFeedFormat("rss")).toBe(true);
    expect(isFeedFormat("atom")).toBe(true);
    expect(isFeedFormat("jsonfeed")).toBe(true);
    expect(isFeedFormat("rdf")).toBe(false);
  });

  it("flattens OPML outlines that declare xmlUrl", () => {
    expect(
      flattenOpml([{ title: "Local", xmlUrl: "https://example.invalid/feed.xml" }]),
    ).toHaveLength(1);
  });
});
