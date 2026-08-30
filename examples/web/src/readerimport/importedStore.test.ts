import { afterEach, describe, expect, it, vi } from "vitest";
import { FEEDS_KEY, webAppLock } from "../lock";
import { clearImportedFeeds, loadImportedFeeds, saveImportedFeeds } from "./importedStore";

afterEach(async () => {
  clearImportedFeeds();
  await webAppLock.wipe();
});

describe("imported store lock", () => {
  it("fails closed while locked and encrypts after unlock", async () => {
    await webAppLock.setSecret("123456", "pin");
    saveImportedFeeds([
      { id: "f1", title: "T", url: "https://ex.example/rss.xml", kind: "news", updatedAt: 1 },
    ]);
    await vi.waitFor(() => {
      expect(localStorage.getItem(FEEDS_KEY)).toBeTruthy();
    });
    const raw = localStorage.getItem(FEEDS_KEY) ?? "";
    expect(raw.includes("https://ex.example/rss.xml")).toBe(false);
    webAppLock.lock();
    expect(loadImportedFeeds()).toEqual([]);
    expect(await webAppLock.unlock("123456")).toBe(true);
    expect(loadImportedFeeds()[0]?.id).toBe("f1");
  });
});
