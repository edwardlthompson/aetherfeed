import type { Article } from "@aetherfeed/domain";
import { loadImportedFeeds } from "../readerimport/importedStore";
import { createArticleFlags } from "./articleFlags";
import { sortArticles } from "./articleNav";
import { loadNewsChrome, saveNewsChrome } from "./chromePrefs";
import { newsFeedsOnly } from "./folders";
import { MemoryNewsRepository } from "./memoryRepo";
import { unifiedExtras, visibleArticles } from "./newsPaneActions";
import { type NewsAbortPair } from "./newsPaneBind";
import { bootNewsPane, runNewsLoad } from "./newsPaneLoad";
import { paintNewsPane } from "./newsPanePaint";
import "./news.css";

export function createNewsPane(root: HTMLElement): HTMLElement {
  const allFeeds = loadImportedFeeds();
  const feeds = newsFeedsOnly(allFeeds);
  const repo = new MemoryNewsRepository({ feeds });
  const flags = createArticleFlags();
  let selectedFolder: string | null = null;
  let selectedFeedId: string | null = null;
  let selectedArticleId: string | null = null;
  let pickKind: "source" | "folder" | "all" | "unified" = "all";
  let articles: Article[] = [];
  let headlines: Article[] = [];
  let status = "";
  let thumbs: Record<string, string> = {};
  let cachedIds = new Set<string>();
  const aborts: NewsAbortPair = {};
  let chrome = loadNewsChrome();
  root.classList.add("af-news-pane");
  root.dataset.testid = "news-pane";
  const shownOf = (): Article[] => sortArticles(visibleArticles(articles), chrome.oldestFirst);
  const openArticle = (article: Article): void => {
    selectedArticleId = article.id;
    flags.setRead(article.id, true);
    render();
  };
  const render = (): void => {
    paintNewsPane(
      root,
      feeds,
      shownOf(),
      selectedFolder,
      selectedFeedId,
      selectedArticleId,
      headlines,
      flags,
      thumbs,
      cachedIds,
      status,
      chrome,
      aborts,
      (next) => {
        chrome = next;
        render();
      },
      openArticle,
      () => {
        void loadArticles(true);
      },
      (oldestFirst) => {
        chrome = { ...chrome, oldestFirst };
        saveNewsChrome(chrome);
        render();
      },
      (next, cached) => {
        thumbs = next;
        if (cached) cachedIds = new Set(cached);
      },
    );
  };
  const loadArticles = async (force = false): Promise<void> => {
    await runNewsLoad(
      repo,
      feeds,
      selectedFeedId,
      selectedFolder,
      pickKind === "unified",
      selectedArticleId,
      force,
      chrome.oldestFirst,
      root,
      (id) => flags.isUnread(id),
      aborts,
      (next) => {
        articles = next.articles;
        status = next.status;
        thumbs = next.thumbs;
        render();
      },
      (rows, cached) => {
        thumbs = rows;
        if (cached) cachedIds = new Set(cached);
      },
      pickKind === "unified" ? unifiedExtras(allFeeds) : [],
    );
  };
  bootNewsPane(
    root,
    feeds,
    repo,
    flags,
    shownOf,
    openArticle,
    loadArticles,
    render,
    {
      get: () => chrome,
      set: (next) => {
        chrome = next;
      },
    },
    {
      setFolder: (name) => {
        selectedFolder = name;
      },
      setFeed: (id) => {
        selectedFeedId = id;
        pickKind = id ? "source" : pickKind;
      },
      setArticle: (id) => {
        selectedArticleId = id;
      },
      setPick: (kind) => {
        pickKind = kind;
      },
      articleId: () => selectedArticleId,
      setHeadlines: (rows) => {
        headlines = rows;
      },
    },
  );
  return root;
}
