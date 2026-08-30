import { wrapLibraryTree } from "../shell/libraryTree";
import { boardCopy } from "./copy";
import { isFavorite } from "./favorites";
import type { BoardPost } from "./types";

function escapeHtml(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

export function emptyMarkup(kind: "sources" | "search"): string {
  const copy = kind === "sources" ? boardCopy.emptySources : boardCopy.emptySearch;
  const testid = kind === "sources" ? "boards-empty-sources" : "boards-empty-search";
  return `<p class="af-boards-empty" data-boards-empty data-${testid}>${escapeHtml(copy)}</p>`;
}

export function errorMarkup(message: string, retryable: boolean): string {
  const retry = retryable
    ? `<button type="button" class="af-boards-retry" data-boards-retry>${escapeHtml(boardCopy.retry)}</button>`
    : "";
  return `<p class="af-boards-error" data-boards-error role="alert">${escapeHtml(message)}</p>${retry}`;
}

export function gridMarkup(posts: BoardPost[]): string {
  const cards = posts
    .map((post) => {
      const src = escapeHtml(post.previewUrl ?? post.fileUrl);
      const fav = isFavorite(post.id);
      const label = fav ? boardCopy.favoriteRemove : boardCopy.favoriteAdd;
      return `<article class="af-boards-card" data-board-id="${escapeHtml(post.id)}" data-file-url="${escapeHtml(post.fileUrl)}">
  <img src="${src}" alt="${escapeHtml(post.tags.slice(0, 6).join(" "))}" />
  <button type="button" class="af-boards-fav" data-board-fav="${escapeHtml(post.id)}" aria-pressed="${fav}">${escapeHtml(label)}</button>
</article>`;
    })
    .join("");
  return `<div class="af-boards-grid" data-boards-grid>${cards}</div>`;
}

export function formMarkup(): string {
  return `<form class="af-boards-form" data-boards-form>
  <label class="af-boards-field">
    <span>${escapeHtml(boardCopy.searchLabel)}</span>
    <input type="search" name="tags" data-boards-input placeholder="${escapeHtml(boardCopy.searchPlaceholder)}" />
  </label>
  <button type="submit" data-boards-submit>${escapeHtml(boardCopy.searchSubmit)}</button>
</form>
<div class="af-boards-status" data-boards-status aria-live="polite"></div>`;
}

export function chromeMarkup(withForm: boolean): string {
  const add = `<form data-boards-add>
    <label>${escapeHtml(boardCopy.sourceUrl)}
      <input type="url" data-boards-url placeholder="https://" />
    </label>
    <button type="submit">${escapeHtml(boardCopy.addSource)}</button>
  </form>`;
  return wrapLibraryTree(`<section class="af-boards-pane" data-boards-pane aria-label="${escapeHtml(boardCopy.title)}">
  <h2>${escapeHtml(boardCopy.title)}</h2>
  ${add}
  ${withForm ? formMarkup() : emptyMarkup("sources")}
</section>`);
}
