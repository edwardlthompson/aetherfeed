import type { Article } from "@aetherfeed/domain";
import { filterNews } from "./browse/wireFilters";
import { loadOfflineLibrary } from "./domain/library";
import { type NewsMountCtx, paintNewsMount } from "./mountNewsPaint";
import { fetchFeedText, refreshNewsArticles } from "./news/refreshNews";
import { htmlForReaderMode } from "./news/renderArticleView";
import { loadBrowsePrefs, loadReaderMode } from "./settings/browsePrefs";
import { loadReaderSettings } from "./settings/readerPrefs";
import { loadLanClientPrefs } from "./sync/lanClientPrefs";
import { markArticleRead, syncLanPool } from "./sync/lanPoolClient";

export function mountNewsModule(newsMount: HTMLElement): void {
  const newsPrefs = loadBrowsePrefs("news");
  const ctx: NewsMountCtx = {
    newsMount,
    articles: loadOfflineLibrary().articles,
    newsUnreadOnly: newsPrefs.unreadOnly,
    newsNode: newsPrefs.nodeId,
    newsSort: newsPrefs.sort,
    openArticle: null,
    readingOrder: [],
    readerHtml: "",
    readerError: "",
    pageHtml: "",
    readerMode: loadReaderMode(),
    pushPoolIfConfigured: () => {
      const { hubUrl, passphrase } = loadLanClientPrefs();
      if (!hubUrl || !passphrase.trim()) return;
      void syncLanPool(hubUrl, passphrase).catch(() => {});
    },
    openOne(article: Article, resetMode = true) {
      const settings = loadReaderSettings();
      if (settings.target !== "LAUNCHER") {
        markArticleRead(article);
        this.pushPoolIfConfigured();
        window.open(article.url, "_blank", "noopener");
        paintNewsMount(this);
        return;
      }
      if (!this.openArticle) {
        this.readingOrder = filterNews(
          this.articles,
          this.newsUnreadOnly,
          this.newsNode,
          this.newsSort,
        );
      }
      markArticleRead(article);
      this.pushPoolIfConfigured();
      this.openArticle = article;
      this.readerHtml = "";
      this.readerError = "";
      this.pageHtml = "";
      if (resetMode) this.readerMode = loadReaderMode();
      paintNewsMount(this);
      void fetchFeedText(article.url)
        .then((html) => {
          if (this.openArticle?.id !== article.id) return;
          this.pageHtml = html;
          this.readerHtml = htmlForReaderMode(article, html, this.readerMode, loadReaderSettings());
          paintNewsMount(this);
        })
        .catch((err: unknown) => {
          if (this.openArticle?.id !== article.id) return;
          this.readerError = String(err);
          paintNewsMount(this);
        });
    },
  };
  paintNewsMount(ctx);
  void refreshNewsArticles().then((next) => {
    if (!newsMount.isConnected) return;
    ctx.articles = next;
    paintNewsMount(ctx);
  });
}
