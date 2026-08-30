import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchImageBlobs } from "./images";
import { ReaderImageError } from "./types";

function abortError(): Error {
  const err = new Error("Aborted");
  err.name = "AbortError";
  return err;
}

afterEach(() => {
  vi.unstubAllGlobals();
  vi.useRealTimers();
});

describe("fetchImageBlobs", () => {
  it("aborts in-flight image fetches", async () => {
    const ctrl = new AbortController();
    const fetchImpl: typeof fetch = (_input, init) => {
      return new Promise<Response>((_resolve, reject) => {
        const fail = () => reject(abortError());
        if (init?.signal?.aborted) {
          fail();
          return;
        }
        init?.signal?.addEventListener("abort", fail);
      });
    };
    const pending = fetchImageBlobs(["https://cdn.example/a.png"], {
      fetchImpl,
      timeoutMs: 50_000,
      signal: ctrl.signal,
    });
    ctrl.abort();
    await expect(pending).rejects.toBeInstanceOf(ReaderImageError);
    await expect(pending).rejects.toMatchObject({ code: "aborted", name: "ReaderImageError" });
  });

  it("times out when the image fetch never settles", async () => {
    vi.useFakeTimers();
    const fetchImpl: typeof fetch = (_input, init) => {
      return new Promise<Response>((_resolve, reject) => {
        init?.signal?.addEventListener("abort", () => reject(abortError()));
      });
    };
    const pending = fetchImageBlobs(["https://cdn.example/a.png"], {
      fetchImpl,
      timeoutMs: 10,
    });
    const expectReject = expect(pending).rejects.toMatchObject({
      name: "ReaderImageError",
      code: "timeout",
    });
    await vi.advanceTimersByTimeAsync(20);
    await expectReject;
  });

  it("skips data and javascript srcs", async () => {
    const fetchImpl = vi.fn<typeof fetch>();
    const blobs = await fetchImageBlobs(["data:image/png;base64,xx", "javascript:alert(1)"], {
      fetchImpl,
    });
    expect(blobs).toEqual({});
    expect(fetchImpl).not.toHaveBeenCalled();
  });
});
