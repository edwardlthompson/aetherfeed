import { describe, expect, it } from "vitest";
import { parseFeed } from "./parseFeed";

describe("parseFeed", () => {
  it("parses RSS items", () => {
    const xml = `<?xml version="1.0"?><rss version="2.0"><channel>
      <title>Desk</title><link>https://ex.test/</link>
      <item><title>Hello</title><link>https://ex.test/a</link>
      <guid>https://ex.test/a</guid><description>Body A</description></item>
    </channel></rss>`;
    const doc = parseFeed(xml, "https://ex.test/rss");
    expect(doc.articles).toHaveLength(1);
    expect(doc.articles[0].title).toBe("Hello");
    expect(doc.articles[0].url).toBe("https://ex.test/a");
    expect(doc.articles[0].summary).toBe("Body A");
  });

  it("reads a thumbnail from media:thumbnail", () => {
    const xml = `<?xml version="1.0"?><rss version="2.0"><channel>
      <item><title>Pic</title><link>https://ex.test/p</link>
      <media:thumbnail url="https://cdn.ex/hero.jpg"/>
      <description>Lead</description></item>
    </channel></rss>`;
    expect(parseFeed(xml, "https://ex.test/rss").articles[0].imageUrl).toBe(
      "https://cdn.ex/hero.jpg",
    );
  });

  it("parses Atom entries", () => {
    const xml = `<?xml version="1.0"?><feed xmlns="http://www.w3.org/2005/Atom">
      <id>https://ex.test/atom</id>
      <entry><id>urn:1</id><title>Beta</title>
      <link href="https://ex.test/b"/><summary>Snippet B</summary></entry>
    </feed>`;
    const doc = parseFeed(xml, "https://ex.test/atom");
    expect(doc.articles[0].id).toBe("urn:1");
    expect(doc.articles[0].title).toBe("Beta");
    expect(doc.articles[0].url).toBe("https://ex.test/b");
  });
});
