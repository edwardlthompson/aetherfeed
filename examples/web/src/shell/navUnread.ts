export type NavUnread = { news: number; podcast: number; booru: number };

export function paintNavUnread(root: HTMLElement, counts: NavUnread): void {
  (Object.keys(counts) as (keyof NavUnread)[]).forEach((mode) => {
    const el = root.querySelector<HTMLElement>(`[data-nav-unread="${mode}"]`);
    if (!el) return;
    const n = counts[mode];
    el.textContent = String(n);
    el.hidden = n <= 0;
  });
}

export function paintUnifiedUnread(
  root: HTMLElement,
  news: number,
  podcast: number,
  booru: number,
): void {
  const el = root.querySelector<HTMLElement>("[data-unified-unread]");
  if (!el) return;
  const n = news + podcast + booru;
  el.textContent = String(n);
  el.hidden = n <= 0;
}

export function modeUnreadBadge(mode: string, count = 0): string {
  const hidden = count <= 0 ? " hidden" : "";
  return `<span class="af-nav-unread" data-nav-unread="${mode}"${hidden}>${count}</span>`;
}
