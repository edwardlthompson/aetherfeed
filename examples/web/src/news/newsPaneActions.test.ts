import { describe, expect, it } from "vitest";
import { MemoryNewsRepository } from "./memoryRepo";
import { headlinesFromAll, pullArticles, visibleArticles } from "./newsPaneActions";

describe("news pane actions", () => {
  it("keeps articles when no exclude rules match", () => {
    expect(
      visibleArticles([
        { id: "a1", feedId: "f1", title: "Headline", url: "https://example.invalid/1" },
      ]),
    ).toHaveLength(1);
  });

  it("collects headlines from every feed", async () => {
    const repo = new MemoryNewsRepository({
      feeds: [
        { id: "f1", title: "One", url: "https://a.example/rss.xml", kind: "news", updatedAt: 1 },
        { id: "f2", title: "Two", url: "https://b.example/rss.xml", kind: "news", updatedAt: 1 },
      ],
    });
    repo.hydrate([
      { id: "a1", feedId: "f1", title: "A", url: "https://a.example/1" },
      { id: "a2", feedId: "f2", title: "B", url: "https://b.example/2" },
    ]);
    const rows = await headlinesFromAll(repo, [{ id: "f1" }, { id: "f2" }], false);
    expect(rows.map((row) => row.id).sort()).toEqual(["a1", "a2"]);
  });

  it("tells the user when a feed returns 404", async () => {
    const repo = new MemoryNewsRepository({
      feeds: [
        {
          id: "f1",
          title: "Dead",
          url: "https://gone.example/rss.xml",
          kind: "news",
          updatedAt: 1,
        },
      ],
      fetch: async () => new Response("", { status: 404 }),
    });
    const next = await pullArticles(repo, "f1", true);
    expect(next.articles).toEqual([]);
    expect(next.status).toContain("Dead");
    expect(next.status).toContain("no longer available");
  });
});
