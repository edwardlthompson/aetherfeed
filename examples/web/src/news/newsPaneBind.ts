import type { Article } from "@aetherfeed/domain";
import { bindReaderSwipe, neighborArticle } from "./articleNav";
import { bindNewsSourcesDrawer } from "./newsDrawer";
import { bindNewsShare, dispatchLibraryPick } from "./newsPaneShare";
import { paintReader, warmNeighbors } from "./openArticle";
import { watchUnreadPrefetch } from "./prefetch";

export type NewsClickApi = {
  selectFolder(name: string): void;
  toggleFolder?(name: string): void;
  selectFeed(id: string): void;
  selectPick?(value: string): void;
  openId(id: string): void;
  toggleUnread(): void;
  toggleStar(): void;
};

export function bindNewsClicks(root: HTMLElement, api: NewsClickApi): void {
  root.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    const lib = target.closest<HTMLElement>("[data-library-pick]");
    if (lib?.dataset.libraryPick) {
      if (lib.dataset.libraryPick === "unified" || lib.dataset.libraryPick === "news") {
        api.selectPick?.(lib.dataset.libraryPick);
      }
      dispatchLibraryPick(lib.dataset.libraryPick);
      return;
    }
    const toggle = target.closest<HTMLElement>("[data-folder-toggle]");
    if (toggle?.dataset.folderToggle) {
      api.toggleFolder?.(toggle.dataset.folderToggle);
      return;
    }
    const folder = target.closest<HTMLElement>("[data-folder]");
    if (folder?.dataset.folder) {
      api.selectFolder(folder.dataset.folder);
      return;
    }
    const feedBtn = target.closest<HTMLElement>("[data-feed-id]");
    if (feedBtn?.dataset.feedId) {
      api.selectFeed(feedBtn.dataset.feedId);
      return;
    }
    const articleBtn = target.closest<HTMLElement>("[data-article-id]");
    if (articleBtn?.dataset.articleId) {
      api.openId(articleBtn.dataset.articleId);
      return;
    }
    if (target.closest("[data-news-unread]")) api.toggleUnread();
    else if (target.closest("[data-news-star]")) api.toggleStar();
  });
}

export type NewsAbortPair = { article?: AbortController; prefetch?: AbortController };

export function queueSelectedReader(
  root: HTMLElement,
  selected: Article | undefined,
  shown: Article[],
  unread: (id: string) => boolean,
  onThumbs: (thumbs: Record<string, string>, cached?: string[]) => void,
  pair: NewsAbortPair,
): void {
  const host = root.querySelector<HTMLElement>("[data-news-reader-host]");
  if (!host || !selected) return;
  pair.article?.abort();
  pair.prefetch?.abort();
  pair.article = new AbortController();
  warmNeighbors(shown, selected.id);
  void paintReader(host, selected, pair.article.signal).then((hit) => {
    if (hit) return;
    pair.prefetch = watchUnreadPrefetch(root, shown, unread, onThumbs, pair.prefetch, selected.id);
  });
}

let live: AbortController | undefined;

export function bindNewsChrome(
  root: HTMLElement,
  shown: Article[],
  selectedId: string | null,
  onOpen: (article: Article) => void,
  onRefresh: () => void,
  onSort: (oldestFirst: boolean) => void,
): void {
  live?.abort();
  live = new AbortController();
  const { signal } = live;
  bindNewsSourcesDrawer(root, signal);
  window.addEventListener("af-news-refresh", onRefresh, { signal });
  const step = (delta: number) => {
    const next = neighborArticle(shown, selectedId, delta);
    if (next) onOpen(next);
  };
  window.addEventListener("af-news-prev", () => step(-1), { signal });
  window.addEventListener("af-news-next", () => step(1), { signal });
  bindNewsShare(shown, selectedId, signal);
  window.addEventListener(
    "af-news-star",
    () => {
      const host = root.querySelector<HTMLElement>("[data-news-star]");
      host?.click();
    },
    { signal },
  );
  window.addEventListener(
    "af-news-unread",
    () => {
      const host = root.querySelector<HTMLElement>("[data-news-unread]");
      host?.click();
    },
    { signal },
  );
  window.addEventListener(
    "keydown",
    (event) => {
      if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
        return;
      }
      const delta = event.key === "j" ? 1 : event.key === "k" ? -1 : 0;
      const next = neighborArticle(shown, selectedId, delta);
      if (next) onOpen(next);
    },
    { signal },
  );
  bindReaderSwipe(
    root.querySelector("[data-news-reader]"),
    (delta) => {
      const next = neighborArticle(shown, selectedId, delta);
      if (next) onOpen(next);
    },
    signal,
  );
  const menu = root.querySelector<HTMLElement>("[data-news-sort-menu]");
  root.querySelector("[data-news-sort]")?.addEventListener(
    "click",
    (event) => {
      event.stopPropagation();
      if (menu) menu.hidden = !menu.hidden;
    },
    { signal },
  );
  root
    .querySelector("[data-news-sort-newest]")
    ?.addEventListener("click", () => onSort(false), { signal });
  root
    .querySelector("[data-news-sort-oldest]")
    ?.addEventListener("click", () => onSort(true), { signal });
}
