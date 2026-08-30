import { type AppShellState, createAppShell } from "./AppShell";
import { APP_VERSION, handleRestartGuard } from "./about/aboutSession";
import { applyPwaUpdate } from "./about/applyUpdate";
import { VENMO_DONATE_URL } from "./about/donate";
import { loadDonations } from "./about/donations";
import { decideLaunchPrompt } from "./about/runAppUpdates";
import { markUpdateChecked, markVersionSeen } from "./about/updatePrefs";
import { assetUrl } from "./assetUrl";
import { t } from "./i18n";
import { createUnlockPane, subscribeLockChange, watchBackgroundLock, webAppLock } from "./lock";
import { hydrateVault } from "./reader/vault";
import { hydrateImportedFeeds, loadImportedFeeds } from "./readerimport/importedStore";
import { isUpdateCheckEnabled } from "./settings/preferences";
import { loadAppMode, saveAppMode } from "./shell/modePrefs";
import { bindPrivacyCover } from "./shell/privacyCover";
import { syncFeedSourcesIfConnected } from "./sync/feedSourceSync";
import { initTheme, subscribeThemeChange } from "./theme";

function isUpdateAvailableStatus(status: string): boolean {
  return status.startsWith(t("about.update.available"));
}

export function bootstrapApp(appRoot: HTMLDivElement): void {
  let state: AppShellState = {
    showAbout: false,
    showSettings: false,
    updateStatus: t("about.update.current"),
    donations: { enabled: false, message: "", links: [] },
    mode: loadAppMode(),
    launchPrompt: null,
  };

  async function runLaunchPolicy(): Promise<void> {
    const prompt = await decideLaunchPrompt(
      APP_VERSION,
      Date.now(),
      undefined,
      isUpdateCheckEnabled(),
    );
    if (!prompt) return;
    state = {
      ...state,
      launchPrompt: prompt,
      updateStatus:
        prompt.kind === "update"
          ? `${t("about.update.available")}: ${prompt.version}`
          : state.updateStatus,
    };
    render();
  }

  async function handleApplyUpdate(): Promise<void> {
    if (!("serviceWorker" in navigator)) return;
    const registration = await navigator.serviceWorker.getRegistration();
    if (!registration) return;
    const applied = await applyPwaUpdate(registration);
    if (applied) {
      state = { ...state, updateStatus: t("about.update.restarting") };
      render();
    }
  }

  function syncWindowTitle(): void {
    const count = loadImportedFeeds().length;
    document.title = count
      ? `${t("app.title")} \u2014 ${t("readerimport.library.count").replace("{count}", String(count))}`
      : t("app.title");
  }

  function render(): void {
    if (webAppLock.state() !== "unlocked") {
      createUnlockPane(appRoot, () => {
        void Promise.all([hydrateImportedFeeds(), hydrateVault()]).then(() => render());
      });
      return;
    }
    createAppShell(appRoot, state, {
      onState: (patch) => {
        state = { ...state, ...patch };
        if (patch.mode) saveAppMode(patch.mode);
        render();
      },
      onUpdateCheckChange: (enabled) => {
        if (enabled) void runLaunchPolicy();
      },
      onLaunchAction: (action) => {
        const prompt = state.launchPrompt;
        if (prompt?.kind === "donate") {
          markVersionSeen(APP_VERSION);
          if (action === "donate") {
            window.open(VENMO_DONATE_URL, "_blank", "noopener,noreferrer");
          }
        } else if (prompt?.kind === "update") {
          markUpdateChecked(Date.now(), action === "later" ? prompt.version : undefined);
          if (action === "install") {
            window.open(prompt.url, "_blank", "noopener,noreferrer");
          }
        }
        state = { ...state, launchPrompt: null };
        render();
      },
      onApplyUpdate: () => {
        void handleApplyUpdate();
      },
      canApplyUpdate: isUpdateAvailableStatus(state.updateStatus),
    });
    syncWindowTitle();
  }

  initTheme();
  subscribeThemeChange(() => render());
  render();
  void hydrateImportedFeeds()
    .catch(() => undefined)
    .then(() => render());
  void syncFeedSourcesIfConnected();
  void loadDonations().then((d) => {
    state = { ...state, donations: d };
    render();
  });

  if (!handleRestartGuard()) {
    void runLaunchPolicy();
  }

  watchBackgroundLock(() => render());
  bindPrivacyCover();
  subscribeLockChange(() => render());
  window.addEventListener("online", render);
  window.addEventListener("offline", render);

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register(assetUrl("sw.js")).catch(() => {});
    });
  }
}
