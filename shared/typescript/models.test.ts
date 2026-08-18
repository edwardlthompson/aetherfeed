import { describe, expect, it } from "vitest";
import { isOpaqueEncName, mergeLww, SYNC_DOCUMENT_VERSION } from "./sync";

describe("sync primitives", () => {
  it("keeps the document version at 1 for the seed protocol", () => {
    expect(SYNC_DOCUMENT_VERSION).toBe(1);
  });

  it("accepts only opaque .enc blob names", () => {
    expect(isOpaqueEncName("vault.enc")).toBe(true);
    expect(isOpaqueEncName("../vault.enc")).toBe(false);
    expect(isOpaqueEncName("vault.json")).toBe(false);
  });

  it("merges last-write-wins by updatedAt", () => {
    const older = { updatedAt: 10, status: "unread" as const };
    const newer = { updatedAt: 20, status: "read" as const };
    expect(mergeLww(older, newer)?.status).toBe("read");
    expect(mergeLww(newer, older)?.status).toBe("read");
  });
});
