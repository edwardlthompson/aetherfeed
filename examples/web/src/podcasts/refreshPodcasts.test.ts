import { afterEach, describe, expect, it } from "vitest";
import { emptyPoolDocument, saveLocalPool } from "../sync/lanPoolClient";
import { refreshPodcastEpisodes } from "./refreshPodcasts";

describe("refreshPodcastEpisodes", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("ingests episodes from subscribed podcast feeds", async () => {
    const pool = emptyPoolDocument();
    pool.subs = [{ url: "https://ex.test/pod.xml", title: "Show", kind: "podcast", updatedAt: 3 }];
    saveLocalPool(pool);
    const episodes = await refreshPodcastEpisodes(async () => {
      return `<rss><channel><title>Show</title>
        <item><title>Lan Ep</title><guid>le1</guid>
        <enclosure url="https://cdn.ex/le1.mp3" type="audio/mpeg"/></item>
      </channel></rss>`;
    });
    expect(episodes.some((e) => e.title === "Lan Ep")).toBe(true);
  });
});
