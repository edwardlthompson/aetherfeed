import type { Article, Feed } from "@aetherfeed/domain";
import { loadArticleIndex } from "./articleIndex";
import { sortArticles } from "./articleNav";
import { dropIfUnstarred, sweepArticleCache } from "./cacheRetain";
import {
  type NewsChromePrefs,
  restoreNewsLocation,
  saveNewsChrome,
  toggleExpanded,
  withLocation,
} from "./chromePrefs";
import { groupNewsByFolder } from "./folders";
import type { MemoryNewsRepository } from "./memoryRepo";
import { headlinesFromAll, loadTimeline, visibleArticles } from "./newsPaneActions";
import { bindNewsClicks, type NewsAbortPair } from "./newsPaneBind";
import { scopedIds } from "./newsPaneScoped";
import { watchUnreadPrefetch } from "./prefetch";

export async function runNewsLoad(
  repo: MemoryNewsRepository,
  feeds: Feed[],
  feedId: string | null,
  folder: string | null,
  unified: boolean,
  articleId: string | null,
  force: boolean,
  oldestFirst: boolean,
  root: HTMLElement,
  unread: (id: string) => boolean,
  aborts: NewsAbortPair,
  apply: (next: { articles: Article[]; status: string; thumbs: Record<string, string> }) => void,
  onThumbs: (thumbs: Record<string, string>, cached?: string[]) => void,
  extras: Article[] = [],
): Promise<void> {
  const ids = scopedIds(feeds, unified ? null : feedId, unified ? null : folder);
  apply(await loadTimeline(repo, ids, force, extras));
  void sweepArticleCache(force);
  if (!articleId) {
    const queue = sortArticles(
      visibleArticles(await headlinesFromAll(repo, feeds, force)),
      oldestFirst,
    );
    aborts.prefetch = watchUnreadPrefetch(root, queue, unread, onThumbs, aborts.prefetch);
  }
}

export function bootNewsPane(
  root: HTMLElement,
  feeds: Feed[],
  repo: MemoryNewsRepository,
  flags: {
    toggleUnread(id: string): void;
    toggleStar(id: string): void;
    isStarred(id: string): boolean;
  },
  shownOf: () => Article[],
  openArticle: (article: Article) => void,
  loadArticles: (force?: boolean) => void,
  render: () => void,
  chrome: { get(): NewsChromePrefs; set(next: NewsChromePrefs): void },
  nav: {
    setFolder(name: string | null): void;
    setFeed(id: string | null): void;
    setArticle(id: string | null): void;
    setPick(kind: "source" | "folder" | "all" | "unified"): void;
    articleId(): string | null;
    setHeadlines(rows: Article[]): void;
  },
): void {
  bindNewsClicks(root, {
    toggleFolder: (name) => {
      chrome.set(toggleExpanded(chrome.get(), name));
      saveNewsChrome(chrome.get());
      render();
    },
    selectFolder: (name) => {
      nav.setFolder(name);
      nav.setFeed(null);
      nav.setArticle(null);
      nav.setPick("folder");
      let next = withLocation(chrome.get(), name, null);
      if (!next.expanded.includes(name)) next = toggleExpanded(next, name);
      chrome.set(next);
      saveNewsChrome(chrome.get());
      render();
      loadArticles();
    },
    selectFeed: (id) => {
      const folder =
        groupNewsByFolder(feeds).find(([, rows]) => rows.some((feed) => feed.id === id))?.[0] ??
        null;
      nav.setFolder(folder);
      nav.setFeed(id);
      nav.setArticle(null);
      nav.setPick("source");
      chrome.set(withLocation(chrome.get(), folder, id));
      saveNewsChrome(chrome.get());
      loadArticles(true);
    },
    selectPick: (value) => {
      nav.setFeed(null);
      nav.setArticle(null);
      nav.setPick(value === "unified" ? "unified" : "all");
      if (value !== "unified") nav.setFolder(null);
      loadArticles();
    },
    openId: (id) => {
      const found = shownOf().find((article) => article.id === id);
      if (found) openArticle(found);
    },
    toggleUnread: () => {
      const id = nav.articleId();
      if (id) {
        flags.toggleUnread(id);
        render();
      }
    },
    toggleStar: () => {
      const id = nav.articleId();
      if (!id) return;
      const was = flags.isStarred(id);
      flags.toggleStar(id);
      if (was) void dropIfUnstarred(id);
      render();
    },
  });
  const groups = groupNewsByFolder(feeds);
  const prior = chrome.get();
  const here = restoreNewsLocation(groups, prior);
  nav.setFolder(here.folder);
  nav.setFeed(here.feedId);
  nav.setPick(here.feedId ? "source" : here.folder ? "folder" : "all");
  if (prior.folder || prior.feedId) {
    chrome.set(withLocation(prior, here.folder, here.feedId));
    saveNewsChrome(chrome.get());
  }
  render();
  void loadArticleIndex().then((rows) => {
    repo.hydrate(rows);
    nav.setHeadlines(rows);
    loadArticles();
  });
}
