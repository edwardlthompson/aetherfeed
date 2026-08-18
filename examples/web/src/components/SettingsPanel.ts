import { t } from "../i18n";
import {
  applySettingsThemeMode,
  getSettingsThemeMode,
  isUpdateCheckEnabled,
  setUpdateCheckEnabled,
} from "../settings/preferences";
import type { ThemeMode } from "../theme";

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

  panel.querySelector(".af-settings-close")?.addEventListener("click", callbacks.onClose);
  return panel;
}
