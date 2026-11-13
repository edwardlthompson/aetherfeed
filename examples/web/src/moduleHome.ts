import type { Article } from "@aetherfeed/domain";
import { createBooruGridChrome } from "./booru/gridChrome";
import {
  filterEpisodes,
  filterNews,
  newsSubscriptionTree,
  podcastSubscriptionTree,
  subscriptionTree,
} from "./browse/wireFilters";
import { loadOfflineLibrary } from "./domain/library";
import { t } from "./i18n";
import { neighborArticle } from "./news/articleNav";
import { createNewsChrome } from "./news/readerChrome";
import { fetchFeedText, refreshNewsArticles, unreadCountFor } from "./news/refreshNews";
import { htmlForReaderMode } from "./news/renderArticleView";
import { createPodcastsChrome } from "./podcasts/paneChrome";
import { podcastShowsFromPool, refreshPodcastEpisodes } from "./podcasts/refreshPodcasts";
import {
  loadBrowsePrefs,
  loadReaderMode,
  saveBrowsePrefs,
  saveReaderMode,
} from "./settings/browsePrefs";
import { loadReaderSettings } from "./settings/readerPrefs";
import { loadLanClientPrefs } from "./sync/lanClientPrefs";
import {
  isArticleRead,
  isArticleStarred,
  markArticleRead,
  markArticleUnread,
  setArticleStarred,
  syncLanPool,
} from "./sync/lanPoolClient";

export type AppModuleTab = "news" | "podcasts" | "booru";

function tabButton(id: AppModuleTab, selected: AppModuleTab): string {
  return `<button type="button" role="tab" class="af-module-tab" data-module-tab="${id}" aria-selected="${selected === id}">${t(`nav.${id}`)}</button>`;
}

export function moduleNavMarkup(activeModule: AppModuleTab): string {
  return `<nav class="af-module-nav" role="tablist" aria-label="${t("app.title")}">${tabButton("news", activeModule)}${tabButton("podcasts", activeModule)}${tabButton("booru", activeModule)}</nav>`;
}

export function moduleHomeMarkup(activeModule: AppModuleTab): string {
  return `
      <div data-news-mount class="af-news-mount"${activeModule !== "news" ? " hidden" : ""}></div>
      <div data-podcasts-mount class="af-podcasts-mount"${activeModule !== "podcasts" ? " hidden" : ""}></div>
      <div data-booru-mount class="af-booru-mount"${activeModule !== "booru" ? " hidden" : ""}></div>`;
}

export function mountActiveModule(
  root: HTMLElement,
  activeModule: AppModuleTab,
  onModuleChange: (next: AppModuleTab) => void,
): void {
  for (const tab of root.querySelectorAll<HTMLButtonElement>("[data-module-tab]")) {
    tab.addEventListener("click", () => {
      const next = tab.dataset.moduleTab as AppModuleTab | undefined;
      if (!next || next === activeModule) return;
      onModuleChange(next);
    });
  }
  if (activeModule === "news") {
    const newsMount = root.querySelector<HTMLElement>("[data-news-mount]");
    if (newsMount) {
      let articles = loadOfflineLibrary().articles;
      const newsPrefs = loadBrowsePrefs("news");
      let newsUnreadOnly = newsPrefs.unreadOnly;
      let newsNode = newsPrefs.nodeId;
      let newsSort = newsPrefs.sort;
      let openArticle: Article | null = null;
      let readingOrder: Article[] = [];
      let readerHtml = "";
      let readerError = "";
      let pageHtml = "";
      let readerMode = loadReaderMode();
      const pushPoolIfConfigured = () => {
        const { hubUrl, passphrase } = loadLanClientPrefs();
        if (!hubUrl || !passphrase.trim()) return;
        void syncLanPool(hubUrl, passphrase).catch(() => {});
      };
      const openOne = (article: Article, resetMode = true) => {
        const settings = loadReaderSettings();
        if (settings.target !== "LAUNCHER") {
          markArticleRead(article);
          pushPoolIfConfigured();
          window.open(article.url, "_blank", "noopener");
          paint();
          return;
        }
        if (!openArticle) {
          readingOrder = filterNews(articles, newsUnreadOnly, newsNode, newsSort);
        }
        markArticleRead(article);
        pushPoolIfConfigured();
        openArticle = article;
        readerHtml = "";
        readerError = "";
        pageHtml = "";
        if (resetMode) readerMode = loadReaderMode();
        paint();
        void fetchFeedText(article.url)
          .then((html) => {
            if (openArticle?.id !== article.id) return;
            pageHtml = html;
            readerHtml = htmlForReaderMode(article, html, readerMode, loadReaderSettings());
            paint();
          })
          .catch((err: unknown) => {
            if (openArticle?.id !== article.id) return;
            readerError = String(err);
            paint();
          });
      };
      const paint = () => {
        const tree = newsSubscriptionTree(articles, newsUnreadOnly);
        if (
          newsNode &&
          !tree.some(
            (node) => node.id === newsNode || node.children?.some((child) => child.id === newsNode),
          )
        ) {
          newsNode = "";
        }
        const visible = filterNews(articles, newsUnreadOnly, newsNode, newsSort);
        const navList = openArticle ? readingOrder : visible;
        const prev = neighborArticle(navList, openArticle?.id, -1);
        const next = neighborArticle(navList, openArticle?.id, 1);
        createNewsChrome(newsMount, {
          unreadCount: unreadCountFor(articles),
          articles: visible,
          sourceCount: articles.length,
          browse: {
            nodes: tree,
            selectedId: newsNode,
            onSelect: (id) => {
              newsNode = id;
              saveBrowsePrefs("news", { nodeId: id });
              paint();
            },
            unreadOnly: newsUnreadOnly,
            onUnreadOnly: (value) => {
              newsUnreadOnly = value;
              saveBrowsePrefs("news", { unreadOnly: value });
              paint();
            },
            sort: newsSort,
            onSort: (nextSort) => {
              newsSort = nextSort;
              saveBrowsePrefs("news", { sort: nextSort });
              paint();
            },
          },
          status: articles.length ? "ready" : "loading",
          openArticle,
          readerHtml,
          readerError,
          readerMode,
          bodyScale: loadReaderSettings().bodyScale,
          canPrev: Boolean(prev),
          canNext: Boolean(next),
          onPrev: prev ? () => openOne(prev, false) : undefined,
          onNext: next ? () => openOne(next, false) : undefined,
          onReaderMode: (mode) => {
            readerMode = mode;
            saveReaderMode(mode);
            if (openArticle && pageHtml) {
              readerHtml = htmlForReaderMode(
                openArticle,
                pageHtml,
                readerMode,
                loadReaderSettings(),
              );
            }
            paint();
          },
          onReaderBack: () => {
            openArticle = null;
            readingOrder = [];
            readerHtml = "";
            readerError = "";
            pageHtml = "";
            readerMode = loadReaderMode();
            paint();
          },
          starred: openArticle ? isArticleStarred(openArticle) : false,
          read: openArticle ? isArticleRead(openArticle) : true,
          onToggleStar: () => {
            if (!openArticle) return;
            setArticleStarred(openArticle, !isArticleStarred(openArticle));
            pushPoolIfConfigured();
            paint();
          },
          onParkUnread: () => {
            if (!openArticle) return;
            markArticleUnread(openArticle);
            pushPoolIfConfigured();
            paint();
          },
          onOpenBrowser: () => {
            if (!openArticle) return;
            window.open(openArticle.url, "_blank", "noopener");
          },
          onArticleOpen: openOne,
        });
      };
      paint();
      void refreshNewsArticles().then((next) => {
        if (!newsMount.isConnected) return;
        articles = next;
        paint();
      });
    }
  }
  if (activeModule === "podcasts") {
    const podcastsMount = root.querySelector<HTMLElement>("[data-podcasts-mount]");
    if (podcastsMount) {
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
      paint(
        loadOfflineLibrary().episodes,
        loadOfflineLibrary().episodes.length ? "ready" : "loading",
      );
      void refreshPodcastEpisodes().then((next) => {
        if (!podcastsMount.isConnected) return;
        paint(next, "ready");
      });
    }
  }
  if (activeModule === "booru") {
    const booruMount = root.querySelector<HTMLElement>("[data-booru-mount]");
    if (booruMount) {
      const booruPrefs = loadBrowsePrefs("booru");
      let booruUnreadOnly = booruPrefs.unreadOnly;
      let booruNode = booruPrefs.nodeId;
      let booruSort = booruPrefs.sort;
      let booruQuery = "";
      const paint = () => {
        createBooruGridChrome(booruMount, {
          searchValue: booruQuery,
          onSearch: (query) => {
            booruQuery = query;
          },
          browse: {
            nodes: subscriptionTree("booru"),
            selectedId: booruNode,
            onSelect: (id) => {
              booruNode = id;
              saveBrowsePrefs("booru", { nodeId: id });
              paint();
            },
            unreadOnly: booruUnreadOnly,
            onUnreadOnly: (value) => {
              booruUnreadOnly = value;
              saveBrowsePrefs("booru", { unreadOnly: value });
              paint();
            },
            sort: booruSort,
            onSort: (nextSort) => {
              booruSort = nextSort;
              saveBrowsePrefs("booru", { sort: nextSort });
              paint();
            },
          },
        });
      };
      paint();
    }
  }
}
