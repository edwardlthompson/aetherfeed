import { afterEach, describe, expect, it } from "vitest";
import { filterNews } from "../browse/wireFilters";
import {
  emptyPoolDocument,
  isArticleStarred,
  loadLocalPool,
  markArticleRead,
  markArticleUnread,
  saveLocalPool,
  setArticleStarred,
} from "../sync/lanPoolClient";

describe("markArticleRead", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("removes the article from the unread filter and parks it back", () => {
    saveLocalPool(emptyPoolDocument());
    const articles = [
      {
        id: "a1",
        feedId: "https://ex.test/rss",
        title: "One",
        url: "https://ex.test/a1",
        publishedAt: 2,
      },
      {
        id: "a2",
        feedId: "https://ex.test/rss",
        title: "Two",
        url: "https://ex.test/a2",
        publishedAt: 1,
      },
    ];
    expect(filterNews(articles, true, "", "newest").map((row) => row.id)).toEqual(["a1", "a2"]);
    markArticleRead(articles[0]);
    expect(loadLocalPool().reads.some((row) => row.url === "https://ex.test/a1" && row.read)).toBe(
      true,
    );
    expect(filterNews(articles, true, "", "newest").map((row) => row.id)).toEqual(["a2"]);
    markArticleUnread(articles[0]);
    expect(filterNews(articles, true, "", "newest").map((row) => row.id)).toEqual(["a1", "a2"]);
  });

  it("toggles star state in the pool", () => {
    saveLocalPool(emptyPoolDocument());
    const article = { id: "a1", url: "https://ex.test/a1" };
    setArticleStarred(article, true);
    expect(isArticleStarred(article)).toBe(true);
    setArticleStarred(article, false);
    expect(isArticleStarred(article)).toBe(false);
  });
});
