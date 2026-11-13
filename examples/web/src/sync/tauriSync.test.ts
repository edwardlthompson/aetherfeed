import { describe, expect, it } from "vitest";
import { tauriSyncPull, tauriSyncPush } from "./tauriSync";

describe("tauriSync", () => {
  it("delegates pull/push to injected invoke", async () => {
    const store = new Map<string, number[]>();
    const endpoint = "memory://local";
    const invoke = async <T>(cmd: string, args?: Record<string, unknown>): Promise<T> => {
      if (cmd === "sync_push") {
        const blob = args?.blob as number[];
        store.set(endpoint, blob);
        return undefined as T;
      }
      if (cmd === "sync_pull") {
        return (store.get(endpoint) ?? null) as T;
      }
      throw new Error(`unexpected ${cmd}`);
    };
    await tauriSyncPush(endpoint, new Uint8Array([9, 8]), invoke);
    const pulled = await tauriSyncPull(endpoint, invoke);
    expect(pulled).toEqual(new Uint8Array([9, 8]));
  });
});
