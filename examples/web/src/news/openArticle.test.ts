import { describe, expect, it, vi } from "vitest";

const mocks = vi.hoisted(() => ({
  painted: [] as string[],
  fetchFeedBody: vi.fn(async () => ({ ok: false, body: "" })),
  saveArticleCache: vi.fn(
    async (_id: string, _html: string, _images: Record<string, string>) => undefined,
  ),
  loadArticleCache: vi.fn(async (_id: string) => ({
    bodyHtml: "<p>Hi</p>",
    images: { "https://img.example/a.png": "data:image/png;base64,xx" },
  })),
}));

vi.mock("../lock", () => ({
  loadArticleCache: mocks.loadArticleCache,
  saveArticleCache: mocks.saveArticleCache,
}));

vi.mock("../reader", () => ({
  createReaderView: (_host: HTMLElement, html: string) => {
    mocks.painted.push(html);
    return {
      ready: Promise.resolve(),
      abort: () => undefined,
      articleId: "a1",
    };
  },
  extractReadable: (html: string) => ({ bodyHtml: html, title: "", imageSrcs: [] }),
}));

vi.mock("./fetchBody", () => ({
  fetchFeedBody: mocks.fetchFeedBody,
}));

import { paintReader, resolveArticleHtml, warmNeighbors } from "./openArticle";

describe("paintReader", () => {
  it("paints cached HTML immediately and never uses a link placeholder", async () => {
    mocks.painted.length = 0;
    mocks.loadArticleCache.mockResolvedValueOnce({
      bodyHtml: `<p>${"Cached body ".repeat(40)}</p>`,
      images: { "https://img.example/a.png": "data:image/png;base64,xx" },
    });
    const host = document.createElement("div");
    const hit = await paintReader(host, {
      id: "a1",
      feedId: "f1",
      title: "Hello",
      url: "https://example.invalid/wait",
      contentHtml: `<p><a href="https://example.invalid/wait">Read more</a></p>`,
    });
    expect(hit).toBe(true);
    expect(mocks.painted[0]).toContain("Cached body");
    expect(mocks.painted[0].length).toBeGreaterThan(400);
    expect(mocks.painted[0]).not.toContain("https://example.invalid/wait");
  });

  it("keeps cached images instead of saving an empty map", async () => {
    const host = document.createElement("div");
    await paintReader(host, {
      id: "a1",
      feedId: "f1",
      title: "Hello",
      url: "https://example.invalid/1",
      contentHtml: `<p>${"word ".repeat(80)}</p>`,
    });
    expect(mocks.saveArticleCache).toHaveBeenCalled();
    const last = mocks.saveArticleCache.mock.calls.at(-1);
    expect(last?.[2]).toMatchObject({
      "https://img.example/a.png": expect.stringContaining("data:image/png"),
    });
  });

  it("skips network when usable cache exists", async () => {
    mocks.fetchFeedBody.mockClear();
    mocks.loadArticleCache.mockResolvedValueOnce({
      bodyHtml: `<p>${"Cached body ".repeat(40)}</p><p>Next page</p>`,
      images: {},
    });
    const html = await resolveArticleHtml({
      id: "a1",
      feedId: "f1",
      title: "Hello",
      url: "https://motoiq.example/nerd/",
    });
    expect(html).toContain("Cached body");
    expect(mocks.fetchFeedBody).not.toHaveBeenCalled();
  });

  it("warms the previous story from cache", async () => {
    mocks.saveArticleCache.mockClear();
    mocks.loadArticleCache.mockResolvedValue({
      bodyHtml: `<p>${"Cached body ".repeat(40)}</p>`,
      images: {},
    });
    warmNeighbors(
      [
        { id: "a1", feedId: "f1", title: "Old", url: "https://example.invalid/1", publishedAt: 10 },
        { id: "a2", feedId: "f1", title: "New", url: "https://example.invalid/2", publishedAt: 20 },
      ],
      "a2",
    );
    await vi.waitFor(() => {
      expect(mocks.saveArticleCache).toHaveBeenCalledWith(
        "a1",
        expect.stringContaining("Cached body"),
        {},
      );
    });
  });
});
