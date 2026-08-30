import { rulesCopy } from "./copy";
import type { Rule } from "./types";

function escapeText(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function csv(list: string[]): string {
  return list.length ? escapeText(list.join(", ")) : "—";
}

export function formMarkup(): string {
  return `
    <h2>${rulesCopy.title}</h2>
    <p>${rulesCopy.hint}</p>
    <form data-rules-form>
      <label>
        <span>${rulesCopy.includeLabel}</span>
        <input type="text" data-rules-include placeholder="${rulesCopy.includePlaceholder}" />
      </label>
      <label>
        <span>${rulesCopy.excludeLabel}</span>
        <input type="text" data-rules-exclude placeholder="${rulesCopy.excludePlaceholder}" />
      </label>
      <label>
        <span>${rulesCopy.tagLabel}</span>
        <input type="text" data-rules-tag placeholder="${rulesCopy.tagPlaceholder}" />
      </label>
      <button type="submit" data-rules-add>${rulesCopy.add}</button>
    </form>
    <div data-rules-host></div>
  `;
}

export function listMarkup(rules: readonly Rule[]): string {
  if (!rules.length) {
    return `<p data-rules-empty>${rulesCopy.empty}</p>`;
  }
  const items = rules
    .map(
      (rule, index) => `
      <li data-rules-item="${index}">
        <p>${rulesCopy.includePrefix}: ${csv(rule.include)}</p>
        <p>${rulesCopy.excludePrefix}: ${csv(rule.exclude)}</p>
        <p>${rulesCopy.tagPrefix}: ${rule.tag ? escapeText(rule.tag) : "—"}</p>
        <button type="button" data-rules-remove="${index}">${rulesCopy.remove}</button>
      </li>`,
    )
    .join("");
  return `<ul data-rules-list>${items}</ul>`;
}
