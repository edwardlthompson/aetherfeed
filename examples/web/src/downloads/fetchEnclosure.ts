import { classifyDownloadError, type DownloadError } from "./types";

export type FetchEnclosureResult =
  | { ok: true; bytes: ArrayBuffer }
  | { ok: false; error: DownloadError };

export type FetchEnclosureOptions = {
  fetch?: typeof fetch;
  timeoutMs?: number;
  signal?: AbortSignal;
};

export async function fetchEnclosure(
  url: string,
  options: FetchEnclosureOptions = {},
): Promise<FetchEnclosureResult> {
  const fetchImpl = options.fetch ?? globalThis.fetch.bind(globalThis);
  const timeoutMs = options.timeoutMs ?? 30_000;
  const controller = new AbortController();
  let timedOut = false;
  const timer = setTimeout(() => {
    timedOut = true;
    controller.abort();
  }, timeoutMs);
  const onAbort = (): void => {
    controller.abort();
  };
  options.signal?.addEventListener("abort", onAbort);
  try {
    if (options.signal?.aborted) {
      return {
        ok: false,
        error: classifyDownloadError(new DOMException("Aborted", "AbortError"), false),
      };
    }
    const res = await fetchImpl(url, { signal: controller.signal });
    if (!res.ok) {
      return { ok: false, error: classifyDownloadError(new Error(`HTTP ${res.status}`), false) };
    }
    return { ok: true, bytes: await res.arrayBuffer() };
  } catch (err) {
    return { ok: false, error: classifyDownloadError(err, timedOut) };
  } finally {
    clearTimeout(timer);
    options.signal?.removeEventListener("abort", onAbort);
  }
}
