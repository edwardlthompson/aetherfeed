import { describe, expect, it } from "vitest";
import { parsePodcastFeed } from "./parsePodcast";

describe("parsePodcastFeed", () => {
  it("keeps items with audio enclosures", () => {
    const xml = `<?xml version="1.0"?><rss><channel><title>Show</title>
      <item><title>Ep 1</title><guid>e1</guid>
      <enclosure url="https://cdn.ex/e1.mp3" type="audio/mpeg"/></item>
      <item><title>Skip</title><guid>s1</guid></item>
    </channel></rss>`;
    const doc = parsePodcastFeed(xml, "https://ex.test/pod.xml");
    expect(doc.showTitle).toBe("Show");
    expect(doc.episodes).toHaveLength(1);
    expect(doc.episodes[0].enclosureUrl).toBe("https://cdn.ex/e1.mp3");
    expect(doc.episodes[0].title).toBe("Ep 1");
  });
});
