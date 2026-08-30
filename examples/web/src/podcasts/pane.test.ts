import { afterEach, describe, expect, it, vi } from "vitest";
import { clearImportedFeeds, saveImportedFeeds } from "../readerimport/importedStore";
import { createPodcastsPane } from "./pane";
import { clearPositions } from "./positionStore";

function seedPodcast() {
  saveImportedFeeds([
    {
      id: "feed:https://example.invalid/news.xml",
      title: "Daily News",
      url: "https://example.invalid/news.xml",
      kind: "news",
      updatedAt: 1,
    },
    {
      id: "feed:https://example.invalid/show.xml",
      title: "Night Show",
      url: "https://example.invalid/pilot.mp3",
      kind: "podcast",
      updatedAt: 2,
    },
  ]);
}

describe("createPodcastsPane", () => {
  afterEach(() => {
    clearImportedFeeds();
    clearPositions();
  });

  it("renders a designed empty state when no podcast shows exist", () => {
    saveImportedFeeds([
      {
        id: "feed:news",
        title: "Only News",
        url: "https://example.invalid/rss.xml",
        kind: "news",
        updatedAt: 1,
      },
    ]);
    const root = document.createElement("div");
    const pane = createPodcastsPane(root);
    expect(root.contains(pane)).toBe(true);
    expect(pane.querySelector("[data-podcasts-empty]")?.textContent).toMatch(/No imported shows/);
    expect(pane.querySelector("[data-podcasts-shows]")).toBeNull();
    expect(pane.querySelector("[data-mini-player]")).toBeNull();
  });

  it("lists podcast shows and exposes play/seek/speed mini-player", () => {
    seedPodcast();
    const root = document.createElement("div");
    const pane = createPodcastsPane(root);
    expect(pane.textContent).toContain("Night Show");
    expect(pane.textContent).not.toContain("Daily News");
    expect(pane.querySelector("[data-play-pause]")).toBeTruthy();
    expect(pane.querySelector("[data-seek]")).toBeTruthy();
    expect(pane.querySelector("[data-speed]")).toBeTruthy();
    expect(pane.querySelector("[data-mini-player]")).toBeTruthy();
  });

  it("shows a typed enclosure error with retry and does not throw", async () => {
    seedPodcast();
    const audio = document.createElement("audio");
    audio.play = vi.fn(async () => {
      throw new DOMException("not supported", "NotSupportedError");
    });
    const root = document.createElement("div");
    const pane = createPodcastsPane(root, { audio });
    expect(() => {
      pane.querySelector<HTMLButtonElement>("[data-podcasts-show]")?.click();
    }).not.toThrow();
    await vi.waitFor(() => {
      const box = pane.querySelector<HTMLElement>("[data-player-error]");
      expect(box?.hidden).toBe(false);
      expect(box?.textContent).toMatch(/Enclosure error/);
      expect(pane.querySelector("[data-retry]")).toBeTruthy();
    });
  });
});
