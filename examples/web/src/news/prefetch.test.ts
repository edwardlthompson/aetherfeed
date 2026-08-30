import { beforeEach, describe, expect, it, vi } from "vitest";

const store = new Map<string, { bodyHtml: string; images: Record<string, string> }>();
const thumbs = new Map<string, string>();

vi.mock("../lock", () => ({
  loadArticleCache: async (id: string) => store.get(id) ?? null,
  saveArticleCache: async (id: string, bodyHtml: string, images: Record<string, string>) => {
    store.set(id, { bodyHtml, images });
    const thumb = Object.values(images).find((blob) => blob.startsWith("data:"));
    if (thumb) thumbs.set(id, thumb);
  },
  loadThumbs: async (ids: string[]) => {
    const out: Record<string, string> = {};
    for (const id of ids) {
      const thumb = thumbs.get(id);
      if (thumb) out[id] = thumb;
    }
    return out;
  },
  saveThumbs: async (map: Record<string, string>) => {
    for (const [id, thumb] of Object.entries(map)) thumbs.set(id, thumb);
  },
}));

vi.mock("./openArticle", () => ({
  resolveArticleHtml: vi.fn(
    async () =>
      `<article><p>${"word ".repeat(80)}</p><img src="https://cdn.example/hero.jpg"></article>`,
  ),
}));

vi.mock("./newsNetwork", () => ({
  canFetchNews: () => true,
}));

vi.mock("../reader", () => ({
  extractReadable: (html: string) => ({
    title: "",
    bodyHtml: html,
    imageSrcs: html.includes("hero.jpg") ? ["https://cdn.example/hero.jpg"] : [],
  }),
  fetchImageBlobs: vi.fn(async (srcs: string[]) =>
    Object.fromEntries(srcs.map((src) => [src, "data:image/jpeg;base64,abc"])),
  ),
}));

import { fetchImageBlobs } from "../reader";
import { resolveArticleHtml } from "./openArticle";
import { prefetchUnread, renderCacheProgress, thumbsFor } from "./prefetch";

describe("prefetch unread", () => {
  beforeEach(() => {
    store.clear();
    thumbs.clear();
    vi.mocked(resolveArticleHtml).mockClear();
    vi.mocked(fetchImageBlobs).mockClear();
  });

  it("caches unread articles in order and reports determinate progress", async () => {
    const seen: number[] = [];
    await prefetchUnread(
      [
        { id: "a1", feedId: "f1", title: "One", url: "https://example.invalid/1" },
        { id: "a2", feedId: "f1", title: "Two", url: "https://example.invalid/2" },
      ],
      () => true,
      (progress) => seen.push(progress.done),
    );
    expect(seen[0]).toBe(0);
    expect(seen.at(-1)).toBe(2);
    expect(store.get("a1")?.images["https://cdn.example/hero.jpg"]).toContain("data:image");
    const thumbs = await thumbsFor(["a1"]);
    expect(thumbs.a1).toContain("data:image");
  });

  it("does not refetch a stored cache record", async () => {
    store.set("a1", { bodyHtml: "short", images: {} });
    await prefetchUnread(
      [{ id: "a1", feedId: "f1", title: "One", url: "https://example.invalid/1" }],
      () => true,
      () => undefined,
    );
    expect(vi.mocked(resolveArticleHtml)).not.toHaveBeenCalled();
    expect(store.get("a1")?.bodyHtml).toBe("short");
  });

  it("remembers a failed extract so the next load skips the network", async () => {
    vi.mocked(resolveArticleHtml).mockResolvedValueOnce("");
    const article = { id: "a1", feedId: "f1", title: "One", url: "https://example.invalid/1" };
    await prefetchUnread(
      [article],
      () => true,
      () => undefined,
    );
    expect(store.has("a1")).toBe(true);
    vi.mocked(resolveArticleHtml).mockClear();
    await prefetchUnread(
      [article],
      () => true,
      () => undefined,
    );
    expect(resolveArticleHtml).not.toHaveBeenCalled();
  });

  it("thumbsFor returns stored bytes without fetching images", async () => {
    thumbs.set("a1", "data:image/jpeg;base64,abc");
    const next = await thumbsFor(["a1"]);
    expect(next.a1).toBe("data:image/jpeg;base64,abc");
    expect(resolveArticleHtml).not.toHaveBeenCalled();
    expect(fetchImageBlobs).not.toHaveBeenCalled();
  });

  it("thumbsFor omits a missing thumb without a network call", async () => {
    const next = await thumbsFor(["missing"]);
    expect(next.missing).toBeUndefined();
    expect(Object.keys(next)).toHaveLength(0);
    expect(resolveArticleHtml).not.toHaveBeenCalled();
    expect(fetchImageBlobs).not.toHaveBeenCalled();
  });

  it("hides the cache bar when the queue is finished", () => {
    expect(renderCacheProgress(2, 2, "done")).toBe("");
    expect(renderCacheProgress(1, 2, "Caching unread 1/2")).toContain("data-news-cache-progress");
    expect(renderCacheProgress(1, 2, "Caching unread 1/2")).toContain("width:50%");
  });
});
