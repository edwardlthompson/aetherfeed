import { describe, expect, it } from "vitest";
import { isFeedExcerpt, isStubBody, looksPaywalled, needsFetch, usableBody } from "./fullText";
import { parseFeedBody } from "./parseBody";

describe("full-text retrieval", () => {
  it("does not treat RSS description as full content", () => {
    const parsed = parseFeedBody(`<?xml version="1.0"?><rss><channel><item>
      <title>Story</title><link>https://ex.example/a</link>
      <description><![CDATA[<p>${"Word ".repeat(80)}</p>]]></description>
    </item></channel></rss>`);
    expect(parsed?.items[0]?.contentHtml).toBeUndefined();
  });

  it("reads content:encoded from RSS", () => {
    const parsed = parseFeedBody(`<?xml version="1.0"?><rss><channel><item>
      <title>Story</title><link>https://ex.example/a</link>
      <description>Teaser</description>
      <content:encoded xmlns:content="http://purl.org/rss/1.0/modules/content/"><![CDATA[<p>${"Word ".repeat(80)}</p>]]></content:encoded>
    </item></channel></rss>`);
    expect(parsed?.items[0]?.contentHtml?.includes("Word")).toBe(true);
  });

  it("treats teasers and paywall markers as empty", () => {
    expect(needsFetch("short")).toBe(true);
    expect(looksPaywalled("subscribe to continue")).toBe(true);
    expect(usableBody("short")).toBe("");
  });

  it("rejects Hacker News link stubs", () => {
    const hn =
      "<p>Article URL: https://ex.example/a</p><p>Comments URL: https://news.ycombinator.com/item?id=1</p>";
    expect(isStubBody(hn)).toBe(true);
    expect(usableBody(hn)).toBe("");
  });

  it("rejects WordPress feed teasers", () => {
    const excerpt = `<img class="webfeedsFeaturedVisual" src="https://cdn.example/a.jpg" link_thumbnail="1"><p>${"Word ".repeat(80)}</p>`;
    expect(isFeedExcerpt(excerpt)).toBe(true);
    expect(usableBody(excerpt)).toBe("");
  });
});
