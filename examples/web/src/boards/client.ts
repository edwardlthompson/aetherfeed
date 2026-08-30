import { parseBoardPosts } from "./parse";
import { BoardFetchError, type BoardPost, type BoardSource, type TagQuery } from "./types";

export interface BoardClient {
  search(source: BoardSource, query: TagQuery, signal?: AbortSignal): Promise<BoardPost[]>;
  isBlacklisted(tag: string): boolean;
}

export type BoardFetchFn = (url: string, init?: RequestInit) => Promise<Response>;

export type FetchBoardClientOptions = {
  blacklist?: Iterable<string>;
  fetchImpl?: BoardFetchFn;
  timeoutMs?: number;
  retries?: number;
};

function searchUrl(source: BoardSource, query: TagQuery): string {
  const base = source.baseUrl.replace(/\/+$/, "");
  const url = new URL(`${base}/posts.json`);
  url.searchParams.set("tags", query.tags.join(" "));
  url.searchParams.set("page", String(query.page > 0 ? query.page : 1));
  return url.toString();
}

function classifyAbort(external?: AbortSignal): BoardFetchError {
  if (external?.aborted) return new BoardFetchError("aborted", "Board search was cancelled");
  return new BoardFetchError("timeout", "Board search timed out");
}

function isAbortError(err: unknown): boolean {
  return err instanceof Error && err.name === "AbortError";
}

export class FetchBoardClient implements BoardClient {
  private readonly blocked: Set<string>;
  private readonly fetchImpl: BoardFetchFn;
  private readonly timeoutMs: number;
  private readonly retries: number;

  constructor(options: FetchBoardClientOptions = {}) {
    this.blocked = new Set([...(options.blacklist ?? [])].map((tag) => tag.toLowerCase()));
    this.fetchImpl = options.fetchImpl ?? ((url, init) => fetch(url, init));
    this.timeoutMs = options.timeoutMs ?? 10_000;
    this.retries = options.retries ?? 1;
  }

  isBlacklisted(tag: string): boolean {
    return this.blocked.has(tag.toLowerCase());
  }

  async search(source: BoardSource, query: TagQuery, signal?: AbortSignal): Promise<BoardPost[]> {
    if (!query.tags.length) return [];
    const payload = await this.fetchJson(searchUrl(source, query), signal);
    return parseBoardPosts(source.id, payload).filter(
      (post) => !post.tags.some((tag) => this.isBlacklisted(tag)),
    );
  }

  private async fetchJson(url: string, signal?: AbortSignal): Promise<unknown> {
    let last: BoardFetchError | undefined;
    for (let attempt = 0; attempt <= this.retries; attempt += 1) {
      try {
        return await this.once(url, signal);
      } catch (err) {
        last =
          err instanceof BoardFetchError
            ? err
            : new BoardFetchError("network", "Board search failed");
        if (!last.retryable || signal?.aborted || attempt === this.retries) throw last;
      }
    }
    throw last ?? new BoardFetchError("network", "Board search failed");
  }

  private async once(url: string, signal?: AbortSignal): Promise<unknown> {
    const ctrl = new AbortController();
    const timer = setTimeout(() => ctrl.abort(), this.timeoutMs);
    const onAbort = () => ctrl.abort();
    signal?.addEventListener("abort", onAbort);
    try {
      if (signal?.aborted) throw classifyAbort(signal);
      const res = await this.fetchImpl(url, { signal: ctrl.signal });
      if (!res.ok) throw new BoardFetchError("network", `Board HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      if (err instanceof BoardFetchError) throw err;
      if (isAbortError(err) || signal?.aborted) throw classifyAbort(signal);
      throw new BoardFetchError("network", "Board search failed");
    } finally {
      clearTimeout(timer);
      signal?.removeEventListener("abort", onAbort);
    }
  }
}
