import { newsDrawerOffstage } from "./newsSwipe";

export function applyNewsDrawerOpen(host: HTMLElement, open: boolean): void {
  const sidebar = host.querySelector<HTMLElement>(".af-news-sidebar");
  const scrim = host.querySelector<HTMLElement>("[data-news-scrim]");
  host.classList.toggle("is-sources-open", open);
  if (sidebar) {
    sidebar.hidden = newsDrawerOffstage(open);
    sidebar.setAttribute("aria-hidden", newsDrawerOffstage(open) ? "true" : "false");
  }
  if (scrim) scrim.hidden = newsDrawerOffstage(open);
}

export function bindNewsSourcesDrawer(root: HTMLElement, signal?: AbortSignal): void {
  const host = root.querySelector<HTMLElement>(".af-news-columns");
  if (!host || !window.matchMedia?.("(max-width: 720px)")?.matches) return;
  applyNewsDrawerOpen(host, host.classList.contains("is-sources-open"));
  const open = (): void => applyNewsDrawerOpen(host, true);
  const close = (): void => applyNewsDrawerOpen(host, false);
  root.querySelector("[data-news-sources]")?.addEventListener("click", open, { signal });
  host.querySelector("[data-news-scrim]")?.addEventListener("click", close, { signal });
  root.addEventListener(
    "click",
    (event) => {
      const target = event.target;
      if (target instanceof HTMLElement && target.closest("[data-feed-id]")) close();
    },
    { signal },
  );
  let startX = 0;
  let tracking = false;
  host.addEventListener(
    "pointerdown",
    (event) => {
      if (event.clientX > 28) return;
      tracking = true;
      startX = event.clientX;
    },
    { signal },
  );
  host.addEventListener(
    "pointerup",
    (event) => {
      if (!tracking) return;
      tracking = false;
      if (event.clientX - startX > 48) open();
    },
    { signal },
  );
}
