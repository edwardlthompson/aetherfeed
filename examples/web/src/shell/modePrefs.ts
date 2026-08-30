import { APP_MODES, type AppMode } from "./modeRail";

export const APP_MODE_KEY = "af-app-mode";

export function parseAppMode(raw: unknown): AppMode {
  return APP_MODES.includes(raw as AppMode) ? (raw as AppMode) : "news";
}

export function loadAppMode(): AppMode {
  try {
    return parseAppMode(localStorage.getItem(APP_MODE_KEY));
  } catch {
    return "news";
  }
}

export function saveAppMode(mode: AppMode): void {
  localStorage.setItem(APP_MODE_KEY, mode);
}
