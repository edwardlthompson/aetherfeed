import { describe, expect, it } from "vitest";
import {
  aroundArticles,
  neighborArticle,
  prefetchAfter,
  readerPlaceholder,
  sortArticles,
} from "./articleNav";

const older = {
  id: "a1",
  feedId: "f1",
  title: "Old",
  url: "https://example.invalid/1",
  publishedAt: 10,
};
const newer = {
  id: "a2",
  feedId: "f1",
  title: "New",
  url: "https://example.invalid/2",
  publishedAt: 20,
};

describe("article nav", () => {
  it("sorts oldest and newest", () => {
    expect(sortArticles([newer, older], true).map((row) => row.id)).toEqual(["a1", "a2"]);
    expect(sortArticles([newer, older], false).map((row) => row.id)).toEqual(["a2", "a1"]);
  });

  it("walks neighbors and priority queue", () => {
    const rows = sortArticles([newer, older], true);
    expect(neighborArticle(rows, "a1", 1)?.id).toBe("a2");
    expect(neighborArticle(rows, "a1", -1)).toBeUndefined();
    expect(aroundArticles(rows, "a1").map((row) => row.id)).toEqual(["a1", "a2"]);
    expect(prefetchAfter(rows, "a1", (id) => id !== "a1").map((row) => row.id)).toEqual([
      "a1",
      "a2",
    ]);
  });

  it("never uses a link as the loading placeholder", () => {
    expect(readerPlaceholder()).not.toContain("href");
    expect(readerPlaceholder()).not.toContain("http");
  });
});
