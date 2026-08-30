import { isOnline } from "../greet";
import { t } from "../i18n";
import { type AppMode } from "./modeRail";

export type { AppMode, ModeNavKind } from "./modeRail";
export { APP_MODES, createModeRail } from "./modeRail";

export const WIDE_MIN_REM = 48;

export type ChromeSlots = {
  rail: HTMLElement;
  list: HTMLElement;
  detail: HTMLElement;
};

export type ChromeLayout = {
  wide: boolean;
  mode: AppMode;
  slots: ChromeSlots;
};

export type ApplyChromeOptions = {
  mode?: AppMode;
  viewportWidth?: number;
  rootFontPx?: number;
  onMode?: (mode: AppMode) => void;
};

export function isWideViewport(widthPx: number, rootFontPx = 16): boolean {
  const rem = rootFontPx > 0 ? rootFontPx : 16;
  return widthPx >= WIDE_MIN_REM * rem;
}

function readRootFontPx(): number {
  if (typeof getComputedStyle !== "function") return 16;
  const raw = Number.parseFloat(getComputedStyle(document.documentElement).fontSize);
  return Number.isFinite(raw) && raw > 0 ? raw : 16;
}

function ensureShell(root: HTMLElement): HTMLElement {
  if (root.classList.contains("af-shell")) return root;
  const existing = root.querySelector<HTMLElement>("main.af-shell");
  if (existing) return existing;
  const main = document.createElement("main");
  main.className = "af-shell";
  root.append(main);
  return main;
}

function appendEl(parent: HTMLElement, tag: string, className: string, text: string): HTMLElement {
  const el = document.createElement(tag);
  el.className = className;
  el.textContent = text;
  parent.append(el);
  return el;
}

function ensureCopy(shell: HTMLElement): void {
  if (!shell.querySelector("h1")) {
    let header = shell.querySelector<HTMLElement>(".af-header");
    if (!header) {
      header = document.createElement("div");
      header.className = "af-header";
      shell.prepend(header);
    }
    appendEl(header, "h1", "af-title", t("app.title"));
  }
  if (!shell.querySelector("[data-testid='status']")) {
    const status = appendEl(
      shell,
      "p",
      "af-body",
      t(isOnline() ? "app.status.online" : "app.status.offline"),
    );
    status.dataset.testid = "status";
  }
}

function ensureSlot(shell: HTMLElement, name: "rail" | "list" | "detail"): HTMLElement {
  const found = shell.querySelector<HTMLElement>(`[data-chrome-slot="${name}"]`);
  if (found) return found;
  const el = document.createElement(name === "rail" ? "nav" : "section");
  el.className = name === "rail" ? "af-shell-rail" : `af-shell-${name}`;
  el.dataset.chromeSlot = name;
  if (name === "rail") el.setAttribute("aria-label", t("nav.label"));
  shell.append(el);
  return el;
}

function ensureRail(shell: HTMLElement, wide: boolean): HTMLElement {
  const rail = ensureSlot(shell, "rail");
  rail.className = wide ? "af-shell-rail" : "af-shell-nav-narrow";
  return rail;
}

export function applyChromeLayout(
  root: HTMLElement,
  options: ApplyChromeOptions = {},
): ChromeLayout {
  const mode = options.mode ?? "news";
  const rem = options.rootFontPx ?? readRootFontPx();
  const width = options.viewportWidth ?? (typeof window === "undefined" ? 0 : window.innerWidth);
  const wide = isWideViewport(width, rem);
  const shell = ensureShell(root);
  shell.classList.add("af-chrome");
  shell.classList.toggle("af-shell-wide", wide);
  shell.classList.toggle("af-shell-narrow", !wide);
  ensureCopy(shell);
  return {
    wide,
    mode,
    slots: {
      rail: ensureRail(shell, wide),
      list: ensureSlot(shell, "list"),
      detail: ensureSlot(shell, "detail"),
    },
  };
}
