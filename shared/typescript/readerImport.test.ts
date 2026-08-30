import { describe, expect, it } from "vitest";
import { detectReaderVendor, parseReaderImport } from "./readerImport";

const OPML = `<?xml version="1.0"?>
<opml version="1.0"><body>
<outline text="News"><outline type="rss" title="Local" xmlUrl="https://example.invalid/rss.xml" htmlUrl="https://example.invalid"/></outline>
</body></opml>`;

const STARRED = JSON.stringify({
  id: "user/1/state/com.google/starred",
  title: "Starred items",
  items: [{ title: "Saved", canonical: [{ href: "https://example.invalid/starred" }] }],
});

describe("reader import detect/parse", () => {
  it("detects Google Reader Takeout from members", () => {
    expect(
      detectReaderVendor({
        name: "takeout.zip",
        members: { "Takeout/Reader/subscriptions.xml": OPML },
      }),
    ).toBe("google-reader");
  });

  it("detects Inoreader from title text", () => {
    expect(detectReaderVendor({ name: "export.opml", text: `<opml><head><title>Inoreader</title></head>${OPML}` })).toBe(
      "inoreader",
    );
  });

  it("treats generic OPML as opml and still yields feeds", () => {
    const file = { name: "feedly.opml", text: OPML };
    expect(detectReaderVendor(file)).toBe("opml");
    const parsed = parseReaderImport(file);
    expect(parsed.outlines).toHaveLength(1);
    expect(parsed.outlines[0]?.xmlUrl).toBe("https://example.invalid/rss.xml");
  });

  it("rejects empty files", () => {
    const parsed = parseReaderImport({ name: "empty.opml", text: "   " });
    expect(parsed.errors[0]?.code).toBe("empty");
    expect(parsed.outlines).toHaveLength(0);
  });

  it("reports malformed JSON without inventing feeds", () => {
    const parsed = parseReaderImport({ name: "backup.json", text: "{not-json" });
    expect(parsed.errors.some((e) => e.code === "malformed")).toBe(true);
    expect(parsed.outlines).toHaveLength(0);
  });

  it("parses Takeout subscriptions plus starred.json", () => {
    const parsed = parseReaderImport({
      name: "takeout.zip",
      members: {
        "Takeout/Reader/subscriptions.xml": OPML,
        "Takeout/Reader/starred.json": STARRED,
      },
    });
    expect(parsed.vendor).toBe("google-reader");
    expect(parsed.outlines).toHaveLength(1);
    expect(parsed.stars[0]?.url).toBe("https://example.invalid/starred");
  });
});
