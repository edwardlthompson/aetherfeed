import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { applyNewsDrawerOpen, bindNewsSourcesDrawer } from "./newsDrawer";
import { newsDrawerOffstage } from "./newsSwipe";

describe("news sources drawer", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "matchMedia",
      vi.fn().mockImplementation((query: string) => ({
        matches: query.includes("720"),
        media: query,
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      })),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("keeps the drawer open when a folder header is toggled", () => {
    const root = document.createElement("div");
    root.innerHTML = `
      <div class="af-news-columns is-sources-open">
        <aside class="af-news-sidebar">
          <nav data-news-folders data-news-feeds>
            <button type="button" class="af-news-folder" data-folder="World">World</button>
            <button type="button" class="af-news-feed" data-feed-id="feed:world">World Desk</button>
          </nav>
        </aside>
        <div class="af-news-scrim" data-news-scrim></div>
      </div>
    `;
    bindNewsSourcesDrawer(root);
    const host = root.querySelector(".af-news-columns");
    root.querySelector<HTMLElement>("[data-folder='World']")?.click();
    expect(host?.classList.contains("is-sources-open")).toBe(true);
    root.querySelector<HTMLElement>("[data-feed-id='feed:world']")?.click();
    expect(host?.classList.contains("is-sources-open")).toBe(false);
    const sidebar = root.querySelector<HTMLElement>(".af-news-sidebar");
    expect(sidebar?.hidden).toBe(true);
    expect(newsDrawerOffstage(false)).toBe(true);
  });

  it("hides the sidebar completely when the drawer is closed", () => {
    const host = document.createElement("div");
    host.className = "af-news-columns";
    host.innerHTML = `<aside class="af-news-sidebar"></aside><div data-news-scrim></div>`;
    applyNewsDrawerOpen(host, false);
    const sidebar = host.querySelector<HTMLElement>(".af-news-sidebar");
    expect(sidebar?.hidden).toBe(true);
    expect(sidebar?.getAttribute("aria-hidden")).toBe("true");
    applyNewsDrawerOpen(host, true);
    expect(sidebar?.hidden).toBe(false);
  });
});
