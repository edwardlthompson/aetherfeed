export const SNIPPET_MAX = 160;

import { isStubBody } from "./fullText";

export function plainSnippet(raw: string | undefined, maxChars = SNIPPET_MAX): string {
  const text = (raw ?? "")
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
  if (text.length <= maxChars) return text;
  return `${text.slice(0, maxChars).trimEnd()}…`;
}

export function listSnippet(raw: string | undefined, maxChars = SNIPPET_MAX): string {
  if (isStubBody(raw)) return "";
  const text = plainSnippet(raw, maxChars);
  return /https?:\/\//.test(text) ? "" : text;
}

export function ageLabel(publishedAt: number | undefined, now = Date.now()): string {
  if (publishedAt == null) return "";
  const minutes = Math.max(0, now - publishedAt) / 60_000;
  if (minutes < 60) return "now";
  const hours = Math.floor(minutes / 60);
  if (hours < 48) return `${hours}h`;
  return `${Math.floor(hours / 24)}d`;
}
