import { describe, expect, it, vi } from "vitest";
import { isPrivateRss, searchDirectory } from "./search";

describe("podcast directory", () => {
  it("returns empty for a blank query", async () => {
    expect(await searchDirectory("  ")).toEqual([]);
  });

  it("maps Apple-style hits and flags private RSS", async () => {
    const fetchImpl = vi.fn(
      async () =>
        new Response(
          JSON.stringify({
            results: [{ collectionName: "Show", feedUrl: "https://x.invalid/rss" }],
          }),
        ),
    ) as unknown as typeof fetch;
    const hits = await searchDirectory("show", fetchImpl);
    expect(hits[0]?.feedUrl).toBe("https://x.invalid/rss");
    expect(isPrivateRss("https://u:p@host.invalid/feed")).toBe(true);
  });
});
