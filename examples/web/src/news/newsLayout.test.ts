import { beforeEach, describe, expect, it } from "vitest";
import { loadNewsChrome, NEWS_CHROME_DEFAULT, NEWS_CHROME_KEY } from "./chromePrefs";
import { bindNewsLayout } from "./newsLayout";

describe("news layout chrome", () => {
  beforeEach(() => {
    localStorage.removeItem(NEWS_CHROME_KEY);
  });

  it("keeps three columns while dragging", () => {
    expect(NEWS_CHROME_DEFAULT.oldestFirst).toBe(false);
  });

  it("commits pane weights on pointerup without dropping expanded folders", () => {
    const root = document.createElement("div");
    root.innerHTML = `<div class="af-news-columns" style="width:1000px">
      <div data-split="source"></div>
    </div>`;
    const host = root.querySelector<HTMLElement>(".af-news-columns");
    if (host) {
      Object.defineProperty(host, "getBoundingClientRect", {
        value: () =>
          ({
            width: 1000,
            height: 100,
            top: 0,
            left: 0,
            bottom: 100,
            right: 1000,
            x: 0,
            y: 0,
            toJSON: () => "",
          }) as DOMRect,
      });
    }
    const start = { ...NEWS_CHROME_DEFAULT, expanded: ["World"] };
    let next = start;
    bindNewsLayout(root, start, (chrome) => {
      next = chrome;
    });
    const handle = root.querySelector<HTMLElement>("[data-split='source']");
    handle?.dispatchEvent(new PointerEvent("pointerdown", { clientX: 280, bubbles: true }));
    window.dispatchEvent(new PointerEvent("pointermove", { clientX: 200 }));
    window.dispatchEvent(new PointerEvent("pointerup", { clientX: 200 }));
    expect(next.expanded).toEqual(["World"]);
    expect(next.source + next.timeline + next.reader).toBeCloseTo(1);
    expect(loadNewsChrome().expanded).toEqual(["World"]);
  });
});
