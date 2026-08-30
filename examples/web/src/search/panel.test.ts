import { describe, expect, it } from "vitest";
import { createSearchPanel } from "./panel";
import type { SearchDoc } from "./types";

const docs: SearchDoc[] = [
  { id: "one", title: "Harbor Report", body: "Ships leave at dawn" },
  { id: "two", title: "Garden Notes", body: "Tomatoes need water" },
];

describe("createSearchPanel", () => {
  it("renders search chrome and keeps empty queries empty without throwing", () => {
    const root = document.createElement("div");
    expect(() => createSearchPanel(root, { docs })).not.toThrow();
    const input = root.querySelector<HTMLInputElement>("[data-search-query]");
    expect(root.dataset.testid).toBe("search-panel");
    expect(root.querySelector("[data-search-chrome]")).toBeTruthy();
    expect(input?.getAttribute("type")).toBe("search");
    expect(root.querySelector("[data-search-hit]")).toBeNull();
    expect(root.querySelector("[data-search-empty]")).toBeTruthy();
    expect(() => {
      if (!input) throw new Error("missing input");
      input.value = "";
      input.dispatchEvent(new Event("input"));
    }).not.toThrow();
    expect(root.querySelectorAll("[data-search-hit]")).toHaveLength(0);
  });

  it("lists case-insensitive token matches as the query changes", () => {
    const root = document.createElement("div");
    createSearchPanel(root, { docs });
    const input = root.querySelector<HTMLInputElement>("[data-search-query]");
    expect(input).toBeTruthy();
    input!.value = "HARBOR";
    input!.dispatchEvent(new Event("input"));
    const hits = [...root.querySelectorAll("[data-search-hit]")].map((el) => el.textContent);
    expect(hits).toEqual(["Harbor Report"]);
    expect(root.querySelector('[data-search-id="one"]')).toBeTruthy();
  });
});
