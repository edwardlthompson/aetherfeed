import { beforeEach, describe, expect, it } from "vitest";
import { getSaveCrashes, setSaveCrashes } from "./saveCrashes";

describe("saveCrashes", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("defaults off and persists the opt-in flag", () => {
    expect(getSaveCrashes()).toBe(false);
    setSaveCrashes(true);
    expect(getSaveCrashes()).toBe(true);
    setSaveCrashes(false);
    expect(getSaveCrashes()).toBe(false);
  });
});
