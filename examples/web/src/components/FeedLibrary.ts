import type { Feed } from "@aetherfeed/domain";
import { t } from "../i18n";
import { loadImportedFeeds } from "../readerimport/importedStore";

function escapeHtml(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function hostOf(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch {
    return url;
  }
}

function fallbackFolder(feed: Feed): string {
  return feed.kind === "podcast"
    ? t("readerimport.library.podcasts")
    : t("readerimport.library.news");
}

function grouped(feeds: Feed[]): [string, Feed[]][] {
  const buckets = new Map<string, Feed[]>();
  for (const feed of feeds) {
    const heading = feed.folder?.trim() || fallbackFolder(feed);
    const list = buckets.get(heading) ?? [];
    list.push(feed);
    buckets.set(heading, list);
  }
  return [...buckets.entries()].sort(([left], [right]) => left.localeCompare(right));
}

function section(title: string, feeds: Feed[]): string {
  if (!feeds.length) return "";
  const items = feeds
    .map(
      (feed) =>
        `<li><span class="af-feed-title">${escapeHtml(feed.title)}</span><span class="af-feed-host">${escapeHtml(hostOf(feed.url))}</span></li>`,
    )
    .join("");
  return `<h3>${escapeHtml(title)}</h3><ul class="af-feed-list">${items}</ul>`;
}

export function renderFeedLibrary(root: HTMLElement): void {
  const feeds = loadImportedFeeds();
  root.className = "af-feed-library";
  root.dataset.testid = "feed-library";
  if (!feeds.length) {
    root.innerHTML = `<p data-feed-empty>${t("readerimport.library.empty")}</p>`;
    return;
  }
  root.innerHTML = `
    <p data-feed-count>${t("readerimport.library.count").replace("{count}", String(feeds.length))}</p>
    ${grouped(feeds)
      .map(([heading, items]) => section(heading, items))
      .join("")}
  `;
}

export function createFeedLibrary(): HTMLElement {
  const root = document.createElement("section");
  renderFeedLibrary(root);
  return root;
}
