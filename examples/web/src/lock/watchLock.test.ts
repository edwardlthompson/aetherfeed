import { afterEach, describe, expect, it, vi } from "vitest";
import { webAppLock } from "./session";
import { watchBackgroundLock } from "./watchLock";

afterEach(async () => {
  await webAppLock.wipe();
  vi.restoreAllMocks();
});

describe("watchBackgroundLock", () => {
  it("remounts lock after a blur timeout without visibilitychange", async () => {
    await webAppLock.setSecret("123456", "pin");
    let remounts = 0;
    const stop = watchBackgroundLock(() => {
      remounts += 1;
    });
    const t0 = 2_000_000;
    const now = vi.spyOn(Date, "now").mockReturnValue(t0);
    window.dispatchEvent(new Event("blur"));
    now.mockReturnValue(t0 + 130_000);
    window.dispatchEvent(new Event("focus"));
    expect(webAppLock.state()).toBe("locked");
    expect(remounts).toBeGreaterThan(0);
    stop();
  });

  it("remounts the lock form on the first click after lock()", async () => {
    await webAppLock.setSecret("123456", "pin");
    webAppLock.lock();
    let remounts = 0;
    const stop = watchBackgroundLock(() => {
      remounts += 1;
    });
    document.dispatchEvent(new MouseEvent("pointerdown", { bubbles: true }));
    expect(remounts).toBeGreaterThan(0);
    stop();
  });
});
