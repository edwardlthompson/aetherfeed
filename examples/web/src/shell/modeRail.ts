import { t } from "../i18n";
import { modeUnreadBadge } from "./navUnread";

export type AppMode = "news" | "podcast" | "booru";

export const APP_MODES: readonly AppMode[] = ["news", "podcast", "booru"];

export type ModeNavKind = "rail" | "bar";

const MODE_LABEL_KEYS: Record<AppMode, string> = {
  news: "nav.news",
  podcast: "nav.podcasts",
  booru: "nav.boards",
};

export function createModeRail(
  active: AppMode,
  onSelect?: (mode: AppMode) => void,
  kind: ModeNavKind = "rail",
): HTMLElement {
  const nav = document.createElement("nav");
  nav.className = kind === "rail" ? "af-shell-rail" : "af-mode-nav af-shell-nav-narrow";
  nav.dataset.chromeSlot = "rail";
  nav.setAttribute("aria-label", t("nav.label"));
  for (const mode of APP_MODES) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "af-mode-btn";
    button.dataset.mode = mode;
    button.innerHTML = `${t(MODE_LABEL_KEYS[mode])}${modeUnreadBadge(mode)}`;
    if (mode === active) button.setAttribute("aria-current", "page");
    if (onSelect) button.addEventListener("click", () => onSelect(mode));
    nav.append(button);
  }
  return nav;
}
