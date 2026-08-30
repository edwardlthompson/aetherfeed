import { describe, expect, it } from "vitest";
import { alreadyFetched } from "./cacheSkip";

describe("cache skip", () => {
  it("treats any stored body as already fetched", () => {
    expect(alreadyFetched(null)).toBe(false);
    expect(alreadyFetched({ bodyHtml: " " })).toBe(true);
    expect(alreadyFetched({ bodyHtml: "short stub" })).toBe(true);
  });
});
