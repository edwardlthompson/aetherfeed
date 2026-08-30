import type { Feed } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";
import { MemoryNewsRepository } from "./memoryRepo";

const sample: Feed = {
  id: "feed:local",
  title: "Local",
  url: "https://example.invalid/rss.xml",
  kind: "news",
  updatedAt: 1,
};

describe("memory news repository", () => {
  it("maps aborted fetch to a typed error string and does not throw", async () => {
    const repo = new MemoryNewsRepository({
      feeds: [sample],
      fetch: async () => {
        throw new DOMException("The operation was aborted.", "AbortError");
      },
    });
    const articles = await repo.refresh(sample.id);
    expect(articles).toEqual([]);
    expect(repo.lastError).toBe("aborted");
  });

  it("maps timeout abort to timeout", async () => {
    const repo = new MemoryNewsRepository({
      feeds: [sample],
      timeoutMs: 1,
      fetch: (_url, init) =>
        new Promise<Response>((_resolve, reject) => {
          init?.signal?.addEventListener("abort", () => {
            reject(new DOMException("The operation was aborted.", "AbortError"));
          });
        }),
    });
    const articles = await repo.refresh(sample.id);
    expect(articles).toEqual([]);
    expect(repo.lastError).toBe("timeout");
  });
});
