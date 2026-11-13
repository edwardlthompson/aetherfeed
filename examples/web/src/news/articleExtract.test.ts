import { describe, expect, it } from "vitest";
import { blocksToReadingHtml, extractBlocks, htmlToText } from "./articleExtract";

describe("articleExtract", () => {
  it("keeps article text and http images", () => {
    const html =
      "<article><p>A long enough paragraph for the extractor to keep as reading text.</p>" +
      '<img src="https://cdn.ex/photo.jpg"><img src="https://cdn.ex/favicon.ico"></article>';
    const blocks = extractBlocks(html);
    expect(blocks.some((b) => b.kind === "text" && b.value.includes("long enough"))).toBe(true);
    expect(blocks.some((b) => b.kind === "image" && b.url.includes("photo.jpg"))).toBe(true);
    expect(blocks.some((b) => b.kind === "image" && b.url.includes("favicon"))).toBe(false);
  });

  it("strips tags for rss text", () => {
    expect(htmlToText("<p>Hello <b>world</b></p>")).toBe("Hello world");
  });

  it("emits in-page markup instead of a nested document", () => {
    const html = blocksToReadingHtml([{ kind: "text", value: "Hello" }], {
      showImages: true,
      bodyScale: 1,
    });
    expect(html).toContain("<p>Hello</p>");
    expect(html.toLowerCase()).not.toContain("<html");
  });
});
