import { type BrowseNavOptions, createBrowseNav, wrapBrowse } from "../browse/browseChrome";
import { t } from "../i18n";
import { createArticleCards } from "./articleCards";
import { unreadLabel } from "./readerChromeHelpers";
import type { NewsChromeOptions } from "./readerChromeTypes";

export function mountNewsListView(
  pane: HTMLElement,
  root: HTMLElement,
  opts: NewsChromeOptions,
  browse?: BrowseNavOptions,
): void {
  const articles = opts.articles ?? [];
  const main = document.createElement("div");
  main.className = "af-browse-list";
  const unread = document.createElement("p");
  unread.className = "af-news-unread";
  unread.dataset.testid = "news-unread";
  unread.textContent = unreadLabel(opts.unreadCount);
  main.append(unread);
  if (articles.length === 0) {
    const body = document.createElement("p");
    body.className = "af-news-body";
    body.dataset.testid = "news-body";
    const filtered = (opts.sourceCount ?? 0) > 0;
    body.textContent =
      opts.status === "loading"
        ? t("news.loading")
        : filtered
          ? t("filter.empty")
          : t("news.pane.body");
    main.append(body);
  } else {
    main.append(createArticleCards(articles, opts.onArticleOpen));
  }
  if (browse) wrapBrowse(pane, createBrowseNav(browse), main);
  else pane.append(main);
  root.append(pane);
}
