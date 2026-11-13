import type { Article } from "@aetherfeed/domain";
import { filterNews, newsSubscriptionTree } from "./browse/wireFilters";
import { neighborArticle } from "./news/articleNav";
import { createNewsChrome } from "./news/readerChrome";
import { unreadCountFor } from "./news/refreshNews";
import { htmlForReaderMode } from "./news/renderArticleView";
import { loadReaderMode, saveBrowsePrefs, saveReaderMode } from "./settings/browsePrefs";
import { loadReaderSettings } from "./settings/readerPrefs";
import {
  isArticleRead,
  isArticleStarred,
  markArticleUnread,
  setArticleStarred,
} from "./sync/lanPoolClient";

export type NewsMountState = {
  articles: Article[];
  newsUnreadOnly: boolean;
  newsNode: string;
  newsSort: import("./browse/contentFilter").SortOrder;
  openArticle: Article | null;
  readingOrder: Article[];
  readerHtml: string;
  readerError: string;
  pageHtml: string;
  readerMode: import("./news/readerChromeTypes").ReaderMode;
};

export type NewsMountCtx = NewsMountState & {
  newsMount: HTMLElement;
  openOne: (article: Article, resetMode?: boolean) => void;
  pushPoolIfConfigured: () => void;
};

export function paintNewsMount(ctx: NewsMountCtx): void {
  const tree = newsSubscriptionTree(ctx.articles, ctx.newsUnreadOnly);
  if (
    ctx.newsNode &&
    !tree.some(
      (node) =>
        node.id === ctx.newsNode || node.children?.some((child) => child.id === ctx.newsNode),
    )
  ) {
    ctx.newsNode = "";
  }
  const visible = filterNews(ctx.articles, ctx.newsUnreadOnly, ctx.newsNode, ctx.newsSort);
  const navList = ctx.openArticle ? ctx.readingOrder : visible;
  const prev = neighborArticle(navList, ctx.openArticle?.id, -1);
  const next = neighborArticle(navList, ctx.openArticle?.id, 1);
  createNewsChrome(ctx.newsMount, {
    unreadCount: unreadCountFor(ctx.articles),
    articles: visible,
    sourceCount: ctx.articles.length,
    browse: {
      nodes: tree,
      selectedId: ctx.newsNode,
      onSelect: (id) => {
        ctx.newsNode = id;
        saveBrowsePrefs("news", { nodeId: id });
        paintNewsMount(ctx);
      },
      unreadOnly: ctx.newsUnreadOnly,
      onUnreadOnly: (value) => {
        ctx.newsUnreadOnly = value;
        saveBrowsePrefs("news", { unreadOnly: value });
        paintNewsMount(ctx);
      },
      sort: ctx.newsSort,
      onSort: (nextSort) => {
        ctx.newsSort = nextSort;
        saveBrowsePrefs("news", { sort: nextSort });
        paintNewsMount(ctx);
      },
    },
    status: ctx.articles.length ? "ready" : "loading",
    openArticle: ctx.openArticle,
    readerHtml: ctx.readerHtml,
    readerError: ctx.readerError,
    readerMode: ctx.readerMode,
    bodyScale: loadReaderSettings().bodyScale,
    canPrev: Boolean(prev),
    canNext: Boolean(next),
    onPrev: prev ? () => ctx.openOne(prev, false) : undefined,
    onNext: next ? () => ctx.openOne(next, false) : undefined,
    onReaderMode: (mode) => {
      ctx.readerMode = mode;
      saveReaderMode(mode);
      if (ctx.openArticle && ctx.pageHtml) {
        ctx.readerHtml = htmlForReaderMode(
          ctx.openArticle,
          ctx.pageHtml,
          ctx.readerMode,
          loadReaderSettings(),
        );
      }
      paintNewsMount(ctx);
    },
    onReaderBack: () => {
      ctx.openArticle = null;
      ctx.readingOrder = [];
      ctx.readerHtml = "";
      ctx.readerError = "";
      ctx.pageHtml = "";
      ctx.readerMode = loadReaderMode();
      paintNewsMount(ctx);
    },
    starred: ctx.openArticle ? isArticleStarred(ctx.openArticle) : false,
    read: ctx.openArticle ? isArticleRead(ctx.openArticle) : true,
    onToggleStar: () => {
      if (!ctx.openArticle) return;
      setArticleStarred(ctx.openArticle, !isArticleStarred(ctx.openArticle));
      ctx.pushPoolIfConfigured();
      paintNewsMount(ctx);
    },
    onParkUnread: () => {
      if (!ctx.openArticle) return;
      markArticleUnread(ctx.openArticle);
      ctx.pushPoolIfConfigured();
      paintNewsMount(ctx);
    },
    onOpenBrowser: () => {
      if (!ctx.openArticle) return;
      window.open(ctx.openArticle.url, "_blank", "noopener");
    },
    onArticleOpen: ctx.openOne,
  });
}
