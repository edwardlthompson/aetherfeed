import type { Article, Episode, ModuleKind, PlaybackPosition, Star } from "@aetherfeed/domain";

const STORAGE_KEY = "af-offline-library";

export type OfflineLibraryState = {
  articles: Article[];
  episodes: Episode[];
  bodies: Record<string, string>;
  stars: Star[];
  playback: PlaybackPosition[];
};

function emptyState(): OfflineLibraryState {
  return { articles: [], episodes: [], bodies: {}, stars: [], playback: [] };
}

export function loadOfflineLibrary(
  storage: Pick<Storage, "getItem"> = localStorage,
): OfflineLibraryState {
  const raw = storage.getItem(STORAGE_KEY);
  if (!raw) return emptyState();
  try {
    const parsed = JSON.parse(raw) as OfflineLibraryState;
    return {
      articles: Array.isArray(parsed.articles) ? parsed.articles : [],
      episodes: Array.isArray(parsed.episodes) ? parsed.episodes : [],
      bodies: parsed.bodies && typeof parsed.bodies === "object" ? parsed.bodies : {},
      stars: Array.isArray(parsed.stars) ? parsed.stars : [],
      playback: Array.isArray(parsed.playback) ? parsed.playback : [],
    };
  } catch {
    return emptyState();
  }
}

export function saveOfflineLibrary(
  state: OfflineLibraryState,
  storage: Pick<Storage, "setItem"> = localStorage,
): void {
  storage.setItem(STORAGE_KEY, JSON.stringify(state));
}

/** Cache an article body for radio-off reading. */
export function cacheArticle(
  state: OfflineLibraryState,
  article: Article,
  body: string,
): OfflineLibraryState {
  const without = state.articles.filter((a) => a.id !== article.id);
  const cached: Article = { ...article, localPath: `local://${article.id}` };
  return {
    ...state,
    articles: [...without, cached],
    bodies: { ...state.bodies, [article.id]: body },
  };
}

/** Prefer local body; when offline, never fall back to network. */
export function readCachedBody(
  state: OfflineLibraryState,
  id: string,
  online: boolean,
  networkBody?: string | null,
): string | null {
  const local = state.bodies[id];
  if (local != null) return local;
  if (!online) return null;
  return networkBody ?? null;
}

export function upsertStar(
  state: OfflineLibraryState,
  targetId: string,
  module: ModuleKind,
  createdAt: number,
): OfflineLibraryState {
  const stars = state.stars.filter((s) => s.targetId !== targetId);
  stars.push({ targetId, module, createdAt });
  return { ...state, stars };
}

export function upsertPlayback(
  state: OfflineLibraryState,
  position: PlaybackPosition,
): OfflineLibraryState {
  const playback = state.playback.filter((p) => p.episodeId !== position.episodeId);
  playback.push(position);
  return { ...state, playback };
}

export function getPlayback(
  state: OfflineLibraryState,
  episodeId: string,
): PlaybackPosition | undefined {
  return state.playback.find((p) => p.episodeId === episodeId);
}
