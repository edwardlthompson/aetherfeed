import { kindOfError, messageFor } from "./errors";

export function esc(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

export function clock(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000));
  return `${Math.floor(total / 60)}:${String(total % 60).padStart(2, "0")}`;
}

export function showError(pane: HTMLElement, err: unknown): void {
  const box = pane.querySelector<HTMLElement>("[data-player-error]");
  const text = pane.querySelector<HTMLElement>("[data-error-text]");
  if (box && text) {
    text.textContent = messageFor(kindOfError(err));
    box.hidden = false;
  }
}
