import { downloadsCopy as copy } from "./copy";
import { fetchEnclosure } from "./fetchEnclosure";
import { createDownloadQueue, type DownloadQueue } from "./queue";
import { panelMarkup } from "./render";
import { canStartDownload, getAutoDownload, setAutoDownload, setWifiOnly } from "./settings";

export type DownloadsPanelOptions = {
  queue?: DownloadQueue;
  fetchImpl?: typeof fetch;
  isWifi?: () => boolean;
  timeoutMs?: number;
};

export function createDownloadsPanel(
  root: HTMLElement,
  options: DownloadsPanelOptions = {},
): HTMLElement {
  const queue = options.queue ?? createDownloadQueue();
  const aborts = new Map<string, AbortController>();
  let message = "";

  const pane = document.createElement("section");
  pane.className = "af-downloads";
  pane.dataset.testid = "downloads-panel";
  pane.setAttribute("aria-label", copy.title);
  root.replaceChildren(pane);

  const paint = (): void => {
    pane.innerHTML = panelMarkup(queue.list(), message);
  };

  const wifiOk = (): boolean => canStartDownload({ isWifi: options.isWifi?.() });

  const startNext = (): void => {
    if (queue.list().some((item) => item.status === "running")) return;
    if (!wifiOk()) {
      message = copy.wifiBlocked;
      paint();
      return;
    }
    const item = queue.nextQueued();
    if (!item) return;
    const ctrl = new AbortController();
    aborts.set(item.episodeId, ctrl);
    queue.setStatus(item.episodeId, "running");
    paint();
    void fetchEnclosure(item.url, {
      fetch: options.fetchImpl,
      timeoutMs: options.timeoutMs,
      signal: ctrl.signal,
    }).then((result) => {
      aborts.delete(item.episodeId);
      if (result.ok) queue.setStatus(item.episodeId, "done");
      else if (result.error.kind === "aborted") queue.setStatus(item.episodeId, "paused");
      else {
        queue.setStatus(item.episodeId, "failed");
        message = result.error.message;
      }
      paint();
      if (getAutoDownload()) startNext();
    });
  };

  pane.addEventListener("change", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLInputElement)) return;
    if (target.matches("[data-wifi-only]")) {
      setWifiOnly(target.checked);
      if (getAutoDownload()) startNext();
    }
    if (target.matches("[data-auto-download]")) {
      setAutoDownload(target.checked);
      if (target.checked) startNext();
    }
  });

  pane.addEventListener("submit", (event) => {
    const form = (event.target as HTMLElement).closest("form");
    if (!form?.matches("[data-enqueue]")) return;
    event.preventDefault();
    const data = new FormData(form);
    const episodeId = String(data.get("episodeId") ?? "").trim();
    const url = String(data.get("url") ?? "").trim();
    if (!episodeId || !url) return;
    queue.enqueue(episodeId, url);
    paint();
    if (getAutoDownload()) startNext();
  });

  pane.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    const pauseId = target.closest<HTMLElement>("[data-pause]")?.dataset.pause;
    const resumeId = target.closest<HTMLElement>("[data-resume]")?.dataset.resume;
    const cancelId = target.closest<HTMLElement>("[data-cancel]")?.dataset.cancel;
    if (pauseId) {
      aborts.get(pauseId)?.abort();
      queue.pause(pauseId);
      paint();
    }
    if (resumeId) {
      queue.resume(resumeId);
      paint();
      startNext();
    }
    if (cancelId) {
      aborts.get(cancelId)?.abort();
      queue.pause(cancelId);
      paint();
    }
  });

  paint();
  if (getAutoDownload()) startNext();
  return pane;
}
