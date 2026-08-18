import { describe, expect, it } from "vitest";
import { totalUnread } from "./unread";

describe("totalUnread", () => {
  it("matches the widget and tray contract", () => {
    const count = totalUnread([
      { targetId: "a", module: "news", status: "unread", updatedAt: 1 },
      { targetId: "b", module: "podcast", status: "in_progress", updatedAt: 2 },
      { targetId: "c", module: "booru", status: "read", updatedAt: 3 },
    ]);
    expect(count).toBe(2);
  });
});
