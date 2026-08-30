export type DownloadStatus = "queued" | "running" | "done" | "failed" | "paused";

export type QueueItem = {
  episodeId: string;
  url: string;
  status: DownloadStatus;
};

export type DownloadErrorKind = "aborted" | "timeout" | "network";

export class DownloadError extends Error {
  readonly kind: DownloadErrorKind;

  constructor(kind: DownloadErrorKind, message: string) {
    super(message);
    this.name = "DownloadError";
    this.kind = kind;
  }
}

export function classifyDownloadError(err: unknown, timedOut: boolean): DownloadError {
  if (timedOut) return new DownloadError("timeout", "Download timed out");
  if (err instanceof DownloadError) return err;
  const name = err instanceof Error || err instanceof DOMException ? err.name : "";
  if (name === "AbortError") return new DownloadError("aborted", "Download was cancelled");
  return new DownloadError("network", "Download failed");
}
