import type { NewsChromePrefs } from "./chromePrefs";
import { clampPanes, saveNewsChrome } from "./chromePrefs";

function columnCss(prefs: NewsChromePrefs): string {
  return `${prefs.source}fr 0.4rem ${prefs.timeline}fr 0.4rem ${prefs.reader}fr`;
}

export function bindNewsLayout(
  root: HTMLElement,
  chrome: NewsChromePrefs,
  onChange: (next: NewsChromePrefs) => void,
): void {
  const commit = (next: NewsChromePrefs): void => {
    saveNewsChrome(next);
    onChange(next);
  };
  for (const handle of root.querySelectorAll<HTMLElement>("[data-split]")) {
    handle.addEventListener("pointerdown", (event) => {
      event.preventDefault();
      const kind = handle.dataset.split;
      const host = root.querySelector<HTMLElement>(".af-news-columns");
      if (!host || !kind) return;
      const startX = event.clientX;
      const width = host.getBoundingClientRect().width || 1;
      let next = chrome;
      const move = (ev: PointerEvent): void => {
        const delta = (ev.clientX - startX) / width;
        const panes =
          kind === "source"
            ? clampPanes(chrome.source + delta, chrome.timeline - delta, chrome.reader)
            : clampPanes(chrome.source, chrome.timeline + delta, chrome.reader - delta);
        next = { ...chrome, ...panes };
        host.style.gridTemplateColumns = columnCss(next);
      };
      const up = (): void => {
        window.removeEventListener("pointermove", move);
        window.removeEventListener("pointerup", up);
        commit(next);
      };
      window.addEventListener("pointermove", move);
      window.addEventListener("pointerup", up);
    });
  }
}
