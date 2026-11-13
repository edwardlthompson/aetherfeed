import { t } from "../i18n";

export function unreadLabel(count: number): string {
  return t("news.unread_total").replace("{count}", String(count));
}

export function edgeButton(
  kind: "prev" | "next",
  enabled: boolean,
  onClick?: () => void,
): HTMLButtonElement {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = `af-reader-edge af-reader-edge-${kind}`;
  btn.dataset.testid = kind === "prev" ? "article-prev" : "article-next";
  const label = t(kind === "prev" ? "feed.reader.previous" : "feed.reader.next");
  btn.setAttribute("aria-label", label);
  btn.title = label;
  btn.textContent = kind === "prev" ? "‹" : "›";
  btn.disabled = !enabled;
  btn.addEventListener("click", () => onClick?.());
  return btn;
}
