import { isTauriRuntime, tauriInvoke } from "../sync/tauriInvoke";
import {
  classifyFetchError,
  classifyHttpMessage,
  classifyHttpStatus,
  type NewsFetchError,
} from "./fetchError";

const FEED_UA = "AetherFeed/0.1";

export type FetchBodyResult = { ok: true; body: string } | { ok: false; error: NewsFetchError };

export type FetchBodyOptions = {
  fetch?: typeof fetch;
  timeoutMs?: number;
  signal?: AbortSignal;
};

export async function fetchFeedBody(
  url: string,
  options: FetchBodyOptions = {},
): Promise<FetchBodyResult> {
  const fetchImpl = options.fetch ?? globalThis.fetch.bind(globalThis);
  const timeoutMs = options.timeoutMs ?? 15_000;
  const controller = new AbortController();
  let timedOut = false;
  const timer = setTimeout(() => {
    timedOut = true;
    controller.abort();
  }, timeoutMs);
  const onAbort = (): void => controller.abort();
  options.signal?.addEventListener("abort", onAbort);
  try {
    if (isTauriRuntime() && options.fetch == null) {
      const invoke = tauriInvoke();
      if (invoke) {
        try {
          const body = String(await invoke("feed_fetch", { url }));
          if (body) return { ok: true, body };
        } catch (err) {
          const classified = classifyHttpMessage(String(err));
          if (classified === "gone") return { ok: false, error: "gone" };
        }
      }
    }
    const res = await fetchImpl(url, {
      signal: controller.signal,
      headers: { "User-Agent": FEED_UA },
    });
    if (!res.ok) return { ok: false, error: classifyHttpStatus(res.status) };
    return { ok: true, body: await res.text() };
  } catch (err) {
    return { ok: false, error: classifyFetchError(err, timedOut) };
  } finally {
    clearTimeout(timer);
    options.signal?.removeEventListener("abort", onAbort);
  }
}
