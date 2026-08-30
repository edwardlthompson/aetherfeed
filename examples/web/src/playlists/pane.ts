import { loadPodcastEpisodes } from "../podcasts/catalog";
import { getPosition } from "../podcasts/positionStore";
import { applyPlaylist } from "./filter";

export function createPlaylistPane(root: HTMLElement): HTMLElement {
  const pane = document.createElement("section");
  pane.dataset.testid = "playlists-pane";
  const episodes = loadPodcastEpisodes().map((episode) => ({
    id: episode.id,
    title: episode.title,
    played: (getPosition(episode.id)?.positionMs ?? 0) > 0,
    publishedAt: Date.now(),
  }));
  const hits = applyPlaylist(episodes, { unplayedOnly: true });
  pane.innerHTML = `<ul data-playlist-hits>${hits.map((hit) => `<li>${hit.title}</li>`).join("")}</ul>`;
  root.replaceChildren(pane);
  return pane;
}
