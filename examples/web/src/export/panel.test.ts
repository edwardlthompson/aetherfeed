import type { Feed } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";
import { createExportPanel } from "./panel";

const sample: Feed = {
  id: "feed:https://example.invalid/rss.xml",
  title: "Local",
  url: "https://example.invalid/rss.xml",
  kind: "news",
  folder: "Automotive",
  updatedAt: 1,
};

describe("createExportPanel", () => {
  it("downloads OPML that contains xmlUrl", () => {
    let body = "";
    const root = document.createElement("div");
    createExportPanel(root, {
      load: () => [sample],
      save: () => undefined,
      now: () => 0,
      download: (_name, contents) => {
        body = contents;
      },
    });
    root.querySelector<HTMLButtonElement>("[data-export-download]")?.click();
    expect(body).toContain("xmlUrl");
    expect(body).toContain("https://example.invalid/rss.xml");
  });

  it("renames a folder and updates the store via injected save", () => {
    let saved: Feed[] = [];
    const root = document.createElement("div");
    createExportPanel(root, {
      load: () => [sample],
      save: (feeds) => {
        saved = feeds;
      },
    });
    const input = root.querySelector<HTMLInputElement>("[data-folder-rename]");
    expect(input?.dataset.folderFrom).toBe("Automotive");
    if (input) input.value = "Cars";
    root.querySelector<HTMLButtonElement>("[data-export-save]")?.click();
    expect(saved).toHaveLength(1);
    expect(saved[0]?.folder).toBe("Cars");
    expect(saved[0]?.url).toBe(sample.url);
  });
});
