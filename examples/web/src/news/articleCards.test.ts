import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createArticleCards } from "./articleCards";

describe("createArticleCards", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("renders a vertical list of article rows", () => {
    root.append(
      createArticleCards([
        { id: "a1", feedId: "f1", title: "Hello", url: "https://ex.test/a1", summary: "Body" },
      ]),
    );
    expect(root.querySelector(".af-news-list")).not.toBeNull();
    expect(root.querySelector("[data-testid='news-article-a1']")?.textContent).toContain("Hello");
  });
});
