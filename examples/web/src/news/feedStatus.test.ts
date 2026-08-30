import { describe, expect, it } from "vitest";
import { emptyFeedNotice, refreshNotice } from "./feedStatus";
import { classifyHttpMessage, classifyHttpStatus } from "./fetchError";

describe("feed status", () => {
  it("treats 404 and 410 as gone", () => {
    expect(classifyHttpStatus(404)).toBe("gone");
    expect(classifyHttpStatus(410)).toBe("gone");
    expect(classifyHttpStatus(500)).toBe("network");
    expect(classifyHttpMessage("HTTP 404")).toBe("gone");
  });

  it("names a gone feed for the user", () => {
    expect(refreshNotice("gone", "NPR News")).toContain("NPR News");
    expect(refreshNotice("gone", "NPR News")).toContain("no longer available");
    expect(emptyFeedNotice("Local")).toContain("Local");
  });
});
