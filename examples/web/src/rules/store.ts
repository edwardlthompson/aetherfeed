import type { Rule } from "./types";

export const RULES_KEY = "af-feed-rules";

function asStringList(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  const out: string[] = [];
  for (const item of value) {
    if (typeof item !== "string") continue;
    const word = item.trim();
    if (word && !out.includes(word)) out.push(word);
  }
  return out;
}

export function normalizeRule(value: unknown): Rule | null {
  if (!value || typeof value !== "object") return null;
  const row = value as Partial<Rule>;
  const include = asStringList(row.include);
  const exclude = asStringList(row.exclude);
  const tag = typeof row.tag === "string" ? row.tag.trim() : "";
  if (!include.length && !exclude.length && !tag) return null;
  return tag ? { include, exclude, tag } : { include, exclude };
}

export function loadRules(): Rule[] {
  try {
    const raw = localStorage.getItem(RULES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return [];
    return parsed.map(normalizeRule).filter((rule): rule is Rule => rule !== null);
  } catch {
    return [];
  }
}

export function saveRules(rules: readonly Rule[]): void {
  const next = rules.map(normalizeRule).filter((rule): rule is Rule => rule !== null);
  localStorage.setItem(RULES_KEY, JSON.stringify(next));
}

export function clearRules(): void {
  localStorage.removeItem(RULES_KEY);
}
