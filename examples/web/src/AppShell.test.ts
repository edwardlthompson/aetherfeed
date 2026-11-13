import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { type AppShellState, createAppShell } from "./AppShell";
import en from "./locales/en.json";

const messages = en as Record<string, string>;

function baseState(overrides: Partial<AppShellState> = {}): AppShellState {
  return {
    showAbout: false,
    showSettings: false,
    updateStatus: messages["about.update.current"],
    donations: { enabled: false, message: "", links: [] },
    activeModule: "news",
    ...overrides,
  };
}

describe("createAppShell module tabs", () => {
  let root: HTMLDivElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("mounts news chrome by default", () => {
    createAppShell(root, baseState(), { onState: () => {} });
    expect(root.querySelector("[data-testid='news-pane']")).not.toBeNull();
    expect(root.querySelector("[data-booru-mount]")?.hasAttribute("hidden")).toBe(true);
  });

  it("mounts booru grid chrome when activeModule is booru", () => {
    createAppShell(root, baseState({ activeModule: "booru" }), { onState: () => {} });
    expect(root.querySelector("[data-testid='booru-pane']")).not.toBeNull();
    expect(root.querySelector("[data-news-mount]")?.hasAttribute("hidden")).toBe(true);
  });

  it("shows a podcasts tab", () => {
    createAppShell(root, baseState(), { onState: () => {} });
    expect(root.querySelector('[data-module-tab="podcasts"]')?.textContent).toBe(
      messages["nav.podcasts"],
    );
  });

  it("mounts podcasts pane when activeModule is podcasts", () => {
    createAppShell(root, baseState({ activeModule: "podcasts" }), { onState: () => {} });
    expect(root.querySelector("[data-testid='podcasts-pane']")).not.toBeNull();
    expect(root.querySelector("[data-news-mount]")?.hasAttribute("hidden")).toBe(true);
  });

  it("requests module change when booru tab is clicked", () => {
    const patches: Partial<AppShellState>[] = [];
    createAppShell(root, baseState(), {
      onState: (next) => patches.push(next),
    });
    root.querySelector<HTMLButtonElement>('[data-module-tab="booru"]')?.click();
    expect(patches).toContainEqual({ activeModule: "booru" });
  });

  it("requests podcasts when that tab is clicked", () => {
    const patches: Partial<AppShellState>[] = [];
    createAppShell(root, baseState(), {
      onState: (next) => patches.push(next),
    });
    root.querySelector<HTMLButtonElement>('[data-module-tab="podcasts"]')?.click();
    expect(patches).toContainEqual({ activeModule: "podcasts" });
  });
});
