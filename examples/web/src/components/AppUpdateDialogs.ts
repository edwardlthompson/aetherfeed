import type { LaunchPrompt } from "../about/runAppUpdates";
import { t } from "../i18n";

export function createLaunchPromptDialog(
  prompt: LaunchPrompt,
  onAction: (action: "donate" | "not-now" | "install" | "later") => void,
): HTMLElement {
  const overlay = document.createElement("div");
  overlay.className = "af-launch-overlay";
  overlay.setAttribute("role", "alertdialog");
  overlay.setAttribute("aria-modal", "true");
  overlay.dataset.testid = prompt.kind === "donate" ? "donate-nudge" : "update-prompt";

  const card = document.createElement("div");
  card.className = "af-launch-card";

  const title = document.createElement("h2");
  const body = document.createElement("p");
  const actions = document.createElement("div");
  actions.className = "af-launch-actions";

  if (prompt.kind === "donate") {
    title.id = "af-donate-title";
    title.textContent = t("about.donate.nudge.title");
    body.textContent = t("about.donate.nudge.body");
    actions.append(
      actionButton(t("about.donate.not_now"), () => onAction("not-now"), "donate-not-now"),
      actionButton(t("about.donate.venmo"), () => onAction("donate"), "donate-venmo"),
    );
  } else {
    title.id = "af-update-title";
    title.textContent = t("about.update.available");
    body.textContent = t("about.update.prompt").replace("{version}", prompt.version);
    actions.append(
      actionButton(t("about.update.later"), () => onAction("later"), "update-later"),
      actionButton(t("about.update.install"), () => onAction("install"), "update-install"),
    );
  }
  overlay.setAttribute("aria-labelledby", title.id);
  card.append(title, body, actions);
  overlay.append(card);
  return overlay;
}

function actionButton(label: string, onClick: () => void, testId: string): HTMLButtonElement {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.textContent = label;
  btn.dataset.testid = testId;
  btn.addEventListener("click", onClick);
  return btn;
}
