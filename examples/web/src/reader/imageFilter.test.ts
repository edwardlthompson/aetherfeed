import { describe, expect, it } from "vitest";
import { dropNonContentImages, firstThumb, isContentImage } from "./imageFilter";

describe("imageFilter", () => {
  it("keeps article photos and drops social or tiny icons", () => {
    expect(isContentImage("https://cdn.example/story.jpg")).toBe(true);
    expect(isContentImage("https://www.facebook.com/sharer.php?u=1")).toBe(false);
    expect(isContentImage("https://cdn.example/share-icon.png", 16, 16, "Share")).toBe(false);
    expect(isContentImage("https://platform.twitter.com/widgets/tweet.png")).toBe(false);
  });

  it("removes skipped imgs from reading-mode DOM in place", () => {
    const doc = new DOMParser().parseFromString(
      `<article><p>Hi</p><img src="https://cdn.example/photo.jpg"><img src="https://facebook.com/share.png" width="16"></article>`,
      "text/html",
    );
    const root = doc.querySelector("article")!;
    dropNonContentImages(root);
    const srcs = [...root.querySelectorAll("img")].map((img) => img.getAttribute("src"));
    expect(srcs).toEqual(["https://cdn.example/photo.jpg"]);
  });

  it("picks the first remaining cached image as the thumbnail", () => {
    expect(
      firstThumb(
        {
          "https://facebook.com/a.png": "data:image/png;base64,aa",
          "https://cdn.example/hero.jpg": "data:image/jpeg;base64,bb",
        },
        ["https://cdn.example/hero.jpg"],
      ),
    ).toBe("data:image/jpeg;base64,bb");
  });
});
