import { afterEach, describe, expect, it, vi } from "vitest";
import { findFeedsFileId, pullFeedsBlob, pushFeedsBlob } from "./driveAppData";

describe("Drive appdata", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("lists then downloads the feeds blob", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ files: [{ id: "file1" }] }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ files: [{ id: "file1" }] }),
      })
      .mockResolvedValueOnce({
        ok: true,
        arrayBuffer: async () => new Uint8Array([1, 2, 3]).buffer,
      });
    vi.stubGlobal("fetch", fetchMock);
    expect(await findFeedsFileId("tok")).toBe("file1");
    const blob = await pullFeedsBlob("tok");
    expect(Array.from(blob ?? [])).toEqual([1, 2, 3]);
  });

  it("refuses an empty push", async () => {
    await expect(pushFeedsBlob("tok", new Uint8Array())).rejects.toThrow("empty");
  });
});
