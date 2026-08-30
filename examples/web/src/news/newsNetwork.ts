import { canStartDownload } from "../downloads/settings";

export const NEWS_WIFI_ONLY_KEY = "af-news-wifi-only";

function readFlag(key: string, fallback: boolean): boolean {
  const raw = localStorage.getItem(key);
  if (raw === null) return fallback;
  return raw === "1" || raw === "true";
}

export function getNewsWifiOnly(): boolean {
  return readFlag(NEWS_WIFI_ONLY_KEY, true);
}

export function setNewsWifiOnly(value: boolean): void {
  localStorage.setItem(NEWS_WIFI_ONLY_KEY, value ? "1" : "0");
}

export function canFetchNews(opts?: { wifiOnly?: boolean; isWifi?: boolean }): boolean {
  return canStartDownload({
    wifiOnly: opts?.wifiOnly ?? getNewsWifiOnly(),
    isWifi: opts?.isWifi,
  });
}
