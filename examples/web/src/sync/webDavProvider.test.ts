import { describe, expect, it } from "vitest";
import { createWebDavProvider, InMemoryWebDavBackend, SYNC_BLOB_NAME } from "./webDavProvider";

describe("webDavProvider", () => {
  it("push then pull round-trips via in-memory backend", async () => {
    const memory = new InMemoryWebDavBackend();
    const provider = createWebDavProvider({ endpoint: "memory://dav.example.test/sync" }, memory);
    const blob = new Uint8Array([1, 2, 3]);
    expect(await provider.pull()).toBeNull();
    await provider.push(blob);
    const pulled = await provider.pull();
    expect(pulled).toEqual(blob);
    expect(SYNC_BLOB_NAME.endsWith(".enc")).toBe(true);
  });

  it("uses fetch for HTTP endpoints", async () => {
    const store = new Map<string, Uint8Array>();
    const provider = createWebDavProvider({
      endpoint: "https://dav.example.test/sync",
      fetchFn: async (input, init) => {
        const url = String(input);
        if (init?.method === "PUT") {
          const bytes = new Uint8Array(await new Response(init.body).arrayBuffer());
          store.set(url, bytes);
          return new Response(null, { status: 201 });
        }
        const found = store.get(url);
        if (!found) {
          return new Response(null, { status: 404 });
        }
        return new Response(Uint8Array.from(found).buffer, { status: 200 });
      },
    });
    const blob = new Uint8Array([4, 5]);
    await provider.push(blob);
    expect(await provider.pull()).toEqual(blob);
  });
});
