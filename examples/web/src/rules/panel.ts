import { formMarkup, listMarkup } from "./render";
import { loadRules, normalizeRule, saveRules } from "./store";
import type { Rule } from "./types";

export type RulesPanelHandle = {
  refresh: () => void;
};

function splitTerms(raw: string): string[] {
  return raw
    .split(",")
    .map((part) => part.trim())
    .filter(Boolean);
}

function readDraft(root: HTMLElement): Rule | null {
  const include = root.querySelector<HTMLInputElement>("[data-rules-include]");
  const exclude = root.querySelector<HTMLInputElement>("[data-rules-exclude]");
  const tag = root.querySelector<HTMLInputElement>("[data-rules-tag]");
  return normalizeRule({
    include: splitTerms(include?.value ?? ""),
    exclude: splitTerms(exclude?.value ?? ""),
    tag: tag?.value ?? "",
  });
}

function paintList(root: HTMLElement, rules: readonly Rule[]): void {
  const host = root.querySelector<HTMLElement>("[data-rules-host]");
  if (host) host.innerHTML = listMarkup(rules);
}

export function createRulesPanel(root: HTMLElement): RulesPanelHandle {
  root.className = "af-rules";
  root.setAttribute("aria-label", "Keyword rules");
  root.dataset.testid = "rules-panel";
  root.innerHTML = formMarkup();

  const refresh = () => {
    paintList(root, loadRules());
  };

  root.querySelector("[data-rules-form]")?.addEventListener("submit", (event) => {
    event.preventDefault();
    const draft = readDraft(root);
    if (!draft) return;
    saveRules([...loadRules(), draft]);
    const include = root.querySelector<HTMLInputElement>("[data-rules-include]");
    const exclude = root.querySelector<HTMLInputElement>("[data-rules-exclude]");
    const tag = root.querySelector<HTMLInputElement>("[data-rules-tag]");
    if (include) include.value = "";
    if (exclude) exclude.value = "";
    if (tag) tag.value = "";
    refresh();
  });

  root.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    const button = target.closest<HTMLElement>("[data-rules-remove]");
    if (!button) return;
    const index = Number(button.getAttribute("data-rules-remove"));
    if (!Number.isInteger(index) || index < 0) return;
    const next = loadRules().filter((_, i) => i !== index);
    saveRules(next);
    refresh();
  });

  refresh();
  return { refresh };
}
