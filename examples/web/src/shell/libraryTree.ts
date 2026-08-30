import { t } from "../i18n";

export function wrapLibraryTree(body: string, newsUnread = 0): string {
  return `<nav class="af-news-source-tree" data-news-folders data-news-feeds>${libraryRootsHtml(newsUnread)}</nav>${body}`;
}

export function bindLibraryRoots(root: HTMLElement): void {
  root.addEventListener("click", (event) => {
    const lib = (event.target as HTMLElement | null)?.closest?.("[data-library-pick]");
    if (lib instanceof HTMLElement && lib.dataset.libraryPick) {
      window.dispatchEvent(new CustomEvent("af-library-pick", { detail: lib.dataset.libraryPick }));
    }
  });
}

export function libraryRootsHtml(newsUnread = 0): string {
  const newsHidden = newsUnread <= 0 ? " hidden" : "";
  return (
    `<button type="button" class="af-news-folder" data-library-pick="unified">${t("nav.unified")}` +
    `<span class="af-nav-unread" data-unified-unread hidden></span></button>` +
    `<button type="button" class="af-news-folder" data-library-pick="news" data-nav-unread-host>${t("nav.news")}` +
    `<span class="af-nav-unread" data-nav-unread="news"${newsHidden}>${newsUnread}</span></button>` +
    `<button type="button" class="af-news-folder" data-library-pick="podcast">${t("nav.podcasts")}` +
    `<span class="af-nav-unread" data-nav-unread="podcast" hidden>0</span></button>` +
    `<button type="button" class="af-news-folder" data-library-pick="booru">${t("nav.boards")}` +
    `<span class="af-nav-unread" data-nav-unread="booru" hidden>0</span></button>`
  );
}
