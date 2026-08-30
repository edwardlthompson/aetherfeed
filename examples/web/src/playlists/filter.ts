export type PlaylistRule = {
  unplayedOnly?: boolean;
  maxAgeDays?: number;
  titleIncludes?: string;
};

export type PlaylistEpisode = {
  id: string;
  title: string;
  played: boolean;
  publishedAt: number;
};

export function applyPlaylist(
  episodes: PlaylistEpisode[],
  rule: PlaylistRule,
  now = Date.now(),
): PlaylistEpisode[] {
  const needle = rule.titleIncludes?.trim().toLowerCase();
  const cutoff = rule.maxAgeDays != null ? now - rule.maxAgeDays * 86_400_000 : 0;
  return episodes.filter((ep) => {
    if (rule.unplayedOnly && ep.played) return false;
    if (cutoff && ep.publishedAt < cutoff) return false;
    if (needle && !ep.title.toLowerCase().includes(needle)) return false;
    return true;
  });
}
