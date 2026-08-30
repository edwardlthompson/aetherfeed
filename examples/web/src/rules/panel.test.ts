import { afterEach, describe, expect, it } from "vitest";
import { rulesCopy } from "./copy";
import { createRulesPanel } from "./panel";
import { clearRules, loadRules, RULES_KEY } from "./store";

afterEach(() => {
  clearRules();
});

function mount(): HTMLElement {
  const root = document.createElement("div");
  createRulesPanel(root);
  return root;
}

describe("createRulesPanel", () => {
  it("renders a labeled form into the given root", () => {
    const root = mount();
    expect(root.getAttribute("aria-label")).toBe(rulesCopy.title);
    expect(root.querySelector("[data-rules-form]")).toBeTruthy();
    expect(root.querySelector("[data-rules-empty]")?.textContent).toBe(rulesCopy.empty);
  });

  it("persists an added include/exclude/tag rule in localStorage", () => {
    const root = mount();
    const include = root.querySelector<HTMLInputElement>("[data-rules-include]");
    const exclude = root.querySelector<HTMLInputElement>("[data-rules-exclude]");
    const tag = root.querySelector<HTMLInputElement>("[data-rules-tag]");
    expect(include && exclude && tag).toBeTruthy();
    include!.value = "rust";
    exclude!.value = "gossip";
    tag!.value = "dev";
    root
      .querySelector<HTMLFormElement>("[data-rules-form]")
      ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
    expect(loadRules()).toEqual([{ include: ["rust"], exclude: ["gossip"], tag: "dev" }]);
    expect(localStorage.getItem(RULES_KEY)).toContain("gossip");
    expect(root.querySelector("[data-rules-item]")?.textContent).toContain("rust");
  });

  it("does not persist an empty draft", () => {
    const root = mount();
    root
      .querySelector<HTMLFormElement>("[data-rules-form]")
      ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
    expect(loadRules()).toEqual([]);
    expect(root.querySelector("[data-rules-empty]")).toBeTruthy();
  });
});
