import type { Article, Feed } from "@aetherfeed/domain";
import { t } from "../i18n";
import { applyRules, loadRules } from "../rules";
import { buildIndex } from "../search";
import { saveArticleIndex } from "./articleIndex";
import { emptyFeedNotice, refreshNotice } from "./feedStatus";
import { MemoryNewsRepository } from "./memoryRepo";
import { canFetchNews } from "./newsNetwork";
import { thumbsFor } from "./prefetch";

export function visibleArticles(articles: Article[]): Article[] {
  return articles.filter(
    (article) =>
      applyRules(
        { title: article.title, summary: article.summary, contentHtml: article.contentHtml },
        loadRules(),
      ).keep,
  );
}

export async function pullArticles(
  repo: MemoryNewsRepository,
  feedId: string,
  force: boolean,
): Promise<{ articles: Article[]; status: string }> {
  let articles = await repo.articles(feedId);
  let status = "";
  if (force || articles.length === 0) {
    if (!canFetchNews()) status = t("news.wifi_blocked");
    else {
      try {
        articles = await repo.refresh(feedId);
        status = refreshNotice(repo.lastError, repo.feedTitle(feedId));
        if (!status && articles.length === 0) status = emptyFeedNotice(repo.feedTitle(feedId));
        await saveArticleIndex(await repo.articles());
      } catch {
        status = "network";
      }
    }
  }
  buildIndex(
    articles.map((article) => ({
      id: article.id,
      title: article.title,
      body: `${article.summary ?? ""} ${article.contentHtml ?? ""}`,
    })),
  );
  return { articles, status };
}

export function unifiedExtras(feeds: Feed[]): Article[] {
  return feeds
    .filter((feed) => feed.kind === "podcast" || feed.kind === "booru")
    .map((feed) => ({
      id: `${feed.kind}:${feed.id}`,
      feedId: feed.id,
      title: feed.title,
      url: feed.url,
      publishedAt: feed.updatedAt,
    }));
}

export async function headlinesFromAll(
  repo: MemoryNewsRepository,
  feeds: { id: string }[],
  force: boolean,
): Promise<Article[]> {
  const out: Article[] = [];
  for (const feed of feeds) {
    out.push(...(await pullArticles(repo, feed.id, force)).articles);
  }
  return out;
}

export async function loadTimeline(
  repo: MemoryNewsRepository,
  feedIds: string[],
  force: boolean,
  extras: Article[] = [],
): Promise<{ articles: Article[]; status: string; thumbs: Record<string, string> }> {
  if (!feedIds.length && !extras.length) return { articles: [], status: "", thumbs: {} };
  const pulled =
    feedIds.length === 1
      ? await pullArticles(repo, feedIds[0], force)
      : {
          articles: await headlinesFromAll(
            repo,
            feedIds.map((id) => ({ id })),
            force,
          ),
          status: "",
        };
  const articles = [...pulled.articles, ...extras];
  return {
    articles,
    status: pulled.status,
    thumbs: await thumbsFor(articles.map((article) => article.id)),
  };
}
