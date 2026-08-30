import { describe, expect, it } from "vitest";
import { modeUnreadBadge, paintNavUnread } from "./navUnread";

describe("nav unread badges", () => {
  it("hides a zero count and paints later totals", () => {
    expect(modeUnreadBadge("news")).toContain('data-nav-unread="news"');
    expect(modeUnreadBadge("news")).toContain(" hidden");
    const root = document.createElement("div");
    root.innerHTML = `<button data-mode="news">${modeUnreadBadge("news")}</button>`;
    paintNavUnread(root, { news: 3, podcast: 0, booru: 0 });
    const badge = root.querySelector<HTMLElement>("[data-nav-unread='news']");
    expect(badge?.textContent).toBe("3");
    expect(badge?.hidden).toBe(false);
  });
});
