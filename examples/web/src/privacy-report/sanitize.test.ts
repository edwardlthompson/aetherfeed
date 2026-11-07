import { describe, expect, it } from "vitest";
import { sanitizeReportText } from "./sanitize";
import fixture from "./sanitize-fixtures.json";

describe("sanitizeReportText", () => {
  it("treats null as empty", () => {
    expect(sanitizeReportText(null)).toBe("");
  });

  it("redacts secrets, JWT, AWS, and home paths", () => {
    const out = sanitizeReportText(fixture.stack, true);
    for (const leak of fixture.must_not_contain) {
      expect(out).not.toContain(leak);
    }
    for (const keep of fixture.must_contain) {
      expect(out).toContain(keep);
    }
  });
});
