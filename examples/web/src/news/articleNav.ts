import type { Article } from "@aetherfeed/domain";
import { articleSwipeDelta } from "./newsSwipe";

export function sortArticles(articles: Article[], oldestFirst: boolean): Article[] {
  const ordered = [...articles].sort((left, right) => {
    const a = left.publishedAt ?? Number.MAX_SAFE_INTEGER;
    const b = right.publishedAt ?? Number.MAX_SAFE_INTEGER;
    return a === b ? left.id.localeCompare(right.id) : a - b;
  });
  return oldestFirst ? ordered : ordered.reverse();
}

export function neighborArticle(
  articles: Article[],
  currentId: string | null,
  delta: number,
): Article | undefined {
  if (!currentId || !articles.length || delta === 0) return undefined;
  const index = articles.findIndex((article) => article.id === currentId);
  if (index < 0) return undefined;
  return articles[index + delta];
}

export function aroundArticles(articles: Article[], currentId: string | null): Article[] {
  const current = articles.find((row) => row.id === currentId);
  if (!current) return [];
  return [
    neighborArticle(articles, currentId, -1),
    current,
    neighborArticle(articles, currentId, 1),
  ].filter((row): row is Article => Boolean(row));
}

export function prefetchAfter(
  articles: Article[],
  startId: string | null,
  unread: (id: string) => boolean,
): Article[] {
  const start = startId ? articles.findIndex((article) => article.id === startId) : -1;
  if (start < 0) return articles.filter((article) => unread(article.id));
  return [articles[start], ...articles.slice(start + 1).filter((article) => unread(article.id))];
}

export function readerPlaceholder(): string {
  return "";
}

export function bindReaderSwipe(
  host: HTMLElement | null,
  onDelta: (delta: number) => void,
  signal?: AbortSignal,
): void {
  if (!host) return;
  let origin = 0;
  let originAt = 0;
  host.addEventListener(
    "pointerdown",
    (event) => {
      origin = event.clientX;
      originAt = event.timeStamp;
    },
    { signal },
  );
  host.addEventListener(
    "pointerup",
    (event) => {
      const dx = event.clientX - origin;
      const dt = Math.max((event.timeStamp - originAt) / 1000, 0.001);
      const width = host.getBoundingClientRect().width || window.innerWidth || 1;
      const delta = articleSwipeDelta(origin, dx, dx / dt, width);
      if (delta !== 0) onDelta(delta);
    },
    { signal },
  );
}
