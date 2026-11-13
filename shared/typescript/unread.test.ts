import { describe, expect, it } from "vitest";
import { totalUnread } from "./unread";
import type { ReadState } from "./models";

function state(
  targetId: string,
  module: ReadState["module"],
  status: ReadState["status"],
  updatedAt: number,
): ReadState {
  return { targetId, module, status, updatedAt };
}

describe("totalUnread", () => {
  it("matches the Android widget and desktop tray contract", () => {
    expect(
      totalUnread([
        state("a", "news", "unread", 1),
        state("b", "podcast", "in_progress", 2),
        state("c", "booru", "read", 3),
      ]),
    ).toBe(2);
  });

  it("returns 0 for an empty list", () => {
    expect(totalUnread([])).toBe(0);
  });

  it("returns 0 when every item is read", () => {
    expect(
      totalUnread([
        state("a", "news", "read", 1),
        state("b", "podcast", "read", 2),
      ]),
    ).toBe(0);
  });

  it("counts in_progress as unread", () => {
    expect(totalUnread([state("ep", "podcast", "in_progress", 1)])).toBe(1);
  });
});
