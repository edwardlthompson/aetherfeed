import type { PlaybackPosition } from "@aetherfeed/domain";
import { afterEach, describe, expect, it } from "vitest";
import { clearPositions, getPosition, POSITION_KEY, savePosition } from "./positionStore";

describe("podcast position store", () => {
  afterEach(() => {
    clearPositions();
  });

  it("saves and loads position keyed by episodeId", () => {
    const row: PlaybackPosition = {
      episodeId: "ep-alpha",
      positionMs: 12_500,
      durationMs: 60_000,
      updatedAt: 9,
    };
    savePosition(row);
    expect(getPosition("ep-alpha")?.positionMs).toBe(12_500);
    expect(getPosition("ep-alpha")?.durationMs).toBe(60_000);
    expect(getPosition("missing")).toBeNull();
    expect(localStorage.getItem(POSITION_KEY)).toContain("ep-alpha");
  });

  it("ignores corrupt storage and empty ids", () => {
    localStorage.setItem(POSITION_KEY, "{not-json");
    expect(getPosition("ep-alpha")).toBeNull();
    savePosition({ episodeId: "", positionMs: 10, updatedAt: 1 });
    expect(localStorage.getItem(POSITION_KEY)).toBe("{not-json");
  });
});
