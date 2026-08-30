import { afterEach, describe, expect, it } from "vitest";
import { addBoardSource, BOARD_SOURCES_KEY, loadBoardSources } from "./sources";

afterEach(() => {
  localStorage.removeItem(BOARD_SOURCES_KEY);
});

describe("board sources", () => {
  it("stores a public board url", () => {
    addBoardSource("https://boards.example/api");
    expect(loadBoardSources()[0]?.baseUrl).toBe("https://boards.example/api");
  });
});
