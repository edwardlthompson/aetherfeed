import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createBrowseNav } from "./browseChrome";

describe("createBrowseNav", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("selects a folder, toggles unread, and changes sort", () => {
    const onSelect = vi.fn();
    const onUnreadOnly = vi.fn();
    const onSort = vi.fn();
    const nav = createBrowseNav({
      nodes: [
        { id: "", label: "All", unreadCount: 3 },
        {
          id: "folder:ex.test",
          label: "ex.test",
          unreadCount: 1,
          children: [{ id: "https://ex.test/rss", label: "Ex", unreadCount: 1 }],
        },
      ],
      selectedId: "",
      onSelect,
      unreadOnly: false,
      onUnreadOnly,
      sort: "newest",
      onSort,
    });
    root.append(nav.tree, nav.toolbar);
    expect(root.querySelector('[data-testid="tree-count-all"]')?.textContent).toBe("3");
    root.querySelector<HTMLButtonElement>('[data-testid="tree-node-https://ex.test/rss"]')?.click();
    const unread = root.querySelector<HTMLInputElement>('[data-testid="filter-unread"]');
    unread!.checked = true;
    unread!.dispatchEvent(new Event("change"));
    root.querySelector<HTMLButtonElement>('[data-testid="filter-sort-oldest"]')?.click();
    expect(onSelect).toHaveBeenCalledWith("https://ex.test/rss");
    expect(onUnreadOnly).toHaveBeenCalledWith(true);
    expect(onSort).toHaveBeenCalledWith("oldest");
  });
});
