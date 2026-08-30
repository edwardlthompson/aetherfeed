import { afterEach, describe, expect, it } from "vitest";
import { resetImportedFeeds } from "../readerimport/ImportPanel";
import { saveImportedFeeds } from "../readerimport/importedStore";
import { createFeedLibrary } from "./FeedLibrary";

describe("feed library", () => {
  afterEach(() => {
    resetImportedFeeds();
  });

  it("lists news and podcasts after import", () => {
    saveImportedFeeds([
      {
        id: "feed:https://example.invalid/rss.xml",
        title: "Local News",
        url: "https://example.invalid/rss.xml",
        kind: "news",
        folder: "Automotive",
        updatedAt: 1,
      },
      {
        id: "feed:https://libsyn.com/show.xml",
        title: "A Show",
        url: "https://libsyn.com/show.xml",
        kind: "podcast",
        folder: "Podcasts",
        updatedAt: 1,
      },
    ]);
    const library = createFeedLibrary();
    expect(library.textContent).toContain("2 subscriptions");
    expect(library.textContent).toContain("Local News");
    expect(library.textContent).toContain("A Show");
    expect(library.textContent).toContain("Automotive");
    expect(library.textContent).toContain("Podcasts");
  });
});
