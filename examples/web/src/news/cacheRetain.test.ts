import { describe, expect, it } from "vitest";
import { CACHE_RETAIN_MS, idsToDrop, parseCacheRetainMode, shouldDropCache } from "./cacheRetain";

describe("cache retain", () => {
  const now = 1_700_000_000_000;

  it("keeps starred and fresh bodies", () => {
    expect(shouldDropCache(now - CACHE_RETAIN_MS - 1, true, "days30", now, true)).toBe(false);
    expect(shouldDropCache(now - 1_000, false, "days30", now, true)).toBe(false);
    expect(shouldDropCache(now, false, "sync", now, false)).toBe(false);
  });

  it("drops expired or next-sync unstarred", () => {
    expect(shouldDropCache(now - CACHE_RETAIN_MS, false, "days30", now, false)).toBe(true);
    expect(shouldDropCache(now, false, "sync", now, true)).toBe(true);
    expect(parseCacheRetainMode("sync")).toBe("sync");
    expect(shouldDropCache(null, false, "days30", now, true)).toBe(false);
  });

  it("skips starred blobs and drops expired unstarred", () => {
    const gone = idsToDrop(
      [
        { id: "old", cachedAt: now - CACHE_RETAIN_MS },
        { id: "star", cachedAt: now - CACHE_RETAIN_MS },
        { id: "fresh", cachedAt: now },
        { id: "", cachedAt: now - CACHE_RETAIN_MS },
      ],
      new Set(["star"]),
      "days30",
      now,
      false,
    );
    expect(gone).toEqual(["old"]);
  });
});
