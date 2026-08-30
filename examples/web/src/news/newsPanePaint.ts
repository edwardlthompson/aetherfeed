import type { Article, Feed } from "@aetherfeed/domain";
import type { ArticleFlags } from "./articleFlags";
import type { NewsChromePrefs } from "./chromePrefs";
import { afterNewsRender } from "./newsPaneAfter";
import type { NewsAbortPair } from "./newsPaneBind";
import { renderArticles, renderEmptyReader, renderNewsShell } from "./newsPaneView";
import { renderNewsTree } from "./newsTreeRender";

export function paintNewsPane(
  root: HTMLElement,
  feeds: Feed[],
  shown: Article[],
  selectedFolder: string | null,
  selectedFeedId: string | null,
  selectedArticleId: string | null,
  headlines: Article[],
  flags: ArticleFlags,
  thumbs: Record<string, string>,
  cachedIds: ReadonlySet<string>,
  status: string,
  chrome: NewsChromePrefs,
  aborts: NewsAbortPair,
  onChrome: (next: NewsChromePrefs) => void,
  onOpen: (article: Article) => void,
  onRefresh: () => void,
  onSort: (oldestFirst: boolean) => void,
  onThumbs: (thumbs: Record<string, string>, cached?: string[]) => void,
): void {
  const selected = shown.find((article) => article.id === selectedArticleId);
  const titleOf = (article: Article) =>
    feeds.find((feed) => feed.id === article.feedId)?.title ?? "";
  const timeline = feeds.length
    ? renderArticles(shown, selectedArticleId, flags, titleOf, thumbs, cachedIds)
    : "";
  root.innerHTML = renderNewsShell(
    renderNewsTree(feeds, selectedFolder, selectedFeedId, chrome.expanded, headlines, (id) =>
      flags.isUnread(id),
    ),
    timeline,
    feeds.length ? `<div data-news-reader-host></div>` : renderEmptyReader(),
    status,
    chrome,
  );
  afterNewsRender(
    root,
    shown,
    selected,
    selectedArticleId,
    chrome,
    shown.filter((article) => flags.isUnread(article.id)).length,
    onChrome,
    onOpen,
    onRefresh,
    onSort,
    onThumbs,
    aborts,
    (id) => flags.isUnread(id),
  );
}
