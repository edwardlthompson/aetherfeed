import { afterEach, describe, expect, it } from "vitest";
import { clearImportedFeeds, saveImportedFeeds } from "../readerimport/importedStore";
import { rememberedEnclosure } from "./enclosures";
import { hydrateEnclosures, isAudioEnclosure } from "./hydrateEnclosures";

afterEach(() => {
  clearImportedFeeds();
});

describe("hydrateEnclosures", () => {
  it("remembers a real enclosure url", async () => {
    saveImportedFeeds([
      {
        id: "show-1",
        title: "Show",
        url: "https://ex.example/feed.xml",
        kind: "podcast",
        updatedAt: 1,
      },
    ]);
    const rss = `<rss><channel><item><enclosure url="https://cdn.example/ep.mp3" /></item></channel></rss>`;
    await hydrateEnclosures(async () => new Response(rss, { status: 200 }));
    expect(rememberedEnclosure("show-1")).toBe("https://cdn.example/ep.mp3");
    expect(isAudioEnclosure("https://cdn.example/ep.mp3")).toBe(true);
    expect(isAudioEnclosure("https://ex.example/feed.xml")).toBe(false);
  });
});
