import { describe, expect, it } from "vitest";
import { openJson, sealJson } from "./envelope";

describe("sync envelope", () => {
  it("round-trips JSON with the same passphrase", async () => {
    const blob = await sealJson('{"ok":true}', "test-pass");
    expect(await openJson(blob, "test-pass")).toBe('{"ok":true}');
  });

  it("rejects the wrong passphrase", async () => {
    const blob = await sealJson("secret", "alpha");
    await expect(openJson(blob, "beta")).rejects.toThrow();
  });
});
