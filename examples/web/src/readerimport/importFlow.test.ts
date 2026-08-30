import { afterEach, describe, expect, it, vi } from "vitest";
import { resetImportedFeeds } from "./ImportPanel";
import { clearImportedFeeds, IMPORTED_FEEDS_KEY } from "./importedStore";
import { applyReaderImportFile } from "./importFlow";

describe("import flow", () => {
  afterEach(() => {
    resetImportedFeeds();
    clearImportedFeeds();
  });

  it("persists many feeds with one localStorage write", async () => {
    const spy = vi.spyOn(localStorage, "setItem");
    const opml = `<opml><body>
      <outline title="A" xmlUrl="https://a.invalid/rss.xml"/>
      <outline title="B" xmlUrl="https://b.invalid/rss.xml"/>
      <outline title="C" xmlUrl="https://c.invalid/rss.xml"/>
    </body></opml>`;
    const result = await applyReaderImportFile({ name: "inoreader.opml", text: opml });
    expect(result.feedsAdded).toBe(3);
    const writes = spy.mock.calls.filter(([key]) => key === IMPORTED_FEEDS_KEY);
    expect(writes).toHaveLength(1);
    spy.mockRestore();
  });
});
