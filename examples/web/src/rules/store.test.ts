import { afterEach, describe, expect, it } from "vitest";
import { clearRules, loadRules, RULES_KEY, saveRules } from "./store";

afterEach(() => {
  clearRules();
});

describe("rules store", () => {
  it("round-trips rules in localStorage", () => {
    saveRules([{ include: [" rust "], exclude: [""], tag: " dev " }]);
    expect(loadRules()).toEqual([{ include: ["rust"], exclude: [], tag: "dev" }]);
    expect(localStorage.getItem(RULES_KEY)).toContain("rust");
  });

  it("returns an empty list for corrupt or empty storage", () => {
    expect(loadRules()).toEqual([]);
    localStorage.setItem(RULES_KEY, "{not-json");
    expect(loadRules()).toEqual([]);
    localStorage.setItem(RULES_KEY, JSON.stringify({ include: ["x"] }));
    expect(loadRules()).toEqual([]);
    localStorage.setItem(RULES_KEY, JSON.stringify([{ include: "nope", exclude: [] }]));
    expect(loadRules()).toEqual([]);
  });
});
