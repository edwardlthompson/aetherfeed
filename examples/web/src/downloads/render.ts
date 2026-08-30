import { downloadsCopy as copy } from "./copy";
import { getAutoDownload, getWifiOnly } from "./settings";
import type { QueueItem } from "./types";

function esc(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function statusLabel(status: QueueItem["status"]): string {
  if (status === "running") return copy.statusRunning;
  if (status === "done") return copy.statusDone;
  if (status === "failed") return copy.statusFailed;
  if (status === "paused") return copy.statusPaused;
  return copy.statusQueued;
}

function rowMarkup(item: QueueItem): string {
  const id = esc(item.episodeId);
  return `<li data-episode-id="${id}" data-status="${item.status}">
    <span>${id}</span>
    <span data-item-status>${esc(statusLabel(item.status))}</span>
    <button type="button" data-pause="${id}">${esc(copy.pause)}</button>
    <button type="button" data-resume="${id}">${esc(copy.resume)}</button>
    <button type="button" data-cancel="${id}">${esc(copy.cancel)}</button>
  </li>`;
}

export function panelMarkup(items: readonly QueueItem[], message: string): string {
  const rows = items.length
    ? items.map(rowMarkup).join("")
    : `<li data-downloads-empty>${esc(copy.empty)}</li>`;
  return `
    <h2>${esc(copy.title)}</h2>
    <label><input type="checkbox" data-wifi-only ${getWifiOnly() ? "checked" : ""} /> ${esc(copy.wifiOnly)}</label>
    <label><input type="checkbox" data-auto-download ${getAutoDownload() ? "checked" : ""} /> ${esc(copy.autoDownload)}</label>
    <form data-enqueue>
      <label>${esc(copy.episodeId)} <input name="episodeId" required /></label>
      <label>${esc(copy.url)} <input name="url" type="url" required /></label>
      <button type="submit">${esc(copy.enqueue)}</button>
    </form>
    <p data-downloads-message aria-live="polite">${esc(message)}</p>
    <ul data-download-queue>${rows}</ul>
  `;
}
