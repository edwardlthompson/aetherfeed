import { beforeEach, describe, expect, it, vi } from "vitest";
import type { AppShellCallbacks } from "./AppShell";
import { handleRestartGuard } from "./about/aboutSession";
import { decideLaunchPrompt } from "./about/runAppUpdates";
import { bootstrapApp } from "./appBootstrap";
import en from "./locales/en.json";
import { webAppLock } from "./lock";

const messages = en as Record<string, string>;

vi.mock("./AppShell", () => ({
  createAppShell: vi.fn(),
}));

vi.mock("./about/aboutSession", () => ({
  handleRestartGuard: vi.fn(() => false),
  APP_VERSION: "0.1.0",
}));

vi.mock("./about/runAppUpdates", () => ({
  decideLaunchPrompt: vi.fn(() => Promise.resolve(null)),
}));

vi.mock("./about/updatePrefs", () => ({
  markUpdateChecked: vi.fn(),
  markVersionSeen: vi.fn(),
}));

vi.mock("./settings/preferences", () => ({
  isUpdateCheckEnabled: vi.fn(() => true),
}));

vi.mock("./about/donations", () => ({
  loadDonations: vi.fn(() => Promise.resolve({ enabled: true, message: "thanks", links: [] })),
}));

vi.mock("./theme", () => ({
  initTheme: vi.fn(),
  subscribeThemeChange: vi.fn(),
}));

vi.mock("./i18n", () => ({
  t: vi.fn((key: string) => messages[key] ?? key),
}));

vi.mock("./about/applyUpdate", () => ({
  applyPwaUpdate: vi.fn(() => Promise.resolve(true)),
}));

vi.mock("./sync/feedSourceSync", () => ({
  syncFeedSourcesIfConnected: vi.fn(() => Promise.resolve(null)),
}));

vi.mock("./readerimport/importedStore", () => ({
  hydrateImportedFeeds: vi.fn(() => Promise.resolve()),
  loadImportedFeeds: vi.fn(() => []),
}));

vi.mock("./readerimport/seedImport", () => ({
  seedImportIfEmpty: vi.fn(() => Promise.resolve(0)),
}));

import { createAppShell } from "./AppShell";
import { applyPwaUpdate } from "./about/applyUpdate";

const mockedCreateAppShell = vi.mocked(createAppShell);
const mockedDecideLaunchPrompt = vi.mocked(decideLaunchPrompt);
const mockedApplyPwaUpdate = vi.mocked(applyPwaUpdate);

describe("bootstrapApp", () => {
  let handlers: AppShellCallbacks | undefined;

  function requireHandlers(): AppShellCallbacks {
    if (!handlers) {
      throw new Error("App shell handlers were not captured");
    }
    return handlers;
  }

  beforeEach(async () => {
    await webAppLock.wipe();
    await webAppLock.setSecret("123456", "pin");
    vi.clearAllMocks();
    handlers = undefined;
    mockedCreateAppShell.mockImplementation((_root, _state, h) => {
      handlers = h;
    });
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: { register: vi.fn(() => Promise.resolve()) },
    });
  });

  it("hydrates then mounts the shell after first-run PIN", async () => {
    await webAppLock.wipe();
    const root = document.createElement("div");
    bootstrapApp(root);
    const input = root.querySelector<HTMLInputElement>("[data-lock-secret]");
    if (input) input.value = "123456";
    root
      .querySelector("form")
      ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(mockedCreateAppShell).toHaveBeenCalled());
  });

  it("swallows hydrate failures", async () => {
    const store = await import("./readerimport/importedStore");
    vi.mocked(store.hydrateImportedFeeds).mockRejectedValueOnce(new Error("hydrate"));
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(mockedCreateAppShell).toHaveBeenCalled());
  });

  it("shows the unlock pane when the vault is unset", async () => {
    await webAppLock.wipe();
    const root = document.createElement("div");
    bootstrapApp(root);
    expect(root.querySelector("[data-lock-form]")).toBeTruthy();
    expect(mockedCreateAppShell).not.toHaveBeenCalled();
  });

  it("locks after a background timeout", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    const t0 = 5_000_000;
    const now = vi.spyOn(Date, "now").mockReturnValue(t0);
    Object.defineProperty(document, "hidden", { configurable: true, get: () => true });
    document.dispatchEvent(new Event("visibilitychange"));
    now.mockReturnValue(t0 + 130_000);
    Object.defineProperty(document, "hidden", { configurable: true, get: () => false });
    document.dispatchEvent(new Event("visibilitychange"));
    expect(webAppLock.state()).toBe("locked");
    expect(root.querySelector("[data-lock-form]")).toBeTruthy();
    now.mockRestore();
    Object.defineProperty(document, "hidden", { configurable: true, get: () => false });
  });

  it("puts the subscription count in the document title", async () => {
    const store = await import("./readerimport/importedStore");
    vi.mocked(store.loadImportedFeeds).mockReturnValueOnce([
      {
        id: "feed:https://example.invalid/rss.xml",
        title: "Local",
        url: "https://example.invalid/rss.xml",
        kind: "news",
        updatedAt: 1,
      },
    ]);
    const root = document.createElement("div");
    bootstrapApp(root);
    expect(document.title).toContain("1 subscriptions");
  });

  it("renders app shell on bootstrap", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => {
      expect(mockedCreateAppShell).toHaveBeenCalledWith(
        root,
        expect.objectContaining({
          updateStatus: messages["about.update.current"],
        }),
        expect.any(Object),
      );
    });
  });

  it("renders immediately before donations load completes", async () => {
    const donationsMod = await import("./about/donations");
    vi.mocked(donationsMod.loadDonations).mockImplementation(() => new Promise(() => {}));
    const root = document.createElement("div");
    mockedCreateAppShell.mockClear();
    bootstrapApp(root);
    expect(mockedCreateAppShell).toHaveBeenCalled();
  });

  it("re-renders when shell state changes", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    requireHandlers().onState({ showAbout: true });
    expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore);
  });

  it("refreshes update status when check toggle enabled", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    mockedDecideLaunchPrompt.mockResolvedValueOnce({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    requireHandlers().onUpdateCheckChange?.(true);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(
          ([, state]) =>
            state.updateStatus === `${messages["about.update.available"]}: 99.0.0` &&
            state.launchPrompt?.kind === "update",
        ),
      ).toBe(true),
    );
  });

  it("registers service worker on load", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    window.dispatchEvent(new Event("load"));
    await vi.waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalledWith("/sw.js");
    });
  });

  it("skips background update check when restart guard is active", async () => {
    vi.mocked(handleRestartGuard).mockReturnValueOnce(true);
    mockedDecideLaunchPrompt.mockClear();
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    expect(mockedDecideLaunchPrompt).not.toHaveBeenCalled();
  });

  it("ignores disabled update-check toggle", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedDecideLaunchPrompt.mock.calls.length;
    requireHandlers().onUpdateCheckChange?.(false);
    expect(mockedDecideLaunchPrompt.mock.calls.length).toBe(callsBefore);
  });

  it("re-renders about panel when background update completes while open", async () => {
    let resolveCheck: (value: Awaited<ReturnType<typeof decideLaunchPrompt>>) => void = () => {};
    mockedDecideLaunchPrompt.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveCheck = resolve;
        }),
    );
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onState({ showAbout: true });
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    resolveCheck({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    await vi.waitFor(() =>
      expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore),
    );
  });

  it("shows a launch prompt when a newer installer is found", async () => {
    let resolveCheck: (value: Awaited<ReturnType<typeof decideLaunchPrompt>>) => void = () => {};
    mockedDecideLaunchPrompt.mockImplementation(
      () =>
        new Promise((resolve) => {
          resolveCheck = resolve;
        }),
    );
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    const callsBefore = mockedCreateAppShell.mock.calls.length;
    resolveCheck({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    await vi.waitFor(() =>
      expect(mockedCreateAppShell.mock.calls.length).toBeGreaterThan(callsBefore),
    );
    expect(
      mockedCreateAppShell.mock.calls.some(
        ([, state]) =>
          state.updateStatus === `${messages["about.update.available"]}: 99.0.0` &&
          state.launchPrompt?.kind === "update",
      ),
    ).toBe(true);
  });

  it("exposes an update prompt when a newer installer is reported", async () => {
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    mockedDecideLaunchPrompt.mockResolvedValueOnce({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    requireHandlers().onUpdateCheckChange?.(true);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, state]) => state.launchPrompt?.kind === "update"),
      ).toBe(true),
    );
  });

  it("records donate seen and opens Venmo", async () => {
    mockedDecideLaunchPrompt.mockResolvedValue({ kind: "donate" });
    const open = vi.spyOn(window, "open").mockReturnValue(null);
    const prefs = await import("./about/updatePrefs");
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, next]) => next.launchPrompt?.kind === "donate"),
      ).toBe(true),
    );
    requireHandlers().onLaunchAction?.("donate");
    expect(prefs.markVersionSeen).toHaveBeenCalledWith("0.1.0");
    expect(open).toHaveBeenCalled();
    open.mockRestore();
  });

  it("silences an installer version on Later", async () => {
    mockedDecideLaunchPrompt.mockResolvedValue({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    const prefs = await import("./about/updatePrefs");
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, next]) => next.launchPrompt?.kind === "update"),
      ).toBe(true),
    );
    requireHandlers().onLaunchAction?.("later");
    expect(prefs.markUpdateChecked).toHaveBeenCalledWith(expect.any(Number), "99.0.0");
  });

  it("opens the installer URL on Install", async () => {
    mockedDecideLaunchPrompt.mockResolvedValue({
      kind: "update",
      version: "99.0.0",
      url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
    });
    const open = vi.spyOn(window, "open").mockReturnValue(null);
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(([, next]) => next.launchPrompt?.kind === "update"),
      ).toBe(true),
    );
    requireHandlers().onLaunchAction?.("install");
    expect(open).toHaveBeenCalledWith(
      "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
      "_blank",
      "noopener,noreferrer",
    );
    open.mockRestore();
  });

  it("applies PWA update through service worker registration", async () => {
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).toHaveBeenCalledWith(registration));
  });

  it("shows restarting status after apply succeeds", async () => {
    mockedApplyPwaUpdate.mockResolvedValueOnce(true);
    const registration = { waiting: {} } as ServiceWorkerRegistration;
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(registration)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() =>
      expect(
        mockedCreateAppShell.mock.calls.some(
          ([, state]) => state.updateStatus === messages["about.update.restarting"],
        ),
      ).toBe(true),
    );
  });

  it("no-ops apply when service worker registration is missing", async () => {
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: {
        register: vi.fn(() => Promise.resolve()),
        getRegistration: vi.fn(() => Promise.resolve(undefined)),
      },
    });
    const root = document.createElement("div");
    bootstrapApp(root);
    await vi.waitFor(() => expect(handlers).toBeDefined());
    requireHandlers().onApplyUpdate?.();
    await vi.waitFor(() => expect(mockedApplyPwaUpdate).not.toHaveBeenCalled());
  });
});
