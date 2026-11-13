import type { Article } from "@aetherfeed/domain";
import { t } from "../i18n";
import { createReaderActions } from "./readerActions";
import { edgeButton } from "./readerChromeHelpers";
import { setDetachReaderKeys } from "./readerChromeState";
import type { NewsChromeOptions, ReaderMode } from "./readerChromeTypes";
import { isWebReaderHtml } from "./renderArticleView";

export function mountNewsReaderView(
  pane: HTMLElement,
  root: HTMLElement,
  opts: NewsChromeOptions,
): void {
  const opened = opts.openArticle as Article;
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
  setDetachReaderKeys(() => {
    window.removeEventListener("keydown", onKey);
    document.body.classList.remove("af-reader-open", "af-reader-web");
  });
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
