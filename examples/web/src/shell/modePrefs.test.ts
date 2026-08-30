import { beforeEach, describe, expect, it } from "vitest";
import { APP_MODE_KEY, loadAppMode, parseAppMode, saveAppMode } from "./modePrefs";

describe("app mode prefs", () => {
  beforeEach(() => {
    localStorage.removeItem(APP_MODE_KEY);
  });

  it("defaults to news and persists the last tab", () => {
    expect(loadAppMode()).toBe("news");
    expect(parseAppMode("nope")).toBe("news");
    saveAppMode("podcast");
    expect(loadAppMode()).toBe("podcast");
  });
});
