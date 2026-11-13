import { afterEach, describe, expect, it } from "vitest";
import {
  emptyPoolDocument,
  loadLocalPool,
  markArticleRead,
  mergePoolDocuments,
  saveLocalPool,
} from "./lanPoolClient";

describe("lanPoolClient merge", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("LWW prefers higher updatedAt for subs", () => {
    const local = emptyPoolDocument();
    local.subs = [{ url: "https://a.example/rss/", title: "Local", kind: "news", updatedAt: 5 }];
    const remote = emptyPoolDocument();
    remote.subs = [{ url: "https://a.example/rss", title: "Remote", kind: "news", updatedAt: 9 }];
    const merged = mergePoolDocuments(local, remote);
    expect(merged.subs).toHaveLength(1);
    expect(merged.subs[0].title).toBe("Remote");
    expect(merged.subs[0].url).toBe("https://a.example/rss");
  });

  it("stores read receipts for article url and id", () => {
    saveLocalPool(emptyPoolDocument());
    markArticleRead({ id: "guid-1", url: "https://ex.test/story/" }, 42);
    const reads = loadLocalPool().reads;
    expect(reads).toEqual(
      expect.arrayContaining([
        { url: "https://ex.test/story", read: true, updatedAt: 42 },
        { url: "guid-1", read: true, updatedAt: 42 },
      ]),
    );
  });
});
