import { describe, expect, it } from "vitest";
import { classifyLockSecret } from "./appLock";

describe("classifyLockSecret", () => {
  it("accepts a 6-digit PIN and rejects shorter digits", () => {
    expect(classifyLockSecret("123456")).toBe("pin");
    expect(classifyLockSecret("12345")).toBeNull();
  });

  it("accepts an 8-character passphrase", () => {
    expect(classifyLockSecret("correct1")).toBe("passphrase");
    expect(classifyLockSecret("short")).toBeNull();
  });
});
