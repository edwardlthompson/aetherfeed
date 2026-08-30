import { describe, expect, it } from "vitest";
import { enclosureFromRss, rememberEnclosure, rememberedEnclosure } from "./enclosures";

describe("enclosureFromRss", () => {
  it("reads the enclosure url", () => {
    const url = enclosureFromRss(
      `<rss><channel><item><enclosure url="https://cdn.example/ep.mp3" type="audio/mpeg"/></item></channel></rss>`,
    );
    expect(url).toBe("https://cdn.example/ep.mp3");
    rememberEnclosure("show-1", url);
    expect(rememberedEnclosure("show-1")).toBe(url);
  });
});
