import { describe, expect, it } from "vitest";
import { parseChapters } from "./chapters";

describe("parseChapters", () => {
  it("returns an empty list when chapters are missing", () => {
    expect(parseChapters(null)).toEqual([]);
    expect(parseChapters(undefined)).toEqual([]);
    expect(parseChapters("")).toEqual([]);
    expect(parseChapters([])).toEqual([]);
    expect(parseChapters({})).toEqual([]);
    expect(parseChapters("{")).toEqual([]);
  });

  it("parses one chapter from a Podcasting 2.0 payload", () => {
    expect(parseChapters({ chapters: [{ title: "Intro", startTime: 12.5 }] })).toEqual([
      { title: "Intro", startMs: 12_500 },
    ]);
  });

  it("parses one chapter from a startMs row", () => {
    expect(parseChapters([{ title: "Cold open", startMs: 0 }])).toEqual([
      { title: "Cold open", startMs: 0 },
    ]);
  });
});
