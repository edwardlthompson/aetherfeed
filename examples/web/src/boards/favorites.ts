export const FAVORITES_KEY = "af-board-favorites";

function readIds(): string[] {
  try {
    const raw = localStorage.getItem(FAVORITES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as unknown;
    if (!Array.isArray(parsed)) return [];
    return parsed.filter((id): id is string => typeof id === "string" && id.length > 0);
  } catch {
    return [];
  }
}

export function loadFavorites(): string[] {
  return readIds();
}

export function isFavorite(postId: string): boolean {
  return readIds().includes(postId);
}

export function toggleFavorite(postId: string): boolean {
  const id = postId.trim();
  if (!id) return false;
  const next = new Set(readIds());
  if (next.has(id)) next.delete(id);
  else next.add(id);
  localStorage.setItem(FAVORITES_KEY, JSON.stringify([...next]));
  return next.has(id);
}

export function clearFavorites(): void {
  localStorage.removeItem(FAVORITES_KEY);
}
