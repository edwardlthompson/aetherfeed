export const NEWS_ARTICLE_SWIPE = {
  edgeReservePx: 48,
  minDistancePx: 112,
  snapFraction: 0.72,
  minVelocityPx: 2400,
} as const;

export function articleSwipeDelta(
  startX: number,
  dx: number,
  velocityX: number,
  pageWidth: number,
  edgeReservePx = NEWS_ARTICLE_SWIPE.edgeReservePx,
  slopPx = NEWS_ARTICLE_SWIPE.minDistancePx,
  snapFraction = NEWS_ARTICLE_SWIPE.snapFraction,
  minVelocityPx = NEWS_ARTICLE_SWIPE.minVelocityPx,
): number {
  if (startX < edgeReservePx) return 0;
  if (Math.abs(dx) < slopPx) return 0;
  const next = dx < 0 ? 1 : -1;
  if (Math.abs(velocityX) >= minVelocityPx) return next;
  const width = Math.max(pageWidth, 1);
  if (Math.abs(dx) >= width * snapFraction) return next;
  return 0;
}

export function newsDrawerOffstage(open: boolean): boolean {
  return !open;
}
