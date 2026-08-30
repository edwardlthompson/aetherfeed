import { describe, expect, it } from "vitest";
import { createDigestSet, fileDigest, rememberDigest } from "./digest";
import { formatFilename } from "./filename";

describe("dedup", () => {
  it("hashes the same bytes to the same digest", async () => {
    const bytes = new TextEncoder().encode("aether");
    expect(await fileDigest(bytes)).toBe(await fileDigest(bytes));
  });

  it("rejects a duplicate digest", async () => {
    const seen = createDigestSet();
    const digest = await fileDigest(new TextEncoder().encode("dup"));
    expect(rememberDigest(seen, digest)).toBe(true);
    expect(rememberDigest(seen, digest)).toBe(false);
  });

  it("expands filename tokens", () => {
    expect(formatFilename("{id}_{md5}.{ext}", { id: "p1", md5: "abc", ext: "jpg" })).toBe(
      "p1_abc.jpg",
    );
  });
});
