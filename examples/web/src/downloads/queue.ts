import type { DownloadStatus, QueueItem } from "./types";

export class DownloadQueue {
  private readonly items: QueueItem[] = [];

  enqueue(episodeId: string, url: string): QueueItem {
    const existing = this.find(episodeId);
    if (existing) {
      if (existing.status === "queued" || existing.status === "running") return existing;
      existing.url = url;
      existing.status = "queued";
      return existing;
    }
    const item: QueueItem = { episodeId, url, status: "queued" };
    this.items.push(item);
    return item;
  }

  list(): readonly QueueItem[] {
    return this.items;
  }

  find(episodeId: string): QueueItem | undefined {
    return this.items.find((item) => item.episodeId === episodeId);
  }

  nextQueued(): QueueItem | undefined {
    return this.items.find((item) => item.status === "queued");
  }

  setStatus(episodeId: string, status: DownloadStatus): QueueItem | undefined {
    const item = this.find(episodeId);
    if (item) item.status = status;
    return item;
  }

  pause(episodeId: string): QueueItem | undefined {
    const item = this.find(episodeId);
    if (!item) return undefined;
    if (item.status === "queued" || item.status === "running") item.status = "paused";
    return item;
  }

  resume(episodeId: string): QueueItem | undefined {
    const item = this.find(episodeId);
    if (!item) return undefined;
    if (item.status === "paused" || item.status === "failed") item.status = "queued";
    return item;
  }
}

export function createDownloadQueue(): DownloadQueue {
  return new DownloadQueue();
}
