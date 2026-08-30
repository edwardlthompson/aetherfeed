/** Lowercase alphanumeric tokens; empty or null input yields []. */
export function tokenize(text: string | null | undefined): string[] {
  if (!text) return [];
  return text
    .toLowerCase()
    .split(/[^a-z0-9]+/)
    .filter(Boolean);
}
