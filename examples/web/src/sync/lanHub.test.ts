import { describe, expect, it, vi } from "vitest";
import { lanHubStart, lanHubStatus, lanHubStop } from "./lanHub";
import type { TauriInvoke } from "./tauriInvoke";

describe("lanHub", () => {
  it("delegates status/start/stop to invoke", async () => {
    const status = {
      running: true,
      port: 8787,
      etag: "1",
      authSaltB64: "abc",
      lanUrls: ["http://127.0.0.1:8787"],
    };
    const invoke = vi.fn(async (cmd: string) => {
      if (cmd === "lan_hub_status" || cmd === "lan_hub_start" || cmd === "lan_hub_stop") {
        return status;
      }
      throw new Error(cmd);
    });
    const tauri = invoke as unknown as TauriInvoke;
    await expect(lanHubStatus(tauri)).resolves.toEqual(status);
    await expect(lanHubStart("secret", 8787, tauri)).resolves.toEqual(status);
    await expect(lanHubStop(tauri)).resolves.toEqual(status);
    expect(invoke).toHaveBeenCalledWith("lan_hub_start", { passphrase: "secret", port: 8787 });
  });

  it("rejects empty passphrase", async () => {
    await expect(lanHubStart("  ", undefined, vi.fn() as unknown as TauriInvoke)).rejects.toThrow(
      /passphrase/,
    );
  });
});
