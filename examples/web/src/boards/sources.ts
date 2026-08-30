import type { BoardSource } from "./types";

export const BOARD_SOURCES_KEY = "af-board-sources";

export function loadBoardSources(): BoardSource[] {
  try {
    const raw = localStorage.getItem(BOARD_SOURCES_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as BoardSource[];
    return Array.isArray(parsed) ? parsed.filter((row) => row?.baseUrl?.trim()) : [];
  } catch {
    return [];
  }
}

export function saveBoardSources(sources: BoardSource[]): void {
  localStorage.setItem(BOARD_SOURCES_KEY, JSON.stringify(sources));
}

export function addBoardSource(baseUrl: string, label = "Boards"): BoardSource {
  const url = baseUrl.trim();
  const source: BoardSource = {
    id: `board:${url}`,
    kind: "generic",
    baseUrl: url,
    label: label.trim() || "Boards",
  };
  const next = [...loadBoardSources().filter((row) => row.baseUrl !== url), source];
  saveBoardSources(next);
  return source;
}
