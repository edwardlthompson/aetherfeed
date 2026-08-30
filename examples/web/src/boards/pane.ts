import { createDigestSet, fileDigest, rememberDigest } from "../dedup";
import "./boards.css";
import { bindShareUrl } from "../news/newsPaneShare";
import { bindLibraryRoots } from "../shell/libraryTree";
import { type BoardClient, FetchBoardClient } from "./client";
import { boardCopy, messageForError } from "./copy";
import { toggleFavorite } from "./favorites";
import { mergeBoardPosts } from "./merge";
import { parseTagQuery } from "./parse";
import { chromeMarkup, emptyMarkup, errorMarkup, gridMarkup } from "./render";
import { addBoardSource, loadBoardSources } from "./sources";
import { BoardFetchError, type BoardSource } from "./types";

export type BoardsPaneOptions = {
  sources?: readonly BoardSource[];
  client?: BoardClient;
};

export type BoardsPaneHandle = {
  search: (raw: string) => Promise<void>;
  abort: () => void;
};

export function createBoardsPane(
  root: HTMLElement,
  options: BoardsPaneOptions = {},
): BoardsPaneHandle {
  let sources = options.sources ? [...options.sources] : loadBoardSources();
  const source = () => sources[0];
  const client = options.client ?? new FetchBoardClient();
  let abortCtrl: AbortController | undefined;
  let lastQuery = "";
  let selectedId = "";
  let selectedUrl = "";

  root.className = "af-boards";
  const paint = (): void => {
    root.innerHTML = chromeMarkup(sources.length > 0);
    bindLibraryRoots(root);
  };
  paint();

  const status = () => root.querySelector<HTMLElement>("[data-boards-status]");

  const abort = () => {
    abortCtrl?.abort();
    abortCtrl = undefined;
  };

  const runSearch = async (raw: string): Promise<void> => {
    const active = source();
    if (!active) return;
    const query = parseTagQuery(raw);
    lastQuery = raw;
    const host = status();
    if (!host) return;
    if (!query.tags.length) {
      host.innerHTML = emptyMarkup("search");
      return;
    }
    abort();
    abortCtrl = new AbortController();
    const signal = abortCtrl.signal;
    try {
      const posts = await client.search(active, query, signal);
      if (signal.aborted) return;
      const seen = createDigestSet();
      const unique: typeof posts = [];
      for (const post of mergeBoardPosts(posts)) {
        const digest = await fileDigest(new TextEncoder().encode(post.fileUrl));
        if (rememberDigest(seen, digest)) unique.push(post);
      }
      host.innerHTML = unique.length ? gridMarkup(unique) : emptyMarkup("search");
    } catch (err) {
      if (signal.aborted && !(err instanceof BoardFetchError && err.code === "aborted")) return;
      const typed = err instanceof BoardFetchError ? err : new BoardFetchError("network", "failed");
      host.innerHTML = errorMarkup(messageForError(typed.code), typed.retryable);
    }
  };

  root.addEventListener("submit", (event) => {
    const form = event.target;
    if (!(form instanceof HTMLFormElement)) return;
    event.preventDefault();
    if (form.matches("[data-boards-add]")) {
      const url = root.querySelector<HTMLInputElement>("[data-boards-url]")?.value ?? "";
      if (!url.trim()) return;
      addBoardSource(url);
      sources = options.sources ? [...options.sources, ...loadBoardSources()] : loadBoardSources();
      paint();
      return;
    }
    if (form.matches("[data-boards-form]")) {
      const input = root.querySelector<HTMLInputElement>("[data-boards-input]");
      void runSearch(input?.value ?? "");
    }
  });

  root.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    const card = target.closest<HTMLElement>("[data-board-id]");
    if (card?.dataset.boardId) {
      selectedId = card.dataset.boardId;
      selectedUrl = card.dataset.fileUrl ?? "";
    }
    const fav = target.closest<HTMLElement>("[data-board-fav]");
    const id = fav?.dataset.boardFav;
    if (fav && id) {
      const on = toggleFavorite(id);
      fav.setAttribute("aria-pressed", String(on));
      fav.textContent = on ? boardCopy.favoriteRemove : boardCopy.favoriteAdd;
      return;
    }
    if (target.closest("[data-boards-retry]")) void runSearch(lastQuery);
  });
  bindShareUrl(() => selectedUrl.trim() || sources[0]?.url);
  window.addEventListener("af-boards-fav", () => {
    if (!selectedId) return;
    const fav = root.querySelector<HTMLElement>(`[data-board-fav="${CSS.escape(selectedId)}"]`);
    fav?.click();
  });
  window.addEventListener("af-boards-info", () => {
    const host = status();
    const card = selectedId
      ? root.querySelector(`[data-board-id="${CSS.escape(selectedId)}"] img`)
      : null;
    if (host && card instanceof HTMLImageElement) host.textContent = card.alt;
  });

  return { search: runSearch, abort };
}
