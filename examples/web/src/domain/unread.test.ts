import type { ReadState } from "@aetherfeed/domain";
import { totalUnread as sharedTotalUnread } from "@aetherfeed/domain";
import { describe, expect, it } from "vitest";
import { totalUnread } from "./unread";

function state(
  targetId: string,
  module: ReadState["module"],
  status: ReadState["status"],
  updatedAt: number,
): ReadState {
  return { targetId, module, status, updatedAt };
}

describe("totalUnread", () => {
  it("matches shared domain and the Android widget/tray contract", () => {
    const states = [
      state("a", "news", "unread", 1),
      state("b", "podcast", "in_progress", 2),
      state("c", "booru", "read", 3),
    ];
    expect(totalUnread(states)).toBe(2);
    expect(sharedTotalUnread(states)).toBe(totalUnread(states));
  });

  it("returns 0 for an empty list", () => {
    expect(totalUnread([])).toBe(0);
    expect(sharedTotalUnread([])).toBe(0);
  });

  it("returns 0 when every item is read", () => {
    const states = [state("a", "news", "read", 1), state("b", "podcast", "read", 2)];
    expect(totalUnread(states)).toBe(0);
    expect(sharedTotalUnread(states)).toBe(0);
  });

  it("counts in_progress as unread", () => {
    const states = [state("ep", "podcast", "in_progress", 1)];
    expect(totalUnread(states)).toBe(1);
    expect(sharedTotalUnread(states)).toBe(1);
  });
});
