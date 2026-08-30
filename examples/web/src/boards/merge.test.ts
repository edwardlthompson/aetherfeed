import { describe, expect, it } from "vitest";
import { mergeBoardPosts } from "./merge";

describe("mergeBoardPosts", () => {
  it("keeps one post per remote id", () => {
    const merged = mergeBoardPosts([
      { id: "a:1", sourceId: "a", remoteId: "1", fileUrl: "https://x/a.jpg", tags: ["sky"] },
      { id: "b:1", sourceId: "b", remoteId: "1", fileUrl: "https://x/b.jpg", tags: ["sky"] },
    ]);
    expect(merged).toHaveLength(1);
  });
});
