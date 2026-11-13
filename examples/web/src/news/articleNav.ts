import type { Article } from "@aetherfeed/domain";

export function stepArticleIndex(index: number, size: number, delta: number): number | null {
  if (size <= 0) return null;
  const next = index + delta;
  return next >= 0 && next < size ? next : null;
}

export function neighborArticle(
  articles: Article[],
  currentId: string | undefined,
  delta: number,
): Article | null {
  const index = articles.findIndex((item) => item.id === currentId);
  const next = stepArticleIndex(index, articles.length, delta);
  return next == null ? null : articles[next];
}
