import type { Rule, RuleArticle, RuleResult } from "./types";

function terms(list: unknown): string[] {
  if (!Array.isArray(list)) return [];
  const out: string[] = [];
  for (const item of list) {
    if (typeof item !== "string") continue;
    const word = item.trim().toLowerCase();
    if (word && !out.includes(word)) out.push(word);
  }
  return out;
}

function haystack(article: RuleArticle | null | undefined): string {
  if (!article || typeof article !== "object") return "";
  return [article.title, article.summary, article.content, article.contentHtml]
    .filter((part): part is string => typeof part === "string")
    .join("\n")
    .toLowerCase();
}

function hits(text: string, keywords: string[]): boolean {
  return keywords.some((word) => text.includes(word));
}

function tagOf(rule: Rule): string | undefined {
  const tag = typeof rule.tag === "string" ? rule.tag.trim() : "";
  return tag || undefined;
}

export function applyRules(
  article: RuleArticle | null | undefined,
  rules: readonly Rule[] | null | undefined,
): RuleResult {
  const list = Array.isArray(rules) ? rules : [];
  if (!list.length) return { keep: true, tags: [] };

  const text = haystack(article);
  let excluded = false;
  let includeRequired = false;
  let includeMatched = false;
  const tags: string[] = [];

  for (const raw of list) {
    if (!raw || typeof raw !== "object") continue;
    const include = terms(raw.include);
    const exclude = terms(raw.exclude);
    const hitExclude = hits(text, exclude);
    const hitInclude = hits(text, include);
    const includeOk = include.length === 0 || hitInclude;
    if (hitExclude) excluded = true;
    if (include.length > 0) {
      includeRequired = true;
      if (hitInclude) includeMatched = true;
    }
    const tag = tagOf(raw);
    if (includeOk && !hitExclude && tag && !tags.includes(tag)) tags.push(tag);
  }

  const keep = !excluded && (!includeRequired || includeMatched);
  return { keep, tags: keep ? tags : [] };
}
