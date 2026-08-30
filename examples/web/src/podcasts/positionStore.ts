import type { PlaybackPosition } from "@aetherfeed/domain";

export const POSITION_KEY = "af-podcast-position";

function isPosition(value: unknown): value is PlaybackPosition {
  if (!value || typeof value !== "object") return false;
  const row = value as PlaybackPosition;
  return (
    typeof row.episodeId === "string" && row.episodeId.length > 0 && Number.isFinite(row.positionMs)
  );
}

export function loadPositions(): Record<string, PlaybackPosition> {
  try {
    const raw = localStorage.getItem(POSITION_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as unknown;
    if (!parsed || typeof parsed !== "object" || Array.isArray(parsed)) return {};
    const out: Record<string, PlaybackPosition> = {};
    for (const [key, value] of Object.entries(parsed)) {
      if (isPosition(value)) out[key] = value;
    }
    return out;
  } catch {
    return {};
  }
}

export function getPosition(episodeId: string): PlaybackPosition | null {
  if (!episodeId) return null;
  return loadPositions()[episodeId] ?? null;
}

export function savePosition(position: PlaybackPosition): void {
  if (!isPosition(position)) return;
  const all = loadPositions();
  all[position.episodeId] = {
    episodeId: position.episodeId,
    positionMs: Math.max(0, Math.floor(position.positionMs)),
    durationMs: position.durationMs,
    updatedAt: position.updatedAt,
  };
  localStorage.setItem(POSITION_KEY, JSON.stringify(all));
}

export function clearPositions(): void {
  localStorage.removeItem(POSITION_KEY);
}
