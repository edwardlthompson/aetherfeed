import { describe, expect, it } from "vitest";
import { articleSwipeDelta, newsDrawerOffstage } from "./newsSwipe";

describe("news swipe and closed drawer", () => {
  it("keeps a closed drawer offstage", () => {
    expect(newsDrawerOffstage(false)).toBe(true);
    expect(newsDrawerOffstage(true)).toBe(false);
  });

  it("ignores the folder edge and requires a grand article swipe", () => {
    expect(articleSwipeDelta(10, -200, -3000, 360)).toBe(0);
    expect(articleSwipeDelta(80, -40, -400, 360)).toBe(0);
    expect(articleSwipeDelta(80, -160, -800, 360)).toBe(0);
    expect(articleSwipeDelta(80, -280, 0, 360)).toBe(1);
    expect(articleSwipeDelta(80, -120, -2400, 360)).toBe(1);
  });
});
