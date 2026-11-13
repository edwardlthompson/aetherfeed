import { t } from "../i18n";
import { mountNewsListView } from "./readerChromeList";
import { mountNewsReaderView } from "./readerChromeOpen";
import { resetDetachReaderKeys } from "./readerChromeState";
import type { NewsChromeOptions } from "./readerChromeTypes";

export type { NewsChromeOptions, ReaderMode } from "./readerChromeTypes";

export function createNewsChrome(root: HTMLElement, opts: NewsChromeOptions): void {
  resetDetachReaderKeys();
  document.body.classList.remove("af-reader-open", "af-reader-web");
  root.replaceChildren();

  const pane = document.createElement("section");
  pane.className = "af-news-pane";
  pane.dataset.testid = "news-pane";
  pane.setAttribute("aria-label", t("news.title"));

  if (!opts.openArticle) {
    mountNewsListView(pane, root, opts, opts.browse);
    return;
  }
  mountNewsReaderView(pane, root, opts);
}
