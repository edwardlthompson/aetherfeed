import { afterEach, describe, expect, it, vi } from "vitest";
import { readerCopy } from "./copy";
import { clearVault, loadArticle } from "./vault";
import { createReaderView } from "./view";

afterEach(() => {
  clearVault();
  vi.unstubAllGlobals();
});

describe("createReaderView", () => {
  it("shows designed empty for empty html", () => {
    const root = document.createElement("div");
    createReaderView(root, "");
    expect(root.dataset.testid).toBe("reader-view");
    expect(root.querySelector("[data-reader-empty]")?.textContent).toBe(readerCopy.empty);
  });

  it("renders stripped article and persists body plus image blobs", async () => {
    const fetchImpl = (async () =>
      ({
        ok: true,
        blob: async () => new Blob([new Uint8Array([1, 2, 3])], { type: "image/png" }),
      }) as Response) as typeof fetch;
    const html = `<html><body><nav>Skip</nav><article><h1>Vaulted</h1><p>Hi</p><script>bad()</script><img src="https://cdn.example/a.png"></article></body></html>`;
    const root = document.createElement("div");
    const handle = createReaderView(root, html, { articleId: "art-1", fetchImpl });
    await handle.ready;
    expect(root.textContent).toContain("Vaulted");
    expect(root.textContent).toContain("Hi");
    expect(root.textContent).not.toContain("Skip");
    expect(root.innerHTML.toLowerCase()).not.toContain("script");
    const stored = loadArticle("art-1");
    expect(stored?.bodyHtml).toContain("Hi");
    expect(stored?.images["https://cdn.example/a.png"]).toMatch(/^data:image\/png;base64,/);
    expect(root.querySelector("img")?.getAttribute("src")).toMatch(/^data:image\/png;base64,/);
  });
});
