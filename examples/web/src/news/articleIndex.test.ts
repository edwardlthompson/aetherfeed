import { describe, expect, it } from "vitest";
import { parseArticleIndex } from "./articleIndex";

describe("article index", () => {
  it("keeps valid headline rows and drops junk", () => {
    const rows = parseArticleIndex([
      {
        id: "feed-1:1",
        feedId: "feed-1",
        title: "Hello",
        url: "https://example.invalid/1",
        publishedAt: 10,
        summary: "Sum",
      },
      { id: "", feedId: "feed-1", title: "Bad", url: "https://example.invalid/x" },
      "nope",
    ]);
    expect(rows).toEqual([
      {
        id: "feed-1:1",
        feedId: "feed-1",
        title: "Hello",
        url: "https://example.invalid/1",
        publishedAt: 10,
        summary: "Sum",
        contentHtml: undefined,
      },
    ]);
    expect(parseArticleIndex(null)).toEqual([]);
  });
});
