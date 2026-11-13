import { describe, expect, it } from "vitest";
import { prepareArticleHtml } from "./prepareArticleHtml";

describe("prepareArticleHtml", () => {
  it("strips scripts, promotes lazy images, and injects base href", () => {
    const html =
      "<html><head></head><body><script>alert(1)</script>" +
      '<img data-src="https://cdn.example/pic.jpg" alt="p"></body></html>';
    const out = prepareArticleHtml(html, "https://news.example/a");
    expect(out).toContain('<base href="https://news.example/a">');
    expect(out.toLowerCase()).not.toContain("<script");
    expect(out).toContain('src="https://cdn.example/pic.jpg"');
  });
});
