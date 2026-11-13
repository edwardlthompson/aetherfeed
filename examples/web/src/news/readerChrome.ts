import type { Article } from "@aetherfeed/domain";
import { type BrowseNavOptions, createBrowseNav, wrapBrowse } from "../browse/browseChrome";
import { t } from "../i18n";
import { createArticleCards } from "./articleCards";
import { createReaderActions } from "./readerActions";
import { isWebReaderHtml } from "./renderArticleView";

export type ReaderMode = "READING" | "FULL" | "WEB";

export type NewsChromeOptions = {
  unreadCount: number;
  articles?: Article[];
  status?: "loading" | "ready";
  openArticle?: Article | null;
  readerHtml?: string;
  readerError?: string;
  readerMode?: ReaderMode;
  bodyScale?: number;
  onReaderMode?: (mode: ReaderMode) => void;
  onArticleOpen?: (article: Article) => void;
  onReaderBack?: () => void;
  onPrev?: () => void;
  onNext?: () => void;
  canPrev?: boolean;
  canNext?: boolean;
  browse?: BrowseNavOptions;
  sourceCount?: number;
  starred?: boolean;
  read?: boolean;
  onToggleStar?: () => void;
  onParkUnread?: () => void;
  onOpenBrowser?: () => void;
};

let detachKeys: (() => void) | undefined;

function unreadLabel(count: number): string {
  return t("news.unread_total").replace("{count}", String(count));
}

function edgeButton(
  kind: "prev" | "next",
  enabled: boolean,
  onClick?: () => void,
): HTMLButtonElement {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = `af-reader-edge af-reader-edge-${kind}`;
  btn.dataset.testid = kind === "prev" ? "article-prev" : "article-next";
  const label = t(kind === "prev" ? "feed.reader.previous" : "feed.reader.next");
  btn.setAttribute("aria-label", label);
  btn.title = label;
  btn.textContent = kind === "prev" ? "‹" : "›";
  btn.disabled = !enabled;
  btn.addEventListener("click", () => onClick?.());
  return btn;
}

export function createNewsChrome(root: HTMLElement, opts: NewsChromeOptions): void {
  detachKeys?.();
  detachKeys = undefined;
  document.body.classList.remove("af-reader-open", "af-reader-web");
  root.replaceChildren();

  const pane = document.createElement("section");
  pane.className = "af-news-pane";
  pane.dataset.testid = "news-pane";
  pane.setAttribute("aria-label", t("news.title"));

  const articles = opts.articles ?? [];
  const opened = opts.openArticle;
  if (!opened) {
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
    if (opts.browse) wrapBrowse(pane, createBrowseNav(opts.browse), main);
    else pane.append(main);
    root.append(pane);
    return;
  }

  document.body.classList.add("af-reader-open");
  const back = document.createElement("button");
  back.type = "button";
  back.className = "af-news-back";
  back.dataset.testid = "article-reader-back";
  back.textContent = t("nav.back");
  back.addEventListener("click", () => opts.onReaderBack?.());
  const heading = document.createElement("h3");
  heading.className = "af-reader-heading";
  heading.dataset.testid = "article-reader-title";
  heading.textContent = opened.title;
  const toolbar = document.createElement("header");
  toolbar.className = "af-reader-toolbar";
  toolbar.append(
    back,
    heading,
    createReaderActions({
      starred: Boolean(opts.starred),
      read: opts.read !== false,
      readerMode: opts.readerMode ?? "READING",
      onToggleStar: () => opts.onToggleStar?.(),
      onParkUnread: () => opts.onParkUnread?.(),
      onOpenBrowser: () => opts.onOpenBrowser?.() ?? window.open(opened.url, "_blank", "noopener"),
      onReaderMode: (mode) => opts.onReaderMode?.(mode),
    }),
  );
  pane.append(toolbar);
  const activeMode: ReaderMode = opts.readerMode ?? "READING";
  if (activeMode === "WEB") document.body.classList.add("af-reader-web");
  pane.append(edgeButton("prev", Boolean(opts.canPrev), opts.onPrev));
  pane.append(edgeButton("next", Boolean(opts.canNext), opts.onNext));
  const onKey = (event: KeyboardEvent) => {
    if (event.key === "ArrowLeft") opts.onPrev?.();
    if (event.key === "ArrowRight") opts.onNext?.();
  };
  window.addEventListener("keydown", onKey);
  detachKeys = () => {
    window.removeEventListener("keydown", onKey);
    document.body.classList.remove("af-reader-open", "af-reader-web");
  };
  if (opts.readerError) {
    const err = document.createElement("p");
    err.dataset.testid = "article-reader-error";
    err.textContent = opts.readerError;
    pane.append(err);
  } else if (!opts.readerHtml) {
    const wait = document.createElement("p");
    wait.dataset.testid = "article-reader-loading";
    wait.textContent = t("news.reader.loading");
    pane.append(wait);
  } else if (activeMode === "WEB" || isWebReaderHtml(opts.readerHtml)) {
    const frame = document.createElement("iframe");
    frame.className = "af-news-reader-frame";
    frame.dataset.testid = "article-reader-frame";
    frame.title = opened.title;
    frame.srcdoc = opts.readerHtml;
    pane.append(frame);
  } else {
    const article = document.createElement("article");
    article.className = "af-reader-article";
    article.dataset.testid = "article-reader-body";
    article.style.fontSize = `${Math.round((opts.bodyScale ?? 1) * 100)}%`;
    article.innerHTML = opts.readerHtml;
    pane.append(article);
  }
  root.append(pane);
}
