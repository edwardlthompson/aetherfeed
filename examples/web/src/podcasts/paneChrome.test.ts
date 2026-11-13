import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { createPodcastsChrome } from "./paneChrome";

describe("createPodcastsChrome", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("lists synced episodes", () => {
    createPodcastsChrome(root, {
      shows: [{ url: "https://ex.test/pod.xml", title: "Show" }],
      episodes: [
        {
          id: "e1",
          showId: "https://ex.test/pod.xml",
          title: "Lan Ep",
          enclosureUrl: "https://cdn.ex/e1.mp3",
        },
      ],
      status: "ready",
    });
    expect(root.querySelector("[data-testid='podcasts-episode-list']")?.textContent).toContain(
      "Lan Ep",
    );
    expect(root.querySelector("audio")?.getAttribute("src")).toBe("https://cdn.ex/e1.mp3");
  });
});
