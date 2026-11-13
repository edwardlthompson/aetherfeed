import { type BrowseNavOptions, createBrowseNav, wrapBrowse } from "../browse/browseChrome";
import { t } from "../i18n";

export type BooruGridChromeOptions = {
  isEmpty?: boolean;
  onSearch?: (query: string) => void;
  onFavorite?: () => void;
  browse?: BrowseNavOptions;
  searchValue?: string;
};

export function createBooruGridChrome(root: HTMLElement, opts: BooruGridChromeOptions = {}): void {
  const isEmpty = opts.isEmpty ?? true;
  root.replaceChildren();

  const pane = document.createElement("section");
  pane.className = "af-booru-pane";
  pane.dataset.testid = "booru-pane";
  pane.setAttribute("aria-label", t("booru.title"));

  const title = document.createElement("h2");
  title.className = "af-booru-title";
  title.dataset.testid = "booru-title";
  title.textContent = t("booru.title");

  const searchLabel = document.createElement("label");
  searchLabel.className = "af-booru-search-label";
  searchLabel.dataset.testid = "booru-search-label";
  searchLabel.textContent = t("booru.search_placeholder");
  searchLabel.htmlFor = "af-booru-search";

  const search = document.createElement("input");
  search.type = "search";
  search.id = "af-booru-search";
  search.className = "af-booru-search";
  search.dataset.testid = "booru-search";
  search.placeholder = t("booru.search_placeholder");
  search.autocomplete = "off";
  search.value = opts.searchValue ?? "";
  search.addEventListener("input", () => {
    opts.onSearch?.(search.value.trim());
  });

  searchLabel.append(search);

  const favorite = document.createElement("button");
  favorite.type = "button";
  favorite.className = "af-booru-favorite";
  favorite.dataset.testid = "booru-favorite";
  favorite.setAttribute("aria-label", t("booru.favorite"));
  favorite.textContent = t("booru.favorite");
  favorite.addEventListener("click", () => {
    opts.onFavorite?.();
  });

  const main = document.createElement("div");
  main.className = "af-browse-list";
  main.append(title, searchLabel, favorite);

  if (isEmpty) {
    const empty = document.createElement("p");
    empty.className = "af-booru-empty";
    empty.dataset.testid = "booru-empty";
    empty.textContent = t("booru.empty");
    main.append(empty);
  }

  if (opts.browse) wrapBrowse(pane, createBrowseNav(opts.browse), main);
  else pane.append(main);
  root.append(pane);
}
