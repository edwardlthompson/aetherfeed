export const BODY_FLOOR = 400;

const PAYWALL = ["subscriber-only", "subscribe to continue", "paywall"];

export function needsFetch(html: string | undefined): boolean {
  const body = html ?? "";
  if (body.length >= BODY_FLOOR) return false;
  return body.trim().length < BODY_FLOOR;
}

export function looksPaywalled(html: string | undefined): boolean {
  const text = html ?? "";
  if (!text.trim()) return true;
  if (text.length >= BODY_FLOOR * 2) return false;
  const lower = text.toLowerCase();
  return PAYWALL.some((marker) => lower.includes(marker));
}

export function isStubBody(html: string | undefined): boolean {
  const raw = html ?? "";
  if (!raw || raw.length >= BODY_FLOOR * 8) return false;
  const text = raw
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
  if (!text) return false;
  const lower = text.toLowerCase();
  const hnMeta = ["comments url", "article url", "# comments"].some((mark) => lower.includes(mark));
  return (hnMeta || /https?:\/\//.test(text)) && text.length < BODY_FLOOR;
}

export function isFeedExcerpt(html: string | undefined): boolean {
  const raw = html ?? "";
  if (!raw.trim()) return false;
  return (
    raw.includes("webfeedsFeaturedVisual") ||
    raw.includes("webfeedsfeaturedvisual") ||
    raw.includes("link_thumbnail")
  );
}

export function usableBody(html: string | undefined): string {
  if (
    !html ||
    needsFetch(html) ||
    looksPaywalled(html) ||
    isStubBody(html) ||
    isFeedExcerpt(html)
  ) {
    return "";
  }
  return html;
}
