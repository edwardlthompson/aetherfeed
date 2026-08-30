import { type ImageFetchOptions, ReaderImageError } from "./types";

const DEFAULT_TIMEOUT_MS = 8_000;

function isAbortError(err: unknown): boolean {
  return err instanceof Error && err.name === "AbortError";
}

function isFetchableSrc(src: string): boolean {
  const lower = src.trim().toLowerCase();
  if (!lower) return false;
  if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("blob:")) {
    return false;
  }
  return true;
}

async function blobToDataUrl(blob: Blob): Promise<string> {
  const bytes = new Uint8Array(await blob.arrayBuffer());
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  const mime = blob.type || "application/octet-stream";
  return `data:${mime};base64,${btoa(binary)}`;
}

async function fetchOne(src: string, options: ImageFetchOptions): Promise<string | null> {
  const fetchImpl = options.fetchImpl ?? globalThis.fetch.bind(globalThis);
  const timeoutMs = options.timeoutMs ?? DEFAULT_TIMEOUT_MS;
  const ctrl = new AbortController();
  let timedOut = false;
  const timer = setTimeout(() => {
    timedOut = true;
    ctrl.abort();
  }, timeoutMs);
  const onAbort = (): void => ctrl.abort();
  options.signal?.addEventListener("abort", onAbort);
  try {
    if (options.signal?.aborted) throw new ReaderImageError("aborted");
    const res = await fetchImpl(src, { signal: ctrl.signal });
    if (!res.ok) return null;
    return await blobToDataUrl(await res.blob());
  } catch (err) {
    if (err instanceof ReaderImageError) throw err;
    if (isAbortError(err) || options.signal?.aborted) {
      throw new ReaderImageError(options.signal?.aborted && !timedOut ? "aborted" : "timeout");
    }
    return null;
  } finally {
    clearTimeout(timer);
    options.signal?.removeEventListener("abort", onAbort);
  }
}

/** Fetch image blobs as data URLs. Honors timeout and AbortSignal. */
export async function fetchImageBlobs(
  srcs: string[],
  options: ImageFetchOptions = {},
): Promise<Record<string, string>> {
  if (options.signal?.aborted) throw new ReaderImageError("aborted");
  const unique = [...new Set((srcs ?? []).map((s) => s.trim()).filter(isFetchableSrc))];
  const out: Record<string, string> = {};
  let done = 0;
  options.onProgress?.(0, unique.length);
  for (const src of unique) {
    if (options.signal?.aborted) throw new ReaderImageError("aborted");
    const dataUrl = await fetchOne(src, options);
    if (dataUrl) out[src] = dataUrl;
    done += 1;
    options.onProgress?.(done, unique.length);
  }
  return out;
}
