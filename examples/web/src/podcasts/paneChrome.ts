import type { Episode } from "@aetherfeed/domain";
import { type BrowseNavOptions, createBrowseNav, wrapBrowse } from "../browse/browseChrome";
import { t } from "../i18n";

export type PodcastsChromeOptions = {
  shows?: Array<{ url: string; title: string }>;
  episodes?: Episode[];
  status?: "loading" | "ready";
  browse?: BrowseNavOptions;
  sourceCount?: number;
};

export function createPodcastsChrome(root: HTMLElement, opts: PodcastsChromeOptions = {}): void {
  root.replaceChildren();
  const pane = document.createElement("section");
  pane.className = "af-podcasts-pane";
  pane.dataset.testid = "podcasts-pane";
  pane.setAttribute("aria-label", t("podcasts.title"));
  const title = document.createElement("h2");
  title.className = "af-podcasts-title";
  title.dataset.testid = "podcasts-title";
  title.textContent = t("podcasts.title");
  const main = document.createElement("div");
  main.className = "af-browse-list";
  main.append(title);
  const shows = opts.shows ?? [];
  const episodes = opts.episodes ?? [];
  if (shows.length === 0 && episodes.length === 0) {
    const body = document.createElement("p");
    body.className = "af-podcasts-body";
    body.dataset.testid = "podcasts-body";
    body.textContent =
      opts.status === "loading"
        ? t("podcasts.loading")
        : (opts.sourceCount ?? 0) > 0
          ? t("filter.empty")
          : t("podcasts.pane.empty");
    main.append(body);
    if (opts.browse) wrapBrowse(pane, createBrowseNav(opts.browse), main);
    else pane.append(main);
    root.append(pane);
    return;
  }
  if (shows.length) {
    const sub = document.createElement("p");
    sub.className = "af-podcasts-shows";
    sub.dataset.testid = "podcasts-shows";
    sub.textContent = t("podcasts.shows_count").replace("{count}", String(shows.length));
    main.append(sub);
  }
  const list = document.createElement("ul");
  list.className = "af-podcasts-list";
  list.dataset.testid = "podcasts-episode-list";
  for (const episode of episodes) {
    const item = document.createElement("li");
    item.className = "af-podcasts-item";
    item.dataset.testid = `podcast-episode-${episode.id}`;
    const name = document.createElement("p");
    name.className = "af-podcasts-item-title";
    name.textContent = episode.title;
    const audio = document.createElement("audio");
    audio.controls = true;
    audio.preload = "none";
    audio.src = episode.enclosureUrl;
    item.append(name, audio);
    list.append(item);
  }
  main.append(list);
  if (opts.browse) wrapBrowse(pane, createBrowseNav(opts.browse), main);
  else pane.append(main);
  root.append(pane);
}
