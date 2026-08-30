export const ART_PREFIX = "af-lock-art:";
export const THUMB_ITEM_PREFIX = "af-lock-thumb:";

const mem = new Map<string, string>();

export function clearPlainSession(): void {
  mem.clear();
}

export function wipeArticleKeys(): void {
  const keys = Object.keys(localStorage).filter(
    (key) => key.startsWith(ART_PREFIX) || key.startsWith(THUMB_ITEM_PREFIX),
  );
  for (const key of keys) localStorage.removeItem(key);
}

export function memGet(key: string): string | undefined {
  return mem.get(key);
}

export function memSet(key: string, value: string): void {
  mem.set(key, value);
}
