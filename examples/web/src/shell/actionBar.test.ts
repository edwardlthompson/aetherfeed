import { describe, expect, it } from "vitest";
import { actionBarHtml, bindActionBar } from "./actionBar";

describe("action bar", () => {
  it("exposes news prev/next ids", () => {
    expect(actionBarHtml("news")).toContain("data-action-bar");
    expect(actionBarHtml("news")).toContain("data-action-prev");
    expect(actionBarHtml("news")).toContain("data-action-next");
    expect(actionBarHtml("news")).toContain("data-action-star");
    expect(actionBarHtml("news")).toContain("data-action-unread");
  });

  it("dispatches news next from the bar", () => {
    const root = document.createElement("div");
    root.innerHTML = actionBarHtml("news");
    const seen: string[] = [];
    window.addEventListener("af-news-next", () => seen.push("next"));
    bindActionBar(root);
    root.querySelector<HTMLButtonElement>("[data-action-next]")?.click();
    expect(seen).toEqual(["next"]);
  });
});
