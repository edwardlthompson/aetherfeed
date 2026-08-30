import type { AppMode } from "./AppShellTypes";
import { createBoardsPane } from "./boards";
import { createDownloadQueue, createDownloadsPanel } from "./downloads";
import { createExportPanel } from "./export";
import { t } from "./i18n";
import { createNewsPane } from "./news";
import { createPlayerFx } from "./playerfx";
import { createPlaylistPane } from "./playlists/pane";
import { createPodcastsPane, loadPodcastEpisodes } from "./podcasts";
import { isAudioEnclosure } from "./podcasts/hydrateEnclosures";
import { createDirectoryPane } from "./podindex/pane";
import { createReaderView } from "./reader";
import { loadImportedFeeds } from "./readerimport/importedStore";
import { createRulesPanel } from "./rules";
import { createSearchPanel } from "./search";
import { modeUnreadBadge } from "./shell/navUnread";

const MODES: { id: AppMode; labelKey: string }[] = [
  { id: "news", labelKey: "nav.news" },
  { id: "podcast", labelKey: "nav.podcasts" },
  { id: "booru", labelKey: "nav.boards" },
];

export function modeNav(active: AppMode): string {
  return MODES.map((item) => {
    const current = item.id === active ? ` aria-current="page"` : "";
    return `<button type="button" class="af-mode-btn" data-mode="${item.id}"${current}>${t(item.labelKey)}${modeUnreadBadge(item.id)}</button>`;
  }).join("");
}

export function modeBody(mode: AppMode): string {
  if (mode === "news") {
    return `<div data-news-pane></div>`;
  }
  if (mode === "podcast") {
    return `<div data-podcasts-pane></div><div data-playerfx-mount></div><div data-downloads-mount></div><div data-podindex-mount></div><div data-playlists-mount></div>`;
  }
  return `<div data-boards-pane></div>`;
}

export function mountModePanes(root: HTMLElement, overlaysOpen: boolean): void {
  if (overlaysOpen) return;
  const news = root.querySelector<HTMLElement>("[data-news-pane]");
  if (news) createNewsPane(news);
  const search = root.querySelector<HTMLElement>("[data-search-mount]");
  if (search) {
    createSearchPanel(search, {
      docs: loadImportedFeeds().map((feed) => ({ id: feed.id, title: feed.title, body: feed.url })),
    });
  }
  const rules = root.querySelector<HTMLElement>("[data-rules-mount]");
  if (rules) createRulesPanel(rules);
  const exp = root.querySelector<HTMLElement>("[data-export-mount]");
  if (exp) createExportPanel(exp);
  const reader = root.querySelector<HTMLElement>("[data-reader-mount]");
  if (reader) createReaderView(reader, "");
  const podcasts = root.querySelector<HTMLElement>("[data-podcasts-pane]");
  if (podcasts) createPodcastsPane(podcasts);
  const playerfx = root.querySelector<HTMLElement>("[data-playerfx-mount]");
  if (playerfx) {
    createPlayerFx(playerfx, {
      onSleepDone: () => {
        const audio = root.querySelector("audio");
        if (audio) audio.pause();
      },
    });
  }
  const downloads = root.querySelector<HTMLElement>("[data-downloads-mount]");
  if (downloads) {
    const queue = createDownloadQueue();
    for (const episode of loadPodcastEpisodes()) {
      if (isAudioEnclosure(episode.enclosureUrl)) queue.enqueue(episode.id, episode.enclosureUrl);
    }
    createDownloadsPanel(downloads, { queue });
  }
  const directory = root.querySelector<HTMLElement>("[data-podindex-mount]");
  if (directory) createDirectoryPane(directory);
  const playlists = root.querySelector<HTMLElement>("[data-playlists-mount]");
  if (playlists) createPlaylistPane(playlists);
  const boards = root.querySelector<HTMLElement>("[data-boards-pane]");
  if (boards) createBoardsPane(boards);
}
