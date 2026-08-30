import { t } from "../i18n";
import type { AppMode } from "./modeRail";

function btn(attr: string, eventName: string, label: string): string {
  return `<button type="button" class="af-action-btn" ${attr} data-action-event="${eventName}">${label}</button>`;
}

export function actionBarHtml(mode: AppMode): string {
  const body =
    mode === "news"
      ? `${btn("data-action-prev", "af-news-prev", t("action.prev"))}${btn("data-action-star", "af-news-star", t("news.star"))}${btn("data-action-unread", "af-news-unread", t("news.unread"))}${btn("data-action-next", "af-news-next", t("action.next"))}`
      : mode === "podcast"
        ? `${btn("data-action-skip-back", "af-pod-skip-back", t("action.skip_back"))}${btn("data-action-play", "af-pod-play", t("action.play"))}${btn("data-action-skip-fwd", "af-pod-skip-fwd", t("action.skip_fwd"))}`
        : `${btn("data-action-favorite", "af-boards-fav", t("action.favorite"))}${btn("data-action-info", "af-boards-info", t("action.info"))}`;
  return `<div class="af-action-bar" data-action-bar>${btn("data-action-share", "af-share", t("action.share"))}${body}</div>`;
}

export function bindActionBar(root: HTMLElement): void {
  root.querySelector("[data-action-bar]")?.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    const name = target.closest<HTMLElement>("[data-action-event]")?.dataset.actionEvent;
    if (name) window.dispatchEvent(new Event(name));
  });
}
