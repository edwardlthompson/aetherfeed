import { describe, expect, it } from "vitest";
import {
  APP_MODES,
  applyChromeLayout,
  createModeRail,
  isWideViewport,
  WIDE_MIN_REM,
} from "./AppChrome";

const WIDE_PX = WIDE_MIN_REM * 16;
const NARROW_PX = WIDE_PX - 1;

describe("isWideViewport", () => {
  it("treats 48rem as the wide breakpoint", () => {
    expect(isWideViewport(WIDE_PX)).toBe(true);
    expect(isWideViewport(NARROW_PX)).toBe(false);
    expect(isWideViewport(0, 16)).toBe(false);
  });
});

describe("createModeRail", () => {
  it("renders three modes with the active current", () => {
    const rail = createModeRail("podcast");
    const modes = [...rail.querySelectorAll("[data-mode]")].map((el) =>
      el.getAttribute("data-mode"),
    );
    expect(modes).toEqual(["news", "podcast", "booru"]);
    expect(rail.querySelector("[data-mode='podcast']")?.getAttribute("aria-current")).toBe("page");
    expect(
      rail.classList.contains("af-shell-nav-narrow") || rail.classList.contains("af-shell-rail"),
    ).toBe(true);
  });
});

describe("applyChromeLayout", () => {
  it("adds af-shell-wide and rail/list/detail slots at 48rem", () => {
    const root = document.createElement("div");
    const layout = applyChromeLayout(root, { viewportWidth: WIDE_PX, mode: "news" });
    const shell = root.querySelector("main.af-shell");
    expect(layout.wide).toBe(true);
    expect(shell?.classList.contains("af-shell-wide")).toBe(true);
    expect(layout.slots.rail.classList.contains("af-shell-rail")).toBe(true);
    expect(layout.slots.list.dataset.chromeSlot).toBe("list");
    expect(layout.slots.detail.classList.contains("af-shell-detail")).toBe(true);
    expect(shell?.querySelectorAll("[data-mode]")).toHaveLength(0);
  });

  it("uses one-column narrow nav below 48rem", () => {
    const root = document.createElement("div");
    const layout = applyChromeLayout(root, { viewportWidth: NARROW_PX, mode: "booru" });
    const shell = root.querySelector("main.af-shell");
    expect(layout.wide).toBe(false);
    expect(shell?.classList.contains("af-shell-wide")).toBe(false);
    expect(shell?.classList.contains("af-shell-narrow")).toBe(true);
    expect(layout.slots.rail.classList.contains("af-shell-nav-narrow")).toBe(true);
    expect(layout.slots.rail.querySelector("[data-mode]")).toBeNull();
  });

  it("keeps heading and status without a left-rail greeting", () => {
    const root = document.createElement("div");
    applyChromeLayout(root, { viewportWidth: WIDE_PX });
    expect(root.querySelector("h1")?.textContent).toBe("AetherFeed");
    expect(root.querySelector(".af-headline")).toBeNull();
    expect(root.querySelector("[data-testid='status']")?.textContent).toContain("AetherFeed");
  });

  it("preserves existing AppShell copy and mode buttons", () => {
    const root = document.createElement("div");
    root.innerHTML = `
      <main class="af-shell">
        <h1 class="af-title">AetherFeed</h1>
        <nav class="af-mode-nav">
          <button type="button" data-mode="news" aria-current="page">News</button>
          <button type="button" data-mode="podcast">Podcasts</button>
          <button type="button" data-mode="booru">Boards</button>
        </nav>
        <p class="af-headline">Read locally. Sync only ciphertext.</p>
        <p class="af-body" data-testid="status">Online — AetherFeed</p>
      </main>
    `;
    const layout = applyChromeLayout(root, { viewportWidth: NARROW_PX });
    expect(root.querySelectorAll("h1")).toHaveLength(1);
    expect(root.querySelectorAll("[data-testid='status']")).toHaveLength(1);
    expect(root.querySelectorAll("[data-mode]")).toHaveLength(3);
    expect(layout.slots.list).toBeTruthy();
    expect(layout.slots.detail).toBeTruthy();
  });

  it("reports each AppMode without injecting a mode rail", () => {
    expect([...APP_MODES]).toEqual(["news", "podcast", "booru"]);
    for (const mode of APP_MODES) {
      const root = document.createElement("div");
      const layout = applyChromeLayout(root, { mode, viewportWidth: WIDE_PX });
      expect(layout.mode).toBe(mode);
      expect(root.querySelector("[data-mode]")).toBeNull();
    }
  });
});
