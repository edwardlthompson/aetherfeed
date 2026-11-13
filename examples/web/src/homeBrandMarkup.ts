import { isOnline } from "./greet";
import { t } from "./i18n";

export function homeBrandMarkup(): string {
  const statusKey = isOnline() ? "app.status.online" : "app.status.offline";
  return `
        <header class="af-home-brand">
          <h1 class="af-home-title">${t("app.title")}</h1>
          <p class="af-home-greeting">${t("app.greeting")}</p>
          <p class="af-home-status" data-testid="status">${t(statusKey)}</p>
        </header>`;
}
