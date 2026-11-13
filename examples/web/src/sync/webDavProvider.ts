import { isOpaqueEncName, SYNC_DOCUMENT_VERSION, type SyncProvider } from "@aetherfeed/domain";

export const SYNC_BLOB_NAME = "aetherfeed-sync.enc";

export type FetchFn = (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>;

export type WebDavProviderOptions = {
  endpoint: string;
  username?: string;
  password?: string;
  blobName?: string;
  fetchFn?: FetchFn;
};

function basicAuthHeader(username?: string, password?: string): HeadersInit {
  const user = username?.trim() ?? "";
  const pass = password?.trim() ?? "";
  if (!user && !pass) {
    return {};
  }
  const token = btoa(`${user}:${pass}`);
  return { Authorization: `Basic ${token}` };
}

function blobUrl(endpoint: string, blobName: string): string {
  const base = endpoint.replace(/\/+$/, "");
  return `${base}/${blobName}`;
}

/** In-memory WebDAV backend for unit tests and desktop `memory://` bridges. */
export class InMemoryWebDavBackend {
  private readonly blobs = new Map<string, Uint8Array>();

  keyFor(endpoint: string): string {
    return endpoint.replace(/\/+$/, "");
  }

  get(endpoint: string, blobName: string): Uint8Array | null {
    return this.blobs.get(`${this.keyFor(endpoint)}/${blobName}`) ?? null;
  }

  put(endpoint: string, blobName: string, blob: Uint8Array): void {
    this.blobs.set(`${this.keyFor(endpoint)}/${blobName}`, blob.slice());
  }
}

export function createWebDavProvider(
  options: WebDavProviderOptions,
  memory?: InMemoryWebDavBackend,
): SyncProvider {
  const blobName = options.blobName ?? SYNC_BLOB_NAME;
  if (!isOpaqueEncName(blobName)) {
    throw new Error("sync blob must be opaque .enc");
  }
  const endpoint = options.endpoint.trim();
  const useMemory = endpoint.startsWith("memory://");
  const fetchFn = options.fetchFn ?? fetch;

  return {
    id: "webdav",
    async pull(): Promise<Uint8Array | null> {
      if (useMemory && memory) {
        return memory.get(endpoint, blobName);
      }
      const url = blobUrl(endpoint, blobName);
      const response = await fetchFn(url, {
        method: "GET",
        headers: basicAuthHeader(options.username, options.password),
      });
      if (response.status === 404) {
        return null;
      }
      if (!response.ok) {
        throw new Error(`WebDAV pull HTTP ${response.status}`);
      }
      const buffer = await response.arrayBuffer();
      if (buffer.byteLength === 0) {
        return null;
      }
      return new Uint8Array(buffer);
    },
    async push(blob: Uint8Array): Promise<void> {
      if (blob.byteLength === 0) {
        throw new Error("sync blob must not be empty");
      }
      if (useMemory && memory) {
        memory.put(endpoint, blobName, blob);
        return;
      }
      const url = blobUrl(endpoint, blobName);
      const response = await fetchFn(url, {
        method: "PUT",
        headers: {
          ...basicAuthHeader(options.username, options.password),
          "Content-Type": "application/octet-stream",
        },
        // Copy into a fresh ArrayBuffer so BodyInit accepts the payload under TS 5.7+.
        body: Uint8Array.from(blob).buffer,
      });
      if (!response.ok) {
        throw new Error(`WebDAV push HTTP ${response.status}`);
      }
    },
  };
}

/** Re-export for callers building encrypted documents. */
export { SYNC_DOCUMENT_VERSION };
