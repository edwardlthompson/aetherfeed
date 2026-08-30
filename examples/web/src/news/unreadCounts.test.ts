import type { Article, Feed } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";
import { unreadBadge, unreadByFeed, unreadByFolder } from "./unreadCounts";

const article = (id: string, feedId: string): Article => ({
  id,
  feedId,
  title: id,
  url: `https://ex.invalid/${id}`,
});

describe("unread counts", () => {
  it("omits zero badges and sums folders", () => {
    const headlines = [article("a1", "f1"), article("a2", "f1"), article("b1", "f2")];
    const unread = (id: string) => id !== "a1";
    const byFeed = unreadByFeed(headlines, unread);
    expect(byFeed).toEqual({ f1: 1, f2: 1 });
    const groups: [string, Feed[]][] = [
      [
        "World",
        [{ id: "f1", title: "W", url: "https://ex.invalid/w", kind: "news", updatedAt: 1 }],
      ],
      ["Art", [{ id: "f2", title: "A", url: "https://ex.invalid/a", kind: "news", updatedAt: 1 }]],
    ];
    expect(unreadByFolder(byFeed, groups)).toEqual({ World: 1, Art: 1 });
    expect(unreadBadge(0, "folder")).toBe("");
    expect(unreadBadge(2, "feed")).toContain("data-news-feed-unread");
  });
});
