import { describe, expect, it } from "vitest";
import { createDownloadQueue } from "./queue";

describe("DownloadQueue order", () => {
  it("keeps FIFO order and serves the first queued episode", () => {
    const queue = createDownloadQueue();
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    queue.enqueue("ep-b", "https://example.invalid/b.mp3");
    queue.enqueue("ep-c", "https://example.invalid/c.mp3");
    expect(queue.list().map((item) => item.episodeId)).toEqual(["ep-a", "ep-b", "ep-c"]);
    expect(queue.nextQueued()?.episodeId).toBe("ep-a");
    queue.setStatus("ep-a", "running");
    expect(queue.nextQueued()?.episodeId).toBe("ep-b");
  });

  it("does not reorder when a later item is re-enqueued while queued", () => {
    const queue = createDownloadQueue();
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    queue.enqueue("ep-b", "https://example.invalid/b.mp3");
    queue.enqueue("ep-a", "https://example.invalid/a2.mp3");
    expect(queue.list().map((item) => item.episodeId)).toEqual(["ep-a", "ep-b"]);
    expect(queue.find("ep-a")?.url).toBe("https://example.invalid/a.mp3");
  });

  it("re-queues a failed item without moving it ahead of earlier queued work", () => {
    const queue = createDownloadQueue();
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    queue.enqueue("ep-b", "https://example.invalid/b.mp3");
    queue.setStatus("ep-a", "failed");
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    expect(queue.list().map((item) => item.episodeId)).toEqual(["ep-a", "ep-b"]);
    expect(queue.nextQueued()?.episodeId).toBe("ep-a");
  });

  it("pauses queued work and resumes it back to queued", () => {
    const queue = createDownloadQueue();
    queue.enqueue("ep-a", "https://example.invalid/a.mp3");
    expect(queue.pause("ep-a")?.status).toBe("paused");
    expect(queue.nextQueued()).toBeUndefined();
    expect(queue.resume("ep-a")?.status).toBe("queued");
    expect(queue.nextQueued()?.episodeId).toBe("ep-a");
  });
});
