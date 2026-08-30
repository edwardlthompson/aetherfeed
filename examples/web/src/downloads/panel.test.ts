import { beforeEach, describe, expect, it } from "vitest";
import { downloadsCopy } from "./copy";
import { createDownloadsPanel } from "./panel";
import { createDownloadQueue } from "./queue";
import { getWifiOnly, WIFI_ONLY_KEY } from "./settings";

describe("createDownloadsPanel", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("renders designed empty copy and wifi settings", () => {
    const root = document.createElement("div");
    const pane = createDownloadsPanel(root);
    expect(root.contains(pane)).toBe(true);
    expect(pane.getAttribute("aria-label")).toBe(downloadsCopy.title);
    expect(pane.querySelector("[data-downloads-empty]")?.textContent).toBe(downloadsCopy.empty);
    expect(pane.querySelector("[data-wifi-only]")).toBeTruthy();
    expect(pane.querySelector("[data-auto-download]")).toBeTruthy();
  });

  it("writes the wifi-only flag when the checkbox is toggled", () => {
    const root = document.createElement("div");
    const pane = createDownloadsPanel(root);
    const box = pane.querySelector<HTMLInputElement>("[data-wifi-only]");
    expect(box).toBeTruthy();
    if (!box) return;
    box.checked = true;
    box.dispatchEvent(new Event("change", { bubbles: true }));
    expect(localStorage.getItem(WIFI_ONLY_KEY)).toBe("1");
    expect(getWifiOnly()).toBe(true);
  });

  it("lists queued episodes in enqueue order", () => {
    const queue = createDownloadQueue();
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    queue.enqueue("ep-b", "https://example.invalid/b.mp3");
    const root = document.createElement("div");
    const pane = createDownloadsPanel(root, { queue, isWifi: () => true });
    const ids = [...pane.querySelectorAll("[data-episode-id]")].map(
      (node) => (node as HTMLElement).dataset.episodeId,
    );
    expect(ids).toEqual(["ep-a", "ep-b"]);
  });
});
