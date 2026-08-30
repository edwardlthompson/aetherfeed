import { VENMO_DONATE_URL } from "../about/donate";
import { t } from "../i18n";
import { loadCacheRetainMode, saveCacheRetainMode } from "../news/cacheRetain";
import { getNewsWifiOnly, setNewsWifiOnly } from "../news/newsNetwork";
import { createReaderImportPanel } from "../readerimport/ImportPanel";
import {
  applySettingsThemeMode,
  getSettingsThemeMode,
  isUpdateCheckEnabled,
  setUpdateCheckEnabled,
} from "../settings/preferences";
import type { ThemeMode } from "../theme";
import { createDriveSyncPanel } from "./DriveSyncPanel";

export type SettingsPanelCallbacks = {
  onClose: () => void;
  onUpdateCheckChange?: (enabled: boolean) => void;
};

export function createSettingsPanel(callbacks: SettingsPanelCallbacks): HTMLElement {
  const panel = document.createElement("section");
  panel.className = "af-settings-panel";
  panel.setAttribute("aria-label", t("settings.title"));
  panel.dataset.testid = "settings-panel";

  const themeMode = getSettingsThemeMode();
  const updateEnabled = isUpdateCheckEnabled();
  const wifiOnly = getNewsWifiOnly();

  panel.innerHTML = `
    <header class="af-settings-header">
      <h2>${t("settings.title")}</h2>
      <button type="button" class="af-settings-close" aria-label="${t("settings.close")}">×</button>
    </header>
    <label class="af-settings-field">
      <span>${t("settings.theme.label")}</span>
      <select data-settings-theme>
        <option value="system">${t("settings.theme.mode.system")}</option>
        <option value="light">${t("settings.theme.mode.light")}</option>
        <option value="dark">${t("settings.theme.mode.dark")}</option>
      </select>
    </label>
    <label class="af-settings-field gp-settings-toggle">
      <input type="checkbox" data-settings-update ${updateEnabled ? "checked" : ""} />
      <span>${t("settings.update_check.label")}</span>
    </label>
    <p class="af-settings-field">
      <a href="${VENMO_DONATE_URL}" target="_blank" rel="noopener noreferrer" data-settings-donate>${t("about.donate.venmo")}</a>
    </p>
    <fieldset class="af-settings-field" data-cache-retain>
      <legend>${t("settings.cache_retain.title")}</legend>
      <p>${t("settings.cache_retain.hint")}</p>
      <label><input type="radio" name="cache-retain" data-cache-retain-days /> ${t("settings.cache_retain.days")}</label>
      <label><input type="radio" name="cache-retain" data-cache-retain-sync /> ${t("settings.cache_retain.sync")}</label>
    </fieldset>
    <fieldset class="af-settings-field" data-news-network>
      <legend>${t("settings.news.network.title")}</legend>
      <p>${t("settings.news.network.hint")}</p>
      <label>
        <input type="radio" name="news-net" data-news-wifi-only ${wifiOnly ? "checked" : ""} />
        ${t("settings.news.wifi_only")}
      </label>
      <label>
        <input type="radio" name="news-net" data-news-allow-cellular ${wifiOnly ? "" : "checked"} />
        ${t("settings.news.allow_cellular")}
      </label>
    </fieldset>
  `;

  const themeSelect = panel.querySelector<HTMLSelectElement>("[data-settings-theme]");
  if (themeSelect) {
    themeSelect.value = themeMode;
    themeSelect.addEventListener("change", () => {
      applySettingsThemeMode(themeSelect.value as ThemeMode);
    });
  }

  panel
    .querySelector<HTMLInputElement>("[data-settings-update]")
    ?.addEventListener("change", (e) => {
      const checked = (e.target as HTMLInputElement).checked;
      setUpdateCheckEnabled(checked);
      callbacks.onUpdateCheckChange?.(checked);
    });

  panel.querySelector("[data-news-wifi-only]")?.addEventListener("change", () => {
    setNewsWifiOnly(true);
  });
  panel.querySelector("[data-news-allow-cellular]")?.addEventListener("change", () => {
    setNewsWifiOnly(false);
  });
  const retain = loadCacheRetainMode();
  const days = panel.querySelector<HTMLInputElement>("[data-cache-retain-days]");
  const sync = panel.querySelector<HTMLInputElement>("[data-cache-retain-sync]");
  if (days) days.checked = retain === "days30";
  if (sync) sync.checked = retain === "sync";
  days?.addEventListener("change", () => saveCacheRetainMode("days30"));
  sync?.addEventListener("change", () => saveCacheRetainMode("sync"));
  panel.querySelector(".af-settings-close")?.addEventListener("click", callbacks.onClose);
  panel.appendChild(createReaderImportPanel());
  panel.appendChild(createDriveSyncPanel());
  return panel;
}
