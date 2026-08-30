import { modeBody, mountModePanes } from "./AppShellChrome";
import type { AppMode, AppShellCallbacks, AppShellState } from "./AppShellTypes";
import { APP_VERSION } from "./about/aboutSession";
import { createAboutPanel } from "./components/AboutPanel";
import { createLaunchPromptDialog } from "./components/AppUpdateDialogs";
import { createSettingsPanel } from "./components/SettingsPanel";
import { createThemeToggle } from "./components/ThemeToggle";
import { isOnline } from "./greet";
import { t } from "./i18n";
import { bindPanelDialog } from "./panelDialog";
import { applyChromeLayout } from "./shell";
import { actionBarHtml, bindActionBar } from "./shell/actionBar";

export type { AppMode, AppShellCallbacks, AppShellState } from "./AppShellTypes";

let dialogCleanup: (() => void) | undefined;

export function createAppShell(
  root: HTMLElement,
  state: AppShellState,
  callbacks: AppShellCallbacks,
): void {
  const statusKey = isOnline() ? "app.status.online" : "app.status.offline";

  root.innerHTML = `
    <main class="af-shell">
      <div class="af-header">
        <div class="af-header-copy">
          <h1 class="af-title">${t("app.title")}</h1>
          <p class="af-header-status" data-testid="status">${t(statusKey)}</p>
        </div>
        <div class="af-header-actions">
          <button type="button" class="af-news-action" data-news-refresh aria-label="${t("news.refresh")}">${t("news.refresh")}</button>
          <button type="button" class="af-settings-btn" data-settings-open aria-label="${t("settings.open")}">⚙</button>
          <button type="button" class="af-about-btn" data-about-open aria-label="${t("about.open")}">i</button>
        </div>
      </div>
      ${state.showAbout || state.showSettings ? "" : actionBarHtml(state.mode)}
      <section class="af-mode-mount" data-mode-mount>${modeBody(state.mode)}</section>
      <div data-panel-mount></div>
    </main>
  `;

  const actions = root.querySelector<HTMLDivElement>(".af-header-actions");
  if (actions) actions.insertBefore(createThemeToggle(), actions.firstChild);

  root.querySelectorAll<HTMLButtonElement>("[data-mode]").forEach((button) => {
    button.addEventListener("click", () => {
      const mode = button.dataset.mode as AppMode | undefined;
      if (mode) callbacks.onState({ mode, showAbout: false, showSettings: false });
    });
  });
  root.querySelector("[data-about-open]")?.addEventListener("click", () => {
    callbacks.onState({ showAbout: !state.showAbout, showSettings: false });
  });
  root.querySelector("[data-settings-open]")?.addEventListener("click", () => {
    callbacks.onState({ showSettings: !state.showSettings, showAbout: false });
  });
  root.querySelector("[data-news-refresh]")?.addEventListener("click", () => {
    window.dispatchEvent(new Event("af-news-refresh"));
  });
  bindActionBar(root);
  window.addEventListener("af-library-pick", ((event: Event) => {
    const detail = (event as CustomEvent<string>).detail;
    if (detail === "podcast" || detail === "booru") {
      callbacks.onState({ mode: detail, showAbout: false, showSettings: false });
    } else {
      callbacks.onState({ mode: "news", showAbout: false, showSettings: false });
    }
  }) as EventListener);

  root.querySelector(".af-mode-nav")?.setAttribute("data-chrome-slot", "rail");
  const chrome = applyChromeLayout(root, {
    mode: state.mode,
    onMode: (mode) => callbacks.onState({ mode, showAbout: false, showSettings: false }),
  });
  const modeMount = root.querySelector("[data-mode-mount]");
  if (modeMount) chrome.slots.list.append(modeMount);
  mountModePanes(root, state.showAbout || state.showSettings);

  const mount = root.querySelector("[data-panel-mount]");
  if (!mount) return;
  dialogCleanup?.();
  dialogCleanup = undefined;
  mount.innerHTML = "";

  if (state.showSettings) {
    const panel = createSettingsPanel({
      onClose: () => callbacks.onState({ showSettings: false }),
      onUpdateCheckChange: callbacks.onUpdateCheckChange,
    });
    mount.appendChild(panel);
    dialogCleanup = bindPanelDialog(panel, () => callbacks.onState({ showSettings: false }));
  } else if (state.showAbout) {
    mount.appendChild(
      createAboutPanel(
        {
          version: APP_VERSION,
          updateStatus: state.updateStatus,
          donations: state.donations,
          canApplyUpdate: callbacks.canApplyUpdate,
        },
        () => callbacks.onState({ showAbout: false }),
        callbacks.onApplyUpdate,
      ),
    );
    dialogCleanup = bindPanelDialog(mount.lastElementChild as HTMLElement, () =>
      callbacks.onState({ showAbout: false }),
    );
  }
  if (state.launchPrompt) {
    root.appendChild(
      createLaunchPromptDialog(state.launchPrompt, (action) => {
        callbacks.onLaunchAction?.(action);
      }),
    );
  }
}
