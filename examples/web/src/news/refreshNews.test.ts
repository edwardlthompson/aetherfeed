import { describe, expect, it } from "vitest";
import { emptyPoolDocument, saveLocalPool } from "../sync/lanPoolClient";
import { refreshNewsArticles } from "./refreshNews";

describe("refreshNewsArticles", () => {
  it("ingests parsed items from subscribed news feeds", async () => {
    const pool = emptyPoolDocument();
    pool.subs = [{ url: "https://ex.test/rss", title: "Ex", kind: "news", updatedAt: 9 }];
    saveLocalPool(pool);
    const articles = await refreshNewsArticles(async () => {
      return `<rss><channel><item><title>Desk Story</title>
        <link>https://ex.test/story</link><guid>g1</guid></item></channel></rss>`;
    });
    expect(articles.some((a) => a.title === "Desk Story")).toBe(true);
  });
});
