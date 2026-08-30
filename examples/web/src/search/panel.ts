import { searchCopy as copy } from "./copy";
import { buildIndex, search } from "./engine";
import "./search.css";
import type { SearchDoc } from "./types";

const DEMO_DOCS: readonly SearchDoc[] = [
  { id: "demo-vault", title: "Local vault", body: "Encrypted articles stay on this device." },
  {
    id: "demo-queue",
    title: "Podcast queue",
    body: "Download episodes over Wi-Fi for offline play.",
  },
];

export type SearchPanelOptions = {
  docs?: readonly SearchDoc[];
};

function escapeHtml(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function resultsMarkup(query: string): string {
  const hits = search(query);
  if (!hits.length) {
    return `<p class="af-search-empty" data-search-empty>${escapeHtml(copy.empty)}</p>`;
  }
  const items = hits
    .map(
      (hit) =>
        `<li class="af-search-hit" data-search-hit data-search-id="${escapeHtml(hit.id)}">${escapeHtml(hit.title)}</li>`,
    )
    .join("");
  return `<ul class="af-search-results" data-search-hits>${items}</ul>`;
}

export function createSearchPanel(
  root: HTMLElement,
  options: SearchPanelOptions = {},
): HTMLElement {
  buildIndex(options.docs ?? DEMO_DOCS);
  root.className = "af-search";
  root.dataset.testid = "search-panel";
  root.setAttribute("aria-label", copy.title);
  root.innerHTML = `
    <section class="af-search-chrome" data-search-chrome>
      <form class="af-search-form" data-search-form>
        <label class="af-search-field">
          <span>${escapeHtml(copy.label)}</span>
          <input type="search" data-search-query placeholder="${escapeHtml(copy.placeholder)}" autocomplete="off" />
        </label>
        <button type="submit">${escapeHtml(copy.submit)}</button>
      </form>
      <div data-search-results aria-live="polite"></div>
    </section>
  `;
  const input = root.querySelector<HTMLInputElement>("[data-search-query]");
  const host = root.querySelector<HTMLElement>("[data-search-results]");
  const paint = (): void => {
    if (!host) return;
    host.innerHTML = resultsMarkup(input?.value ?? "");
  };
  root.querySelector("[data-search-form]")?.addEventListener("submit", (event) => {
    event.preventDefault();
    paint();
  });
  input?.addEventListener("input", paint);
  paint();
  return root;
}
