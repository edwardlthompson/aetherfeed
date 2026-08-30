import { describe, expect, it } from "vitest";
import { extractReadable } from "./extract";

describe("extractReadable", () => {
  it("returns an empty body for empty html", () => {
    expect(extractReadable("")).toEqual({ title: "", bodyHtml: "", imageSrcs: [] });
    expect(extractReadable("   \n")).toEqual({ title: "", bodyHtml: "", imageSrcs: [] });
  });

  it("strips script tags from article html", () => {
    const html = `<html><body><article><p>Safe</p><script>alert(1)</script></article></body></html>`;
    const readable = extractReadable(html);
    expect(readable.bodyHtml).toContain("Safe");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("script");
    expect(readable.bodyHtml).not.toContain("alert");
  });

  it("strips nav chrome and onclick handlers", () => {
    const html = `<html><body>
      <nav>Menu</nav>
      <article><h1>Story</h1><p onclick="evil()">Body</p><img src="https://cdn.example/a.png"></article>
    </body></html>`;
    const readable = extractReadable(html);
    expect(readable.title).toBe("Story");
    expect(readable.bodyHtml).toContain("Body");
    expect(readable.bodyHtml).not.toContain("Menu");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("onclick");
    expect(readable.imageSrcs).toEqual(["https://cdn.example/a.png"]);
  });

  it("drops social icons after reading-mode extract and keeps story photos in place", () => {
    const html = `<article><p>Lead</p><img src="https://cdn.example/hero.jpg"><p>More</p><img src="https://www.facebook.com/sharer.png" width="16"></article>`;
    const readable = extractReadable(html);
    expect(readable.bodyHtml).toContain("https://cdn.example/hero.jpg");
    expect(readable.bodyHtml).toContain("Lead");
    expect(readable.bodyHtml.indexOf("hero.jpg")).toBeLessThan(readable.bodyHtml.indexOf("More"));
    expect(readable.bodyHtml).not.toContain("facebook.com");
    expect(readable.imageSrcs).toEqual(["https://cdn.example/hero.jpg"]);
  });

  it("drops comment threads and bare article urls", () => {
    const html = `<article><p>Lead</p><p>Article URL: https://ex.example/a</p><div class="comments">Noisy</div></article>`;
    const readable = extractReadable(html);
    expect(readable.bodyHtml).toContain("Lead");
    expect(readable.bodyHtml).not.toContain("https://");
    expect(readable.bodyHtml).not.toContain("Noisy");
  });

  it("keeps lazy images and the longest content root", () => {
    const html = `<html><body>
      <article><p>Card</p></article>
      <div class="entry-content"><p>${"Story ".repeat(40)}</p><img data-src="https://cdn.example/hero.jpg"></div>
    </body></html>`;
    const readable = extractReadable(html);
    expect(readable.bodyHtml).toContain("Story");
    expect(readable.bodyHtml).toContain("https://cdn.example/hero.jpg");
    expect(readable.imageSrcs).toEqual(["https://cdn.example/hero.jpg"]);
    expect(readable.bodyHtml).not.toContain("Card");
  });

  it("drops site links and inline style so reading mode stays in-app", () => {
    const html = `<article><p style="font-family:Comic Sans">Hi <a href="https://evil.example">there</a></p></article>`;
    const readable = extractReadable(html);
    expect(readable.bodyHtml).toContain("there");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("href");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("<a");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("comic");
    expect(readable.bodyHtml.toLowerCase()).not.toContain("style=");
  });
});
