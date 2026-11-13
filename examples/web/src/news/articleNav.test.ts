import { describe, expect, it } from "vitest";
import { neighborArticle, stepArticleIndex } from "./articleNav";

const articles = [
  { id: "a", feedId: "f", title: "A", url: "https://ex.test/a" },
  { id: "b", feedId: "f", title: "B", url: "https://ex.test/b" },
  { id: "c", feedId: "f", title: "C", url: "https://ex.test/c" },
];

describe("articleNav", () => {
  it("steps within the list", () => {
    expect(stepArticleIndex(1, 3, -1)).toBe(0);
    expect(stepArticleIndex(1, 3, 1)).toBe(2);
    expect(stepArticleIndex(0, 3, -1)).toBeNull();
    expect(neighborArticle(articles, "b", 1)?.id).toBe("c");
    expect(neighborArticle(articles, "a", -1)).toBeNull();
  });
});
