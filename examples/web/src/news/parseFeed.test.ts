import { parseJsonFeed, sniffFeedFormat } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";

describe("feed parse", () => {
  it("sniffs JSON Feed and maps items", () => {
    const body = JSON.stringify({
      version: "https://jsonfeed.org/version/1.1",
      title: "Local",
      home_page_url: "https://example.invalid",
      items: [{ id: "1", title: "Hello", url: "https://example.invalid/1" }],
    });
    expect(sniffFeedFormat(body)).toBe("jsonfeed");
    const feed = parseJsonFeed(body);
    expect(feed.title).toBe("Local");
    expect(feed.items).toHaveLength(1);
    expect(feed.items[0]?.title).toBe("Hello");
  });
});
