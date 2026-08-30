import { describe, expect, it } from "vitest";
import { applyPlaylist } from "./filter";

describe("smart playlists", () => {
  it("keeps unplayed titles that match", () => {
    const now = 1_000_000;
    const hits = applyPlaylist(
      [
        { id: "1", title: "Daily news", played: false, publishedAt: now },
        { id: "2", title: "Daily news", played: true, publishedAt: now },
        { id: "3", title: "Other", played: false, publishedAt: now },
      ],
      { unplayedOnly: true, titleIncludes: "daily" },
      now,
    );
    expect(hits.map((row) => row.id)).toEqual(["1"]);
  });
});
