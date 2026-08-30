import { afterEach, describe, expect, it, vi } from "vitest";
import { createReaderImportPanel, resetImportedFeeds } from "./ImportPanel";

describe("reader import panel", () => {
  afterEach(() => {
    resetImportedFeeds();
  });

  it("renders a labeled file picker", () => {
    const panel = createReaderImportPanel();
    const input = panel.querySelector<HTMLInputElement>("[data-readerimport-file]");
    expect(panel.getAttribute("aria-label")).toBe("Import subscriptions");
    expect(input?.getAttribute("type")).toBe("file");
    expect(input?.getAttribute("accept")).toContain(".opml");
    expect(panel.querySelector("[data-feed-empty]")?.textContent).toContain("No subscriptions");
  });

  it("applies a picked OPML file and announces counts", async () => {
    const panel = createReaderImportPanel();
    const input = panel.querySelector<HTMLInputElement>("[data-readerimport-file]");
    const result = panel.querySelector("[data-readerimport-result]");
    expect(input && result).toBeTruthy();
    const file = new File(
      [
        `<opml><body><outline title="Local" xmlUrl="https://example.invalid/rss.xml"/></body></opml>`,
      ],
      "feedly.opml",
      { type: "text/xml" },
    );
    Object.defineProperty(input, "files", {
      configurable: true,
      value: { 0: file, length: 1, item: (i: number) => (i === 0 ? file : null) },
    });
    input?.dispatchEvent(new Event("change"));
    await vi.waitFor(() => {
      expect(result?.textContent).toContain("Added 1");
    });
  });
});
