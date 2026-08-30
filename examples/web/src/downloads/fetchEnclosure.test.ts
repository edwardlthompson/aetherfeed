import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchEnclosure } from "./fetchEnclosure";
import { DownloadError } from "./types";

function abortError(): Error {
  const err = new Error("Aborted");
  err.name = "AbortError";
  return err;
}

afterEach(() => {
  vi.useRealTimers();
});

describe("fetchEnclosure abort and timeout", () => {
  it("maps an aborted fetch to a typed aborted error", async () => {
    const ctrl = new AbortController();
    const fetchImpl: typeof fetch = vi.fn((_url: RequestInfo | URL, init?: RequestInit) => {
      return new Promise<Response>((_resolve, reject) => {
        const fail = () => reject(abortError());
        if (init?.signal?.aborted) {
          fail();
          return;
        }
        init?.signal?.addEventListener("abort", fail);
      });
    });
    const pending = fetchEnclosure("https://example.invalid/ep.mp3", {
      fetch: fetchImpl,
      timeoutMs: 50_000,
      signal: ctrl.signal,
    });
    ctrl.abort();
    const result = await pending;
    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.error).toBeInstanceOf(DownloadError);
    expect(result.error.kind).toBe("aborted");
  });

  it("maps a timeout abort to a typed timeout error", async () => {
    vi.useFakeTimers();
    const fetchImpl: typeof fetch = vi.fn((_url: RequestInfo | URL, init?: RequestInit) => {
      return new Promise<Response>((_resolve, reject) => {
        init?.signal?.addEventListener("abort", () => reject(abortError()));
      });
    });
    const pending = fetchEnclosure("https://example.invalid/ep.mp3", {
      fetch: fetchImpl,
      timeoutMs: 10,
    });
    await vi.advanceTimersByTimeAsync(20);
    const result = await pending;
    expect(result.ok).toBe(false);
    if (result.ok) return;
    expect(result.error).toBeInstanceOf(DownloadError);
    expect(result.error.kind).toBe("timeout");
  });
});
