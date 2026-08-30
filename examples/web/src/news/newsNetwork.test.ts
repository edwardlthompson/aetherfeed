import { beforeEach, describe, expect, it } from "vitest";
import { canFetchNews, getNewsWifiOnly, NEWS_WIFI_ONLY_KEY, setNewsWifiOnly } from "./newsNetwork";

describe("news wifi policy", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("defaults to wifi-only and can allow cellular", () => {
    expect(getNewsWifiOnly()).toBe(true);
    expect(canFetchNews({ isWifi: false })).toBe(false);
    expect(canFetchNews({ isWifi: true })).toBe(true);
    setNewsWifiOnly(false);
    expect(localStorage.getItem(NEWS_WIFI_ONLY_KEY)).toBe("0");
    expect(canFetchNews({ isWifi: false })).toBe(true);
  });
});
