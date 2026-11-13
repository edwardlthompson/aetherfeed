import { afterEach, describe, expect, it } from "vitest";
import { emptyPoolDocument, saveLocalPool } from "../sync/lanPoolClient";
import { filterNews, folderKeyForUrl, newsSubscriptionTree } from "./wireFilters";

describe("subscription tree", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("nests feeds under the site host they come from", () => {
    const pool = emptyPoolDocument();
    pool.subs = [
      { url: "https://one.test/rss", title: "One RSS", kind: "news", updatedAt: 1 },
      { url: "https://one.test/atom", title: "One Atom", kind: "news", updatedAt: 1 },
      { url: "https://two.test/feed", title: "Two", kind: "news", updatedAt: 1 },
    ];
    saveLocalPool(pool);
    expect(folderKeyForUrl("https://www.one.test/rss")).toBe("one.test");
    const tree = newsSubscriptionTree(
      [
        {
          id: "a",
          feedId: "https://one.test/rss",
          title: "A",
          url: "https://one.test/a",
          publishedAt: 2,
        },
        {
          id: "b",
          feedId: "https://two.test/feed",
          title: "B",
          url: "https://two.test/b",
          publishedAt: 1,
        },
      ],
      false,
    );
    const one = tree.find((node) => node.id === "folder:one.test");
    expect(one?.children?.map((child) => child.label)).toEqual(["One Atom", "One RSS"]);
    expect(
      filterNews(
        [
          {
            id: "a",
            feedId: "https://one.test/rss",
            title: "A",
            url: "https://one.test/a",
            publishedAt: 2,
          },
          {
            id: "b",
            feedId: "https://two.test/feed",
            title: "B",
            url: "https://two.test/b",
            publishedAt: 1,
          },
        ],
        false,
        "folder:one.test",
        "newest",
      ).map((row) => row.id),
    ).toEqual(["a"]);
  });
});
