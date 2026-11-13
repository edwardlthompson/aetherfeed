import { afterEach, describe, expect, it, vi } from "vitest";
import { LAN_CLIENT_PASS_KEY, LAN_CLIENT_URL_KEY } from "./lanClientPrefs";
import { syncLanPoolOnStartup } from "./startupSync";

vi.mock("./lanPoolClient", () => ({
  syncLanPool: vi.fn(async () => ({})),
}));

describe("syncLanPoolOnStartup", () => {
  afterEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  it("skips when passphrase is missing", async () => {
    localStorage.setItem(LAN_CLIENT_URL_KEY, "http://10.0.0.7:8787");
    expect(await syncLanPoolOnStartup()).toBe(false);
  });

  it("pulls the pool when hub prefs exist", async () => {
    localStorage.setItem(LAN_CLIENT_URL_KEY, "http://10.0.0.7:8787");
    localStorage.setItem(LAN_CLIENT_PASS_KEY, "smoke-lan-pass");
    const { syncLanPool } = await import("./lanPoolClient");
    expect(await syncLanPoolOnStartup()).toBe(true);
    expect(syncLanPool).toHaveBeenCalledWith("http://10.0.0.7:8787", "smoke-lan-pass");
  });
});
