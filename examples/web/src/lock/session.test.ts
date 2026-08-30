import { afterEach, describe, expect, it, vi } from "vitest";
import { loadArticleCache, loadThumbs, saveArticleCache, saveThumb } from "./cache";
import { openUtf8, wrapUtf8 } from "./crypto";
import { subscribeLockChange, touchUnlock, WRAP_KEY, webAppLock } from "./session";

afterEach(async () => {
  vi.useRealTimers();
  await webAppLock.wipe();
});

describe("webAppLock", () => {
  it("rejects a weak PIN and unlocks with a valid one", async () => {
    await expect(webAppLock.setSecret("12345", "pin")).rejects.toThrow();
    await webAppLock.setSecret("123456", "pin");
    expect(webAppLock.state()).toBe("unlocked");
    webAppLock.lock();
    expect(webAppLock.state()).toBe("locked");
    expect(await webAppLock.unlock("000000")).toBe(false);
    expect(await webAppLock.unlock("123456")).toBe(true);
  });

  it("round-trips article cache as ciphertext", async () => {
    await webAppLock.setSecret("123456", "pin");
    await saveArticleCache("a1", "<p>Hello</p>", {
      "https://img.example/a.png": "data:image/png;base64,xx",
    });
    const raw = localStorage.getItem("af-lock-art:a1") ?? "";
    expect(raw.includes("<p>Hello</p>")).toBe(false);
    const row = await loadArticleCache("a1");
    expect(row?.bodyHtml).toBe("<p>Hello</p>");
    const thumbs = await loadThumbs(["a1", "missing"]);
    expect(thumbs.a1).toContain("data:image");
    expect(thumbs.missing).toBeUndefined();
  });

  it("loads a stored thumb without reading the article body", async () => {
    await webAppLock.setSecret("123456", "pin");
    await saveThumb("a9", "data:image/jpeg;base64,only");
    const next = await loadThumbs(["a9"]);
    expect(next.a9).toBe("data:image/jpeg;base64,only");
    expect(await loadArticleCache("a9")).toBeNull();
  });

  it("wraps utf8 so plaintext is not on disk", async () => {
    const sealed = await wrapUtf8('{"feeds":[]}', "12345678");
    expect(sealed.includes("feeds")).toBe(false);
    expect(await openUtf8(sealed, "12345678")).toBe('{"feeds":[]}');
    expect(WRAP_KEY).toBe("af-lock-wrap");
  });

  it("notifies listeners when the idle timer expires", async () => {
    await webAppLock.setSecret("123456", "pin");
    vi.useFakeTimers();
    touchUnlock();
    let sawLock = 0;
    const stop = subscribeLockChange(() => {
      sawLock += 1;
    });
    await vi.advanceTimersByTimeAsync(120_000);
    expect(webAppLock.state()).toBe("locked");
    expect(sawLock).toBeGreaterThan(0);
    stop();
    vi.useRealTimers();
  });
});
