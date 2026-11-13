import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import en from "../locales/en.json";
import { createBooruGridChrome } from "./gridChrome";

const messages = en as Record<string, string>;

describe("createBooruGridChrome", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("renders title, search field, favorite affordance, and empty state", () => {
    createBooruGridChrome(root);

    const pane = root.querySelector<HTMLElement>("[data-testid='booru-pane']");
    expect(pane).not.toBeNull();
    expect(pane?.getAttribute("aria-label")).toBe(messages["booru.title"]);

    expect(root.querySelector("[data-testid='booru-title']")?.textContent).toBe(
      messages["booru.title"],
    );

    const search = root.querySelector<HTMLInputElement>("[data-testid='booru-search']");
    expect(search).not.toBeNull();
    expect(search?.placeholder).toBe(messages["booru.search_placeholder"]);
    expect(search?.type).toBe("search");

    const favorite = root.querySelector<HTMLButtonElement>("[data-testid='booru-favorite']");
    expect(favorite?.textContent).toBe(messages["booru.favorite"]);
    expect(favorite?.getAttribute("aria-label")).toBe(messages["booru.favorite"]);

    expect(root.querySelector("[data-testid='booru-empty']")?.textContent).toBe(
      messages["booru.empty"],
    );
  });

  it("hides empty state when isEmpty is false", () => {
    createBooruGridChrome(root, { isEmpty: false });
    expect(root.querySelector("[data-testid='booru-empty']")).toBeNull();
  });

  it("invokes search and favorite callbacks", () => {
    const onSearch = vi.fn();
    const onFavorite = vi.fn();
    createBooruGridChrome(root, { onSearch, onFavorite });

    const search = root.querySelector<HTMLInputElement>("[data-testid='booru-search']");
    search!.value = " landscape";
    search!.dispatchEvent(new Event("input"));
    expect(onSearch).toHaveBeenCalledWith("landscape");

    root.querySelector<HTMLButtonElement>("[data-testid='booru-favorite']")!.click();
    expect(onFavorite).toHaveBeenCalledOnce();
  });

  it("replaces prior content when re-rendered", () => {
    createBooruGridChrome(root, { isEmpty: true });
    createBooruGridChrome(root, { isEmpty: false });
    expect(root.querySelectorAll("[data-testid='booru-pane']")).toHaveLength(1);
    expect(root.querySelector("[data-testid='booru-empty']")).toBeNull();
  });
});
