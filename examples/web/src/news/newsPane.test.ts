import { afterEach, describe, expect, it, vi } from "vitest";
import { t } from "../i18n";
import { clearImportedFeeds, saveImportedFeeds } from "../readerimport/importedStore";
import { NEWS_CHROME_KEY } from "./chromePrefs";
import { createNewsPane } from "./newsPane";

describe("news pane", () => {
  afterEach(() => {
    clearImportedFeeds();
    localStorage.removeItem(NEWS_CHROME_KEY);
  });

  it("shows designed empty when the news library is empty", () => {
    clearImportedFeeds();
    const root = document.createElement("div");
    createNewsPane(root);
    expect(root.dataset.testid).toBe("news-pane");
    expect(root.querySelector("[data-news-empty]")?.textContent).toBe(t("news.empty"));
    expect(root.querySelector("[data-news-folders]")).toBeTruthy();
    expect(root.querySelector("[data-news-feeds]")).toBeTruthy();
  });

  it("groups imported news feeds by folder and hides podcasts", () => {
    saveImportedFeeds([
      {
        id: "feed:cars",
        title: "Cars Daily",
        url: "https://example.invalid/cars.xml",
        kind: "news",
        folder: "Automotive",
        updatedAt: 1,
      },
      {
        id: "feed:world",
        title: "World Desk",
        url: "https://example.invalid/world.xml",
        kind: "news",
        folder: "World",
        updatedAt: 1,
      },
      {
        id: "feed:show",
        title: "A Show",
        url: "https://libsyn.com/show.xml",
        kind: "podcast",
        folder: "Podcasts",
        updatedAt: 1,
      },
    ]);
    const root = document.createElement("div");
    createNewsPane(root);
    const folders = [...root.querySelectorAll("[data-folder]")].map((el) => el.textContent);
    expect(folders).toContain("Automotive");
    expect(folders).toContain("World");
    expect(folders).not.toContain("Podcasts");
    expect(root.textContent).not.toContain("Cars Daily");
    expect(root.textContent).not.toContain("A Show");
    root.querySelector<HTMLElement>("[data-folder='World']")?.click();
    expect(root.textContent).toContain("World Desk");
    expect(root.textContent).not.toContain("Cars Daily");
    expect(root.querySelector("[data-folder-toggle='World']")?.getAttribute("aria-expanded")).toBe(
      "true",
    );
    expect(root.querySelector("[data-news-sort]")).toBeTruthy();
    const again = document.createElement("div");
    createNewsPane(again);
    expect(again.textContent).toContain("World Desk");
    expect(again.querySelector("[data-folder='World']")?.getAttribute("aria-current")).toBe("true");
    expect(again.querySelector("[data-folder-toggle='World']")?.getAttribute("aria-expanded")).toBe(
      "true",
    );
    expect(again.querySelector("[data-news-hide-sidebar]")).toBeNull();
    again.querySelector<HTMLElement>("[data-folder-toggle='World']")?.click();
    expect(again.textContent).not.toContain("World Desk");
    expect(again.querySelector("[data-folder-toggle='World']")?.getAttribute("aria-expanded")).toBe(
      "false",
    );
    const third = document.createElement("div");
    createNewsPane(third);
    expect(third.textContent).not.toContain("World Desk");
    expect(third.querySelector("[data-folder='World']")?.getAttribute("aria-current")).toBe("true");
    expect(third.querySelector("[data-folder-toggle='World']")?.getAttribute("aria-expanded")).toBe(
      "false",
    );
  });

  it("auto-refreshes the first feed so headlines appear", async () => {
    saveImportedFeeds([
      {
        id: "feed:local",
        title: "Local",
        url: "https://example.invalid/rss.json",
        kind: "news",
        folder: "World",
        updatedAt: 1,
      },
    ]);
    vi.stubGlobal(
      "fetch",
      async () =>
        new Response(
          JSON.stringify({
            version: "https://jsonfeed.org/version/1.1",
            title: "Local",
            items: [{ id: "1", title: "Hello from feed", url: "https://example.invalid/1" }],
          }),
          { status: 200, headers: { "Content-Type": "application/json" } },
        ),
    );
    const root = document.createElement("div");
    createNewsPane(root);
    await vi.waitFor(() => {
      expect(root.textContent).toContain("Hello from feed");
    });
    vi.unstubAllGlobals();
  });
});
