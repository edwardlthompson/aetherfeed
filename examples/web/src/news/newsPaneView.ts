import type { Article, Feed } from "@aetherfeed/domain";
import { t } from "../i18n";
import { libraryRootsHtml } from "../shell/libraryTree";
import type { ArticleFlags } from "./articleFlags";
import { ageLabel, listSnippet } from "./newsFormat";
import { unreadBadge } from "./unreadCounts";

export function escapeHtml(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function rowAge(publishedAt: number | undefined): string {
  const raw = ageLabel(publishedAt);
  return raw === "now" ? t("news.age.now") : raw;
}

export function renderSourceTree(
  groups: [string, Feed[]][],
  selectedFolder: string | null,
  selectedFeedId: string | null,
  expanded: readonly string[] = [],
  folderUnread: Record<string, number> = {},
  feedUnread: Record<string, number> = {},
): string {
  const open = new Set(expanded);
  const blocks = groups
    .map(([name, feeds]) => {
      const isOpen = open.has(name);
      const folderCurrent = selectedFolder === name ? ` aria-current="true"` : "";
      const items = isOpen
        ? feeds
            .map((feed) => {
              const current = selectedFeedId === feed.id ? ` aria-current="true"` : "";
              return `<li><button type="button" class="af-news-feed" data-feed-id="${escapeHtml(feed.id)}"${current}>${escapeHtml(feed.title)}${unreadBadge(feedUnread[feed.id], "feed")}</button></li>`;
            })
            .join("")
        : "";
      return `<section class="af-news-folder-block"><button type="button" class="af-news-folder-chevron" data-folder-toggle="${escapeHtml(name)}" aria-expanded="${isOpen}"></button><button type="button" class="af-news-folder" data-folder="${escapeHtml(name)}"${folderCurrent}><span class="af-news-folder-name">${escapeHtml(name)}</span>${unreadBadge(folderUnread[name], "folder")}</button>${isOpen ? `<ul class="af-news-folder-feeds">${items}</ul>` : ""}</section>`;
    })
    .join("");
  const newsUnread = Object.values(folderUnread).reduce((sum, n) => sum + n, 0);
  return `<nav class="af-news-source-tree" aria-label="${t("news.folders")}" data-news-folders data-news-feeds>${libraryRootsHtml(newsUnread)}${blocks}</nav>`;
}

export function renderArticles(
  articles: Article[],
  selectedId: string | null,
  flags: ArticleFlags,
  feedTitleOf: (article: Article) => string,
  thumbs: Record<string, string> = {},
  cached: ReadonlySet<string> = new Set(),
): string {
  const showFeedName = new Set(articles.map((article) => article.feedId)).size > 1;
  const items = articles
    .map((article) => {
      const current = selectedId === article.id ? ` aria-current="true"` : "";
      const unread = flags.isUnread(article.id);
      const starred = flags.isStarred(article.id);
      const feed = showFeedName ? feedTitleOf(article) : "";
      const snippet = listSnippet(article.summary ?? article.contentHtml);
      const age = rowAge(article.publishedAt);
      const state = unread ? " is-unread" : " is-read";
      const star = starred ? " is-starred" : "";
      const selected = selectedId === article.id ? " is-selected" : "";
      const dot = unread ? `<span class="af-news-row-dot" aria-hidden="true"></span>` : "";
      const cachedEl = cached.has(article.id)
        ? `<span class="af-news-cached" data-news-cached aria-label="${t("news.cached")}"></span>`
        : "";
      const feedEl = feed ? `<span class="af-news-row-feed">${escapeHtml(feed)}</span>` : "";
      const starEl = starred ? `<span class="af-news-row-star" aria-hidden="true"></span>` : "";
      const ageEl = age ? `<span class="af-news-row-age">${escapeHtml(age)}</span>` : "";
      const snippetEl = snippet
        ? `<span class="af-news-row-snippet">${escapeHtml(snippet)}</span>`
        : "";
      const thumb = thumbs[article.id];
      const thumbEl = thumb
        ? `<img class="af-news-thumb" data-news-thumb alt="" src="${escapeHtml(thumb)}">`
        : "";
      return `<li><button type="button" class="af-news-row${state}${star}${selected}" data-article-id="${escapeHtml(article.id)}"${current}>${thumbEl}<span class="af-news-row-copy"><span class="af-news-row-meta">${dot}${cachedEl}${feedEl}${starEl}${ageEl}</span><span class="af-news-row-title">${escapeHtml(article.title)}</span>${snippetEl}</span></button></li>`;
    })
    .join("");
  return `<ul class="af-news-articles" aria-label="${t("news.timeline")}">${items}</ul>`;
}

export function renderEmptyReader(): string {
  return `<p data-news-empty>${t("news.empty")}</p><p>${t("news.reader_hint")}</p>`;
}

export function renderNewsShell(
  sidebarHtml: string,
  timelineHtml: string,
  readerHtml: string,
  status: string,
  chrome: { source: number; timeline: number; reader: number; oldestFirst?: boolean } = {
    source: 0.28,
    timeline: 0.34,
    reader: 0.38,
  },
): string {
  const cols = `${chrome.source}fr 0.4rem ${chrome.timeline}fr 0.4rem ${chrome.reader}fr`;
  return `
      <div class="af-news-columns" style="grid-template-columns:${cols}">
        <aside class="af-news-sidebar">
          ${sidebarHtml}
        </aside>
        <div class="af-news-scrim" data-news-scrim hidden></div>
        <div class="af-news-split" data-split="source" role="separator" aria-orientation="vertical"></div>
        <section class="af-news-timeline" data-news-timeline aria-label="${t("news.timeline")}">
          <div class="af-news-actions">
            <button type="button" class="af-news-sources" data-news-sources aria-label="${t("news.sources")}">☰</button>
            <button type="button" class="af-news-filter" data-news-sort aria-label="${t("news.filter")}" aria-haspopup="menu">
              <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true"><path fill="currentColor" d="M10 18h4v-2h-4zm-7-11v2h18V7zm3 6h12v-2H6z"/></svg>
            </button>
            <button type="button" hidden data-news-unread></button>
            <button type="button" hidden data-news-star></button>
            <div class="af-news-filter-menu" data-news-sort-menu hidden>
              <button type="button" data-news-sort-newest>${t("news.sort.newest")}</button>
              <button type="button" data-news-sort-oldest>${t("news.sort.oldest")}</button>
            </div>
          </div>
          <p class="af-news-status" data-news-status data-news-feed-status aria-live="polite">${escapeHtml(status)}</p>
          ${timelineHtml}
        </section>
        <div class="af-news-split" data-split="timeline" role="separator" aria-orientation="vertical"></div>
        <section class="af-news-reader" data-news-reader aria-label="${t("nav.news")}">
          ${readerHtml}
        </section>
      </div>
    `;
}

export function renderArticleBody(article: Article | undefined): string {
  if (!article) return `<p>${t("news.reader_hint")}</p>`;
  return `<article class="af-news-article" data-news-article><h2>${escapeHtml(article.title)}</h2></article>`;
}
