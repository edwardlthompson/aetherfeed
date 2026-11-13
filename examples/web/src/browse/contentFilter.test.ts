import { describe, expect, it } from "vitest";
import {
  applyBrowseFilter,
  pruneEmptyTree,
  sortByDate,
  topTags,
  uniqueCategories,
} from "./contentFilter";

describe("browse filters", () => {
  const items = [
    { id: "a", feed: "one", unread: true, starred: false, at: 2 },
    { id: "b", feed: "two", unread: false, starred: true, at: 1 },
  ];

  it("keeps unread in a folder and sorts oldest first", () => {
    const unread = applyBrowseFilter(items, {
      unreadOnly: true,
      starredOnly: false,
      inCategory: (row) => row.feed === "one",
      isUnread: (row) => row.unread,
      isStarred: (row) => row.starred,
    });
    expect(unread.map((row) => row.id)).toEqual(["a"]);
    expect(sortByDate(items, "oldest", (row) => row.at).map((row) => row.id)).toEqual(["b", "a"]);
  });

  it("hides empty folders while keeping All", () => {
    const pruned = pruneEmptyTree([
      { id: "", label: "All", unreadCount: 2 },
      { id: "empty", label: "Empty", unreadCount: 0 },
      { id: "hot", label: "Hot", unreadCount: 2 },
    ]);
    expect(pruned.map((row) => row.id)).toEqual(["", "hot"]);
  });

  it("dedupes folders and ranks tags", () => {
    expect(
      uniqueCategories([
        { id: " ", label: "x" },
        { id: "one", label: " One " },
      ]),
    ).toEqual([{ id: "one", label: "One" }]);
    expect(topTags([["sky", "safe"], ["safe"]], 1)).toEqual([{ id: "safe", label: "safe" }]);
  });
});
