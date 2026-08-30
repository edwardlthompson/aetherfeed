import { bindShareUrl } from "../news/newsPaneShare";
import { bindLibraryRoots, wrapLibraryTree } from "../shell/libraryTree";
import { attachVoiceGain } from "../silence/bind";
import { loadPodcastEpisodes, loadPodcastShows } from "./catalog";
import { podcastsCopy as copy } from "./copy";
import { hydrateEnclosures } from "./hydrateEnclosures";
import { clock, esc, showError } from "./paneFormat";
import { HtmlAudioPodcastPlayer } from "./player";

export type PodcastsPaneOptions = {
  audio?: HTMLAudioElement;
};

export function createPodcastsPane(root: HTMLElement, options?: PodcastsPaneOptions): HTMLElement {
  const pane = document.createElement("section");
  pane.className = "af-podcasts";
  pane.dataset.testid = "podcasts-pane";
  pane.setAttribute("aria-label", copy.title);
  const shows = loadPodcastShows();
  if (!shows.length) {
    pane.innerHTML = wrapLibraryTree(
      `<p class="af-podcasts-empty" data-podcasts-empty>${esc(copy.empty)}</p>`,
    );
    bindLibraryRoots(pane);
    root.replaceChildren(pane);
    return pane;
  }
  const episodes = loadPodcastEpisodes();
  const audio = options?.audio ?? document.createElement("audio");
  audio.preload = "metadata";
  attachVoiceGain(audio);
  const byId = new Map(episodes.map((episode) => [episode.id, episode]));
  void hydrateEnclosures().then(() => {
    for (const episode of loadPodcastEpisodes()) byId.set(episode.id, episode);
  });
  const player = new HtmlAudioPodcastPlayer(audio, (id) => byId.get(id));
  const items = shows
    .map(
      (show) =>
        `<li><button type="button" data-podcasts-show data-episode-id="${esc(show.id)}">${esc(show.title)}</button></li>`,
    )
    .join("");
  pane.innerHTML = wrapLibraryTree(`
    <ul class="af-podcasts-shows" data-podcasts-shows>${items}</ul>
    <aside class="af-podcasts-mini" data-mini-player>
      <p data-now-playing></p>
      <button type="button" data-play-pause>${esc(copy.play)}</button>
      <input type="range" data-seek min="0" max="0" value="0" step="1" aria-label="${esc(copy.seek)}" />
      <label>${esc(copy.speed)}
        <select data-speed>
          <option value="0.75">0.75x</option>
          <option value="1" selected>1x</option>
          <option value="1.25">1.25x</option>
          <option value="1.5">1.5x</option>
          <option value="2">2x</option>
        </select>
      </label>
      <span data-clock>0:00</span>
      <p data-player-error hidden><span data-error-text></span>
        <button type="button" data-retry>${esc(copy.retry)}</button></p>
    </aside>
  `);
  bindLibraryRoots(pane);
  pane.appendChild(audio);
  let token = 0;
  let currentId = episodes[0]?.id ?? "";
  const playBtn = pane.querySelector<HTMLButtonElement>("[data-play-pause]");
  const seek = pane.querySelector<HTMLInputElement>("[data-seek]");
  const speed = pane.querySelector<HTMLSelectElement>("[data-speed]");
  const nowEl = pane.querySelector<HTMLElement>("[data-now-playing]");
  const clockEl = pane.querySelector<HTMLElement>("[data-clock]");
  const errBox = pane.querySelector<HTMLElement>("[data-player-error]");

  const paint = (): void => {
    if (playBtn) playBtn.textContent = audio.paused ? copy.play : copy.pause;
    if (clockEl) clockEl.textContent = clock(audio.currentTime * 1000);
    if (seek && Number.isFinite(audio.duration) && audio.duration > 0) {
      seek.max = String(Math.floor(audio.duration));
      seek.value = String(Math.floor(audio.currentTime));
    }
    const episode = byId.get(currentId);
    if (nowEl && episode) nowEl.textContent = `${copy.nowPlaying}: ${episode.title}`;
  };

  const start = (id: string): void => {
    currentId = id;
    const mine = ++token;
    if (errBox) errBox.hidden = true;
    void player.play(id).then(
      () => {
        if (mine === token) paint();
      },
      (err: unknown) => {
        if (mine === token) showError(pane, err);
      },
    );
  };

  pane.querySelectorAll<HTMLButtonElement>("[data-podcasts-show]").forEach((button) => {
    button.addEventListener("click", () => {
      const id = button.dataset.episodeId;
      if (id) start(id);
    });
  });
  playBtn?.addEventListener("click", () => {
    if (!currentId) return;
    if (!audio.paused) {
      void player.pause().then(paint);
      return;
    }
    start(currentId);
  });
  seek?.addEventListener("input", () => {
    void player.seekTo(Number(seek.value) * 1000).then(paint);
  });
  speed?.addEventListener("change", () => {
    player.setSpeed(Number(speed.value));
  });
  pane.querySelector("[data-retry]")?.addEventListener("click", () => {
    if (currentId) start(currentId);
  });
  audio.addEventListener("timeupdate", paint);
  bindShareUrl(() => (shows.find((show) => show.id === currentId) ?? shows[0])?.url);
  window.addEventListener("af-pod-play", () => playBtn?.click());
  window.addEventListener("af-pod-skip-back", () => {
    audio.currentTime = Math.max(0, audio.currentTime - 15);
    paint();
  });
  window.addEventListener("af-pod-skip-fwd", () => {
    audio.currentTime += 15;
    paint();
  });
  if (nowEl && episodes[0]) nowEl.textContent = `${copy.nowPlaying}: ${episodes[0].title}`;
  root.replaceChildren(pane);
  return pane;
}
