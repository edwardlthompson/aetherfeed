import { afterEach, describe, expect, it } from "vitest";
import {
  allowImages,
  DEFAULT_READER_MODE,
  loadReaderSettings,
  parseArticleTarget,
  saveReaderSettings,
} from "./readerPrefs";

describe("readerPrefs", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("defaults to in-app reader like Hermes launcher", () => {
    expect(parseArticleTarget(null)).toBe("LAUNCHER");
    expect(loadReaderSettings().target).toBe("LAUNCHER");
    expect(loadReaderSettings().imagePolicy).toBe("ALWAYS");
    expect(DEFAULT_READER_MODE).toBe("READING");
  });

  it("persists opener and image policy", () => {
    saveReaderSettings({ target: "BROWSER", imagePolicy: "NEVER", bodyScale: 1.3 });
    expect(loadReaderSettings()).toEqual({
      target: "BROWSER",
      imagePolicy: "NEVER",
      bodyScale: 1.3,
    });
  });

  it("gates images by policy", () => {
    expect(allowImages("ALWAYS")).toBe(true);
    expect(allowImages("WIFI")).toBe(true);
    expect(allowImages("NEVER")).toBe(false);
  });
});
