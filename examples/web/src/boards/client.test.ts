import { afterEach, describe, expect, it, vi } from "vitest";
import { FetchBoardClient } from "./client";
import { BoardFetchError, type BoardSource } from "./types";

const source: BoardSource = {
  id: "demo",
  kind: "danbooru",
  baseUrl: "https://board.example",
  label: "Demo",
};

function abortError(): Error {
  const err = new Error("Aborted");
  err.name = "AbortError";
  return err;
}

afterEach(() => {
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("FetchBoardClient blacklist", () => {
  it("honors blacklist case-insensitively", () => {
    const client = new FetchBoardClient({ blacklist: ["spam"] });
    expect(client.isBlacklisted("SPAM")).toBe(true);
    expect(client.isBlacklisted("safe")).toBe(false);
  });

  it("drops posts that carry a blacklisted tag", async () => {
    const fetchImpl = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [
        { id: 1, file_url: "https://x/a.jpg", tag_string: "sky spam" },
        { id: 2, file_url: "https://x/b.jpg", tag_string: "sky" },
      ],
    });
    const client = new FetchBoardClient({ blacklist: ["spam"], fetchImpl });
    const posts = await client.search(source, { tags: ["sky"], page: 1 });
    expect(posts.map((post) => post.remoteId)).toEqual(["2"]);
  });
});

describe("FetchBoardClient abort and retry", () => {
  it("maps an aborted fetch to a typed non-retryable error", async () => {
    const ctrl = new AbortController();
    const fetchImpl = vi.fn((_url: string, init?: RequestInit) => {
      return new Promise<Response>((_resolve, reject) => {
        const fail = () => reject(abortError());
        if (init?.signal?.aborted) {
          fail();
          return;
        }
        init?.signal?.addEventListener("abort", fail);
      });
    });
    const client = new FetchBoardClient({ fetchImpl, timeoutMs: 50_000, retries: 0 });
    const pending = client.search(source, { tags: ["cat"], page: 1 }, ctrl.signal);
    ctrl.abort();
    await expect(pending).rejects.toMatchObject({
      name: "BoardFetchError",
      code: "aborted",
      retryable: false,
    });
    await expect(pending).rejects.toBeInstanceOf(BoardFetchError);
  });

  it("retries once after a network failure", async () => {
    const fetchImpl = vi
      .fn()
      .mockRejectedValueOnce(new TypeError("Failed to fetch"))
      .mockResolvedValueOnce({
        ok: true,
        json: async () => [{ id: 9, file_url: "https://x/c.jpg", tag_string: "cloud" }],
      });
    const client = new FetchBoardClient({ fetchImpl, retries: 1 });
    const posts = await client.search(source, { tags: ["cloud"], page: 1 });
    expect(posts).toHaveLength(1);
    expect(fetchImpl).toHaveBeenCalledTimes(2);
  });

  it("times out into a typed retryable error", async () => {
    vi.useFakeTimers();
    const fetchImpl = vi.fn((_url: string, init?: RequestInit) => {
      return new Promise<Response>((_resolve, reject) => {
        init?.signal?.addEventListener("abort", () => reject(abortError()));
      });
    });
    const client = new FetchBoardClient({ fetchImpl, timeoutMs: 10, retries: 0 });
    const pending = client.search(source, { tags: ["cat"], page: 1 });
    const expectReject = expect(pending).rejects.toMatchObject({
      name: "BoardFetchError",
      code: "timeout",
      retryable: true,
    });
    await vi.advanceTimersByTimeAsync(20);
    await expectReject;
  });
});
