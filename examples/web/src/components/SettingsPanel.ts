import { t } from "../i18n";
import {
  applySettingsThemeMode,
  getSettingsThemeMode,
  isUpdateCheckEnabled,
  setUpdateCheckEnabled,
} from "../settings/preferences";
import { appendReaderSettings } from "../settings/readerSettingsUi";
import {
  LAN_CLIENT_PASS_KEY,
  LAN_CLIENT_URL_KEY,
  saveLanClientPrefs,
} from "../sync/lanClientPrefs";
import { type LanHubStatus, lanHubStart, lanHubStatus, lanHubStop } from "../sync/lanHub";
import { syncLanPool } from "../sync/lanPoolClient";
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
  const savedUrl = localStorage.getItem(LAN_CLIENT_URL_KEY) || "http://127.0.0.1:8787";
  const savedPass = localStorage.getItem(LAN_CLIENT_PASS_KEY) || "";

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
    <div class="af-settings-field" data-lan-hub>
      <strong>${t("settings.lan_hub.title")}</strong>
      <p class="af-settings-hint">${t("settings.lan_hub.hint")}</p>
      <label>
        <span>${t("settings.lan_hub.passphrase")}</span>
        <input type="password" data-lan-passphrase autocomplete="off" value="${savedPass.replace(/"/g, "&quot;")}" />
      </label>
      <div class="af-settings-actions">
        <button type="button" data-lan-start>${t("settings.lan_hub.start")}</button>
        <button type="button" data-lan-stop>${t("settings.lan_hub.stop")}</button>
      </div>
      <p data-lan-urls aria-live="polite">${t("settings.lan_hub.urls")}: —</p>
      <p class="af-settings-hint">${t("settings.lan_hub.client_hint")}</p>
      <label>
        <span>${t("settings.lan_hub.client_url")}</span>
        <input type="url" data-lan-client-url value="${savedUrl.replace(/"/g, "&quot;")}" />
      </label>
      <button type="button" data-lan-sync>${t("settings.lan_hub.sync")}</button>
      <p data-lan-status aria-live="polite"></p>
    </div>
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

  const urlsEl = panel.querySelector<HTMLElement>("[data-lan-urls]");
  const statusEl = panel.querySelector<HTMLElement>("[data-lan-status]");
  const passEl = panel.querySelector<HTMLInputElement>("[data-lan-passphrase]");
  const clientUrlEl = panel.querySelector<HTMLInputElement>("[data-lan-client-url]");

  const renderHub = (status: LanHubStatus | null) => {
    if (!urlsEl || !statusEl) return;
    if (!status) {
      return;
    }
    urlsEl.textContent = `${t("settings.lan_hub.urls")}: ${status.lanUrls.join(" · ") || "—"}`;
    if (status.running && clientUrlEl && !clientUrlEl.value.trim()) {
      clientUrlEl.value =
        status.lanUrls.find((u) => u.includes("127.0.0.1")) || status.lanUrls[0] || "";
    }
    statusEl.textContent = status.running
      ? `Hub running on port ${status.port} (etag ${status.etag})`
      : statusEl.textContent || "Hub stopped";
  };

  void lanHubStatus()
    .then(renderHub)
    .catch(() => renderHub(null));

  panel.querySelector("[data-lan-start]")?.addEventListener("click", () => {
    const passphrase = passEl?.value ?? "";
    saveLanClientPrefs(clientUrlEl?.value ?? "", passphrase);
    void lanHubStart(passphrase)
      .then((status) => {
        renderHub(status);
        if (clientUrlEl && status.lanUrls.length) {
          clientUrlEl.value =
            status.lanUrls.find((u) => u.includes("127.0.0.1")) || status.lanUrls[0];
          saveLanClientPrefs(clientUrlEl.value, passphrase);
        }
      })
      .catch((err: unknown) => {
        if (statusEl) statusEl.textContent = String(err);
      });
  });

  panel.querySelector("[data-lan-stop]")?.addEventListener("click", () => {
    void lanHubStop()
      .then(renderHub)
      .catch((err: unknown) => {
        if (statusEl) statusEl.textContent = String(err);
      });
  });

  panel.querySelector("[data-lan-sync]")?.addEventListener("click", () => {
    const passphrase = passEl?.value ?? "";
    const hubUrl = clientUrlEl?.value ?? "";
    saveLanClientPrefs(hubUrl, passphrase);
    void syncLanPool(hubUrl, passphrase)
      .then((doc) => {
        if (statusEl) {
          statusEl.textContent = `Synced (${doc.subs.length} feeds) — use the same URL/pass on phone & tablet`;
        }
      })
      .catch((err: unknown) => {
        if (statusEl) statusEl.textContent = String(err);
      });
  });

  appendReaderSettings(panel);
  panel.querySelector(".af-settings-close")?.addEventListener("click", callbacks.onClose);
  return panel;
}
