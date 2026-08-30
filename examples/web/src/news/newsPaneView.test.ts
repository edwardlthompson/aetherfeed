import type { Feed } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";
import { createArticleFlags } from "./articleFlags";
import { renderArticles, renderNewsShell, renderSourceTree } from "./newsPaneView";

const article = {
  id: "a1",
  feedId: "f1",
  title: "Headline one",
  url: "https://example.invalid/1",
  publishedAt: Date.now() - 5 * 3_600_000,
  summary: "<p>Snippet text from the feed</p>",
};

describe("news timeline rows", () => {
  it("steals ReadYou chrome: snippet, age, unread dot, star", () => {
    const flags = createArticleFlags(() => 100_000_000);
    flags.toggleStar(article.id);
    const html = renderArticles([article], null, flags, () => "Hacker News");
    expect(html).toContain("af-news-row");
    expect(html).toContain("af-news-row-dot");
    expect(html).toContain("af-news-row-star");
    expect(html).toContain("Snippet text from the feed");
    expect(html).toContain("5h");
    expect(html).toContain("Headline one");
    expect(html).not.toContain("Hacker News");
    const withThumb = renderArticles([article], null, flags, () => "", {
      [article.id]: "data:image/png;base64,xx",
    });
    expect(withThumb).toContain("data-news-thumb");
    const withCache = renderArticles([article], null, flags, () => "", {}, new Set([article.id]));
    expect(withCache).toContain("data-news-cached");
  });

  it("does not show feed url stubs in the snippet", () => {
    const flags = createArticleFlags();
    const html = renderArticles(
      [
        {
          ...article,
          summary:
            "<p>Article URL: https://ex.example/a</p><p>Comments URL: https://news.ycombinator.com/item?id=1</p>",
        },
      ],
      null,
      flags,
      () => "",
    );
    expect(html).not.toContain("https://");
    expect(html).not.toContain("Comments URL");
  });

  it("shows feed name only when the timeline mixes feeds", () => {
    const flags = createArticleFlags();
    const html = renderArticles(
      [article, { ...article, id: "a2", feedId: "f2", title: "Other" }],
      null,
      flags,
      (row) => (row.feedId === "f1" ? "Hacker News" : "NPR"),
    );
    expect(html).toContain("Hacker News");
    expect(html).toContain("NPR");
  });

  it("lists every folder and feed in the left column", () => {
    const groups: [string, Feed[]][] = [
      [
        "Automotive",
        [
          {
            id: "feed:cars",
            title: "Cars Daily",
            url: "https://example.invalid/cars.xml",
            kind: "news",
            updatedAt: 1,
          },
        ],
      ],
      [
        "World",
        [
          {
            id: "feed:world",
            title: "World Desk",
            url: "https://example.invalid/world.xml",
            kind: "news",
            updatedAt: 1,
          },
        ],
      ],
    ];
    const collapsed = renderSourceTree(groups, "Automotive", "feed:cars");
    expect(collapsed).toContain("data-news-folders");
    expect(collapsed).toContain("Automotive");
    expect(collapsed).toContain("World");
    expect(collapsed).toContain("af-news-folder-chevron");
    expect(collapsed).toContain('aria-expanded="false"');
    expect(collapsed).not.toContain("Cars Daily");
    const open = renderSourceTree(groups, "Automotive", "feed:cars", ["Automotive", "World"]);
    expect(open).toContain("Cars Daily");
    expect(open).toContain("World Desk");
    expect(open).toContain('aria-expanded="true"');
    const unread = renderSourceTree(
      groups,
      "Automotive",
      "feed:cars",
      [],
      { Automotive: 3 },
      { "feed:cars": 3 },
    );
    expect(unread).toContain("data-news-folder-unread");
    expect(unread).toContain(">3<");
    expect(unread).not.toContain("Cars Daily");
  });

  it("offers oldest-first sort in the timeline", () => {
    expect(renderNewsShell("", "", "", "")).toContain("data-news-sort");
    expect(renderNewsShell("", "", "", "")).toContain("data-news-sort-menu");
    expect(renderNewsShell("", "", "", "")).toContain("data-news-sources");
    expect(renderNewsShell("", "", "", "")).not.toContain("data-news-hide-sidebar");
    expect(
      renderNewsShell("", "", "", "", {
        source: 0.28,
        timeline: 0.34,
        reader: 0.38,
        oldestFirst: true,
      }),
    ).toContain("data-news-sort");
  });
});
