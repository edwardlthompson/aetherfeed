import { DRIVE_LOOPBACK_PORT } from "@aetherfeed/domain";
import { t } from "../i18n";
import { sweepArticleCache } from "../news/cacheRetain";
import { buildAuthUrl, createPkceVerifier } from "../sync/driveAuth";
import {
  ensureSyncPassphrase,
  getDriveClientId,
  getDriveClientSecret,
  isDriveConnected,
  setDriveClientId,
  setDriveClientSecret,
  setSyncPassphrase,
} from "../sync/driveStore";
import { completeDriveConnect, disconnectDrive, syncFeedSources } from "../sync/feedSourceSync";
import { waitForOauthCode } from "../sync/tauriInvoke";

export function createDriveSyncPanel(): HTMLElement {
  const panel = document.createElement("section");
  panel.className = "af-drive-sync";
  panel.dataset.testid = "drive-sync-panel";
  const passphrase = ensureSyncPassphrase();
  panel.innerHTML = `
    <h3>${t("settings.drive.title")}</h3>
    <p>${t("settings.drive.hint")}</p>
    <label class="af-settings-field af-drive-field">
      <span>${t("settings.drive.client_id")}</span>
      <input type="text" data-drive-client-id autocomplete="off" />
    </label>
    <label class="af-settings-field af-drive-field">
      <span>${t("settings.drive.client_secret")}</span>
      <input type="password" data-drive-client-secret autocomplete="off" />
    </label>
    <label class="af-settings-field af-drive-field">
      <span>${t("settings.drive.passphrase")}</span>
      <input type="text" data-drive-passphrase autocomplete="off" />
    </label>
    <div class="af-drive-actions">
      <button type="button" data-drive-connect>${t("settings.drive.connect")}</button>
      <button type="button" data-drive-sync>${t("settings.drive.sync")}</button>
      <button type="button" data-drive-disconnect>${t("settings.drive.disconnect")}</button>
    </div>
    <p class="af-drive-status" data-drive-status aria-live="polite"></p>
  `;
  const clientInput = panel.querySelector<HTMLInputElement>("[data-drive-client-id]");
  const secretInput = panel.querySelector<HTMLInputElement>("[data-drive-client-secret]");
  const passInput = panel.querySelector<HTMLInputElement>("[data-drive-passphrase]");
  const status = panel.querySelector<HTMLElement>("[data-drive-status]");
  if (clientInput) clientInput.value = getDriveClientId();
  if (secretInput) secretInput.value = getDriveClientSecret();
  if (passInput) passInput.value = passphrase;
  const persist = () => {
    if (clientInput) setDriveClientId(clientInput.value);
    if (secretInput) setDriveClientSecret(secretInput.value);
    if (passInput) setSyncPassphrase(passInput.value);
  };
  const setStatus = (text: string) => {
    if (status) status.textContent = text;
  };
  setStatus(isDriveConnected() ? t("settings.drive.connected") : t("settings.drive.disconnected"));
  panel.querySelector("[data-drive-connect]")?.addEventListener("click", () => {
    persist();
    void (async () => {
      try {
        const verifier = createPkceVerifier();
        const url = await buildAuthUrl(getDriveClientId(), verifier);
        const wait = waitForOauthCode(DRIVE_LOOPBACK_PORT);
        window.open(url, "_blank", "noopener");
        await completeDriveConnect(await wait, verifier);
        setStatus(t("settings.drive.connected"));
      } catch (err) {
        setStatus(err instanceof Error ? err.message : t("settings.drive.sync_fail"));
      }
    })();
  });
  panel.querySelector("[data-drive-sync]")?.addEventListener("click", () => {
    persist();
    void (async () => {
      try {
        const result = await syncFeedSources();
        sweepArticleCache(true);
        setStatus(
          t("settings.drive.sync_ok")
            .replace("{count}", String(result.count))
            .replace("{pulled}", String(result.pulled)),
        );
      } catch (err) {
        setStatus(err instanceof Error ? err.message : t("settings.drive.sync_fail"));
      }
    })();
  });
  panel.querySelector("[data-drive-disconnect]")?.addEventListener("click", () => {
    disconnectDrive();
    setStatus(t("settings.drive.disconnected"));
  });
  return panel;
}
