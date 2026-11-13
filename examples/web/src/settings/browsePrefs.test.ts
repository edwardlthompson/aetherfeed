import { afterEach, describe, expect, it } from "vitest";
import {
  loadActiveModule,
  loadBrowsePrefs,
  loadReaderMode,
  saveActiveModule,
  saveBrowsePrefs,
  saveReaderMode,
} from "./browsePrefs";

describe("browsePrefs", () => {
  afterEach(() => {
    localStorage.clear();
  });

  it("defaults browse filters and persists across loads", () => {
    expect(loadBrowsePrefs("news")).toEqual({
      unreadOnly: false,
      nodeId: "",
      sort: "newest",
    });
    saveBrowsePrefs("news", {
      unreadOnly: true,
      nodeId: "folder:ex.test",
      sort: "oldest",
    });
    expect(loadBrowsePrefs("news")).toEqual({
      unreadOnly: true,
      nodeId: "folder:ex.test",
      sort: "oldest",
    });
    expect(loadBrowsePrefs("podcasts").unreadOnly).toBe(false);
  });

  it("remembers active module and reader mode", () => {
    expect(loadActiveModule()).toBe("news");
    expect(loadReaderMode()).toBe("READING");
    saveActiveModule("booru");
    saveReaderMode("WEB");
    expect(loadActiveModule()).toBe("booru");
    expect(loadReaderMode()).toBe("WEB");
  });
});
