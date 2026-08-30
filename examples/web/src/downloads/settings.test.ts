import { beforeEach, describe, expect, it } from "vitest";
import {
  AUTO_DOWNLOAD_KEY,
  canStartDownload,
  getAutoDownload,
  getWifiOnly,
  setAutoDownload,
  setWifiOnly,
  WIFI_ONLY_KEY,
} from "./settings";

describe("download wifi and auto-download flags", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("defaults wifi-only off and persists the flag", () => {
    expect(getWifiOnly()).toBe(false);
    setWifiOnly(true);
    expect(localStorage.getItem(WIFI_ONLY_KEY)).toBe("1");
    expect(getWifiOnly()).toBe(true);
    setWifiOnly(false);
    expect(localStorage.getItem(WIFI_ONLY_KEY)).toBe("0");
    expect(getWifiOnly()).toBe(false);
  });

  it("blocks starts when wifi-only is on and the link is not wifi", () => {
    setWifiOnly(true);
    expect(canStartDownload({ isWifi: false })).toBe(false);
    expect(canStartDownload({ isWifi: true })).toBe(true);
    setWifiOnly(false);
    expect(canStartDownload({ isWifi: false })).toBe(true);
  });

  it("persists auto-download independently of the wifi flag", () => {
    setAutoDownload(true);
    setWifiOnly(true);
    expect(localStorage.getItem(AUTO_DOWNLOAD_KEY)).toBe("1");
    expect(getAutoDownload()).toBe(true);
    expect(getWifiOnly()).toBe(true);
  });
});
