import { describe, expect, it } from "vitest";
import {
  encodeLibraryPick,
  mergeUnified,
  parentPick,
  parseLibraryPick,
  shareUrl,
} from "./libraryPick";

describe("library pick", () => {
  it("round-trips folder and source", () => {
    const folder = { kind: "folder" as const, mode: "news" as const, folder: "Automotive" };
    const source = {
      kind: "source" as const,
      mode: "news" as const,
      folder: "Automotive",
      sourceId: "moto",
    };
    expect(parseLibraryPick(encodeLibraryPick(folder))).toEqual(folder);
    expect(parseLibraryPick(encodeLibraryPick(source))).toEqual(source);
    expect(parentPick(source)).toEqual(folder);
    expect(parentPick({ kind: "all", mode: "news" })).toBeNull();
  });

  it("merges unified newest first and skips empty share urls", () => {
    const rows = mergeUnified(
      [{ mode: "news", id: "a1", title: "Old", publishedAt: 10, url: "https://n.example/1" }],
      [{ mode: "podcast", id: "p1", title: "Show", publishedAt: 20, url: "https://p.example/rss" }],
      [{ mode: "booru", id: "b1", title: "Board", publishedAt: 0, url: "https://b.example/api" }],
      false,
    );
    expect(rows.map((row) => row.id)).toEqual(["p1", "a1", "b1"]);
    expect(shareUrl(rows[1])).toBe("https://n.example/1");
    expect(
      shareUrl({ mode: "news", id: "x", title: "X", publishedAt: 1, url: "  " }),
    ).toBeUndefined();
  });
});
