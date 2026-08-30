import { beforeEach, describe, expect, it } from "vitest";
import {
  clampPanes,
  loadNewsChrome,
  NEWS_CHROME_DEFAULT,
  NEWS_CHROME_KEY,
  parseNewsChrome,
  restoreNewsLocation,
  saveNewsChrome,
  toggleExpanded,
  withLocation,
} from "./chromePrefs";

describe("news chrome prefs", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("defaults folders collapsed and persists expand plus pane sizes", () => {
    const first = loadNewsChrome();
    expect(first.expanded).toEqual([]);
    expect(first.sidebarHidden).toBe(false);
    const next = toggleExpanded(first, "World");
    expect(next.expanded).toEqual(["World"]);
    expect(toggleExpanded(next, "World").expanded).toEqual([]);
    const panes = clampPanes(0.5, 0.3, 0.2);
    saveNewsChrome({
      ...NEWS_CHROME_DEFAULT,
      expanded: ["World"],
      sidebarHidden: true,
      oldestFirst: true,
      ...panes,
    });
    expect(loadNewsChrome().oldestFirst).toBe(true);
    expect(localStorage.getItem(NEWS_CHROME_KEY)).toContain("World");
    expect(loadNewsChrome().sidebarHidden).toBe(true);
    expect(
      loadNewsChrome().source + loadNewsChrome().timeline + loadNewsChrome().reader,
    ).toBeCloseTo(1);
  });

  it("rejects junk payloads", () => {
    expect(parseNewsChrome(null).expanded).toEqual([]);
    expect(parseNewsChrome({ expanded: [1, "A"] }).expanded).toEqual(["A"]);
  });

  it("restores last folder and feed", () => {
    const groups: [string, { id: string }[]][] = [
      ["Automotive", [{ id: "feed:cars" }]],
      ["World", [{ id: "feed:world" }]],
    ];
    const prefs = withLocation(NEWS_CHROME_DEFAULT, "World", "feed:world");
    expect(prefs.folder).toBe("World");
    expect(prefs.expanded).toEqual([]);
    expect(restoreNewsLocation(groups, prefs)).toEqual({ folder: "World", feedId: "feed:world" });
    expect(restoreNewsLocation(groups, { ...prefs, feedId: "gone" })).toEqual({
      folder: "World",
      feedId: "feed:world",
    });
  });

  it("toggle expand persists and location save does not reopen", () => {
    const open = toggleExpanded(NEWS_CHROME_DEFAULT, "World");
    expect(open.expanded).toEqual(["World"]);
    const located = withLocation(open, "World", "feed:world");
    expect(located.expanded).toEqual(["World"]);
    const closed = toggleExpanded(located, "World");
    expect(closed.expanded).toEqual([]);
    expect(withLocation(closed, "World", "feed:world").expanded).toEqual([]);
    saveNewsChrome(closed);
    expect(loadNewsChrome().expanded).toEqual([]);
  });
});
