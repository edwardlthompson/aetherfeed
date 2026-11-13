import { beforeEach, describe, expect, it } from "vitest";
import {
  cacheArticle,
  getPlayback,
  loadOfflineLibrary,
  readCachedBody,
  saveOfflineLibrary,
  upsertPlayback,
  upsertStar,
} from "./library";

describe("offline library", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("reads cached items with the radio off and persists star + playback", () => {
    let state = cacheArticle(
      loadOfflineLibrary(),
      { id: "a1", feedId: "f1", title: "Cached", url: "https://example.test/a1" },
      "body offline",
    );
    state = upsertStar(state, "a1", "news", 100);
    state = upsertPlayback(state, {
      episodeId: "ep1",
      positionMs: 45000,
      durationMs: 120000,
      updatedAt: 101,
    });
    saveOfflineLibrary(state);

    expect(readCachedBody(state, "a1", false)).toBe("body offline");
    expect(readCachedBody(state, "missing", false)).toBeNull();
    expect(readCachedBody(state, "missing", true, "from net")).toBe("from net");
    expect(readCachedBody(state, "missing", true)).toBeNull();
    expect(readCachedBody(state, "missing", true, null)).toBeNull();

    const reloaded = loadOfflineLibrary();
    expect(readCachedBody(reloaded, "a1", false)).toBe("body offline");
    expect(reloaded.stars).toEqual([{ targetId: "a1", module: "news", createdAt: 100 }]);
    expect(getPlayback(reloaded, "ep1")?.positionMs).toBe(45000);
    expect(getPlayback(reloaded, "missing")).toBeUndefined();
  });

  it("recovers from corrupt or partial storage", () => {
    localStorage.setItem("af-offline-library", "{not-json");
    expect(loadOfflineLibrary().articles).toEqual([]);

    localStorage.setItem(
      "af-offline-library",
      JSON.stringify({ articles: "x", bodies: null, stars: null, playback: null }),
    );
    const partial = loadOfflineLibrary();
    expect(partial.articles).toEqual([]);
    expect(partial.episodes).toEqual([]);
    expect(partial.bodies).toEqual({});
    expect(partial.stars).toEqual([]);
    expect(partial.playback).toEqual([]);
  });
});
