import { afterEach, describe, expect, it } from "vitest";
import { boardCopy } from "./copy";
import { clearFavorites, FAVORITES_KEY } from "./favorites";
import { createBoardsPane } from "./pane";
import type { BoardPost, BoardSource } from "./types";

const source: BoardSource = {
  id: "demo",
  kind: "danbooru",
  baseUrl: "https://board.example",
  label: "Demo",
};

const post: BoardPost = {
  id: "demo:1",
  sourceId: "demo",
  remoteId: "1",
  fileUrl: "https://x/a.jpg",
  tags: ["sky"],
};

afterEach(() => {
  clearFavorites();
});

describe("createBoardsPane empty states", () => {
  it("shows designed empty copy when there are zero sources", () => {
    const root = document.createElement("div");
    createBoardsPane(root);
    const empty = root.querySelector("[data-boards-empty]");
    expect(empty?.textContent).toBe(boardCopy.emptySources);
    expect(root.querySelector("[data-boards-form]")).toBeNull();
  });

  it("shows designed empty copy for an empty search", async () => {
    const root = document.createElement("div");
    const handle = createBoardsPane(root, {
      sources: [source],
      client: { search: async () => [], isBlacklisted: () => false },
    });
    await handle.search("");
    expect(root.querySelector("[data-boards-empty]")?.textContent).toBe(boardCopy.emptySearch);
  });
});

describe("createBoardsPane favorite toggle", () => {
  it("writes af-board-favorites when a card is favorited", async () => {
    const root = document.createElement("div");
    const handle = createBoardsPane(root, {
      sources: [source],
      client: { search: async () => [post], isBlacklisted: () => false },
    });
    await handle.search("sky");
    const btn = root.querySelector<HTMLButtonElement>("[data-board-fav]");
    expect(btn).toBeTruthy();
    btn?.click();
    expect(JSON.parse(localStorage.getItem(FAVORITES_KEY) ?? "[]")).toContain("demo:1");
    expect(btn?.getAttribute("aria-pressed")).toBe("true");
    btn?.click();
    expect(JSON.parse(localStorage.getItem(FAVORITES_KEY) ?? "[]")).not.toContain("demo:1");
  });
});
