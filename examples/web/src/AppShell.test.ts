import { describe, expect, it } from "vitest";
import { type AppShellState, createAppShell } from "./AppShell";

function state(partial: Partial<AppShellState> = {}): AppShellState {
  return {
    showAbout: false,
    showSettings: false,
    updateStatus: "You are on the latest version.",
    donations: { enabled: false, message: "", links: [] },
    mode: "news",
    ...partial,
  };
}

describe("createAppShell", () => {
  it("renders the library tree, action bar, and refresh without a mode rail", () => {
    const root = document.createElement("div");
    createAppShell(root, state(), { onState: () => undefined });
    expect(root.querySelectorAll("[data-mode]")).toHaveLength(0);
    expect(root.querySelector("[data-library-pick='unified']")).not.toBeNull();
    expect(root.querySelector("[data-library-pick='news']")).not.toBeNull();
    expect(root.querySelector("[data-news-pane]")).not.toBeNull();
    expect(root.querySelector("[data-news-folders]")).not.toBeNull();
    expect(root.querySelector("[data-news-timeline]")).not.toBeNull();
    expect(root.querySelector("[data-news-reader]")).not.toBeNull();
    expect(root.querySelector("[data-news-refresh]")).not.toBeNull();
    expect(root.querySelector("[data-action-bar]")).not.toBeNull();
    expect(root.querySelector("[data-action-prev]")).not.toBeNull();
    expect(root.querySelector("[data-action-share]")).not.toBeNull();
    expect(root.querySelector("[data-nav-unread='news']")).not.toBeNull();
    expect(root.querySelector("[data-unified-unread]")).not.toBeNull();
    expect(root.querySelector("[data-readerimport-mount]")).toBeNull();
    expect(root.querySelector("[data-testid='search-panel']")).toBeNull();
    expect(root.querySelector("main.af-shell")?.classList.contains("af-chrome")).toBe(true);
    expect(root.querySelectorAll("[data-chrome-slot='rail']")).toHaveLength(1);
    expect(root.querySelector("[data-chrome-slot='list'] [data-mode-mount]")).not.toBeNull();
  });

  it("mounts podcasts pane and player fx in podcast mode", () => {
    const root = document.createElement("div");
    createAppShell(root, state({ mode: "podcast" }), { onState: () => undefined });
    expect(root.querySelector("[data-testid='podcasts-pane']")).not.toBeNull();
    expect(root.querySelector("[data-testid='playerfx']")).not.toBeNull();
    expect(root.querySelector("[data-testid='downloads-panel']")).not.toBeNull();
    expect(root.querySelector("[data-testid='rules-panel']")).toBeNull();
  });

  it("requests podcast mode when Podcasts is clicked", () => {
    const root = document.createElement("div");
    const patches: Partial<AppShellState>[] = [];
    createAppShell(root, state(), { onState: (next) => patches.push(next) });
    root.querySelector<HTMLButtonElement>("[data-library-pick='podcast']")?.click();
    expect(patches.at(-1)?.mode).toBe("podcast");
  });

  it("puts subscription import in Settings", () => {
    const root = document.createElement("div");
    createAppShell(root, state({ showSettings: true }), { onState: () => undefined });
    expect(root.querySelector("[data-testid='readerimport-panel']")).not.toBeNull();
    expect(root.querySelector("[data-news-refresh]")).not.toBeNull();
  });

  it("mounts a donate nudge without an update dialog", () => {
    const root = document.createElement("div");
    const actions: string[] = [];
    createAppShell(root, state({ launchPrompt: { kind: "donate" } }), {
      onState: () => undefined,
      onLaunchAction: (action) => actions.push(action),
    });
    expect(root.querySelector("[data-testid='donate-nudge']")).not.toBeNull();
    expect(root.querySelector("[data-testid='update-prompt']")).toBeNull();
    root.querySelector<HTMLButtonElement>("[data-testid='donate-not-now']")?.click();
    expect(actions).toEqual(["not-now"]);
  });

  it("shows designed Boards empty when mode is booru", () => {
    const root = document.createElement("div");
    createAppShell(root, state({ mode: "booru" }), { onState: () => undefined });
    expect(root.querySelector("[data-boards-empty]")).not.toBeNull();
    expect(root.querySelector("[data-nav-unread='booru']")).not.toBeNull();
  });
});
