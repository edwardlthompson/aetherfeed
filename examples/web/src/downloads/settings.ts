export const WIFI_ONLY_KEY = "af-downloads-wifi-only";
export const AUTO_DOWNLOAD_KEY = "af-downloads-auto";

function readFlag(key: string, fallback: boolean): boolean {
  const raw = localStorage.getItem(key);
  if (raw === null) return fallback;
  return raw === "1" || raw === "true";
}

export function getWifiOnly(): boolean {
  return readFlag(WIFI_ONLY_KEY, false);
}

export function setWifiOnly(value: boolean): void {
  localStorage.setItem(WIFI_ONLY_KEY, value ? "1" : "0");
}

export function getAutoDownload(): boolean {
  return readFlag(AUTO_DOWNLOAD_KEY, false);
}

export function setAutoDownload(value: boolean): void {
  localStorage.setItem(AUTO_DOWNLOAD_KEY, value ? "1" : "0");
}

export function detectWifi(nav: Navigator = navigator): boolean {
  const conn = (nav as Navigator & { connection?: { type?: string } }).connection;
  if (!conn?.type) return true;
  return conn.type === "wifi";
}

export function canStartDownload(opts?: { wifiOnly?: boolean; isWifi?: boolean }): boolean {
  const wifiOnly = opts?.wifiOnly ?? getWifiOnly();
  if (!wifiOnly) return true;
  return opts?.isWifi ?? detectWifi();
}
