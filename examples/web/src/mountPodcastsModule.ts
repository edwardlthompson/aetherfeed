import { filterEpisodes, podcastSubscriptionTree } from "./browse/wireFilters";
import { loadOfflineLibrary } from "./domain/library";
import { t } from "./i18n";
import { createPodcastsChrome } from "./podcasts/paneChrome";
import { podcastShowsFromPool, refreshPodcastEpisodes } from "./podcasts/refreshPodcasts";
import { loadBrowsePrefs, saveBrowsePrefs } from "./settings/browsePrefs";

export function mountPodcastsModule(podcastsMount: HTMLElement): void {
  const podPrefs = loadBrowsePrefs("podcasts");
  let podUnreadOnly = podPrefs.unreadOnly;
  let podNode = podPrefs.nodeId;
  let podSort = podPrefs.sort;
  const paint = (
    episodes = loadOfflineLibrary().episodes,
    status: "loading" | "ready" = "loading",
  ) => {
    const tree = podcastSubscriptionTree(episodes, podUnreadOnly);
    if (
      podNode &&
      !tree.some((node) => node.id === podNode || node.children?.some((c) => c.id === podNode))
    ) {
      podNode = "";
    }
    const visible = filterEpisodes(episodes, podUnreadOnly, podNode, podSort);
    createPodcastsChrome(podcastsMount, {
      shows: podcastShowsFromPool(),
      episodes: visible,
      sourceCount: episodes.length,
      status,
      browse: {
        nodes: tree,
        selectedId: podNode,
        onSelect: (id) => {
          podNode = id;
          saveBrowsePrefs("podcasts", { nodeId: id });
          paint(episodes, status);
        },
        unreadOnly: podUnreadOnly,
        onUnreadOnly: (value) => {
          podUnreadOnly = value;
          saveBrowsePrefs("podcasts", { unreadOnly: value });
          paint(episodes, status);
        },
        unreadLabel: t("filter.unplayed"),
        sort: podSort,
        onSort: (nextSort) => {
          podSort = nextSort;
          saveBrowsePrefs("podcasts", { sort: nextSort });
          paint(episodes, status);
        },
      },
    });
  };
  paint(loadOfflineLibrary().episodes, loadOfflineLibrary().episodes.length ? "ready" : "loading");
  void refreshPodcastEpisodes().then((next) => {
    if (!podcastsMount.isConnected) return;
    paint(next, "ready");
  });
}
