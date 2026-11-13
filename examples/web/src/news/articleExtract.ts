export type ArticleBlock = { kind: "text"; value: string } | { kind: "image"; url: string };

const SCRIPT = /<script\b[^>]*>[\s\S]*?<\/script>/gi;
const STYLE = /<style\b[^>]*>[\s\S]*?<\/style>/gi;
const NOSCRIPT = /<noscript\b[^>]*>[\s\S]*?<\/noscript>/gi;
const TAGS = /<[^>]+>/g;
const ARTICLE = /<article\b[^>]*>([\s\S]*?)<\/article>/i;
const IMG = /<img\b[^>]*>/gi;
const TINY = /favicon|sprite|pixel|1x1|tracking|emoji|badge|icon[-_/]|\/icons?\/|social|share/i;

export function htmlToText(html: string): string {
  let out = html.replace(SCRIPT, " ").replace(STYLE, " ").replace(NOSCRIPT, " ");
  out = out.replace(/<br\s*\/?>/gi, "\n").replace(/<\/(p|div|h[1-6]|li)>/gi, "\n\n");
  out = out.replace(TAGS, " ");
  out = out
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#39;|&apos;/g, "'");
  out = out.replace(/[ \t\x0B\f\r]+/g, " ").replace(/ *\n */g, "\n");
  return out.replace(/\n{3,}/g, "\n\n").trim();
}

export function absUrl(raw: string): string | null {
  const trimmed = raw.trim();
  const url = trimmed.startsWith("//") ? `https:${trimmed}` : trimmed;
  if (!/^https?:\/\//i.test(url) || TINY.test(url) || url.startsWith("data:")) return null;
  return url;
}

export function bestFromImageElement(markup: string): string | null {
  const src = /(?:src|data-src)\s*=\s*["']([^"']+)["']/i.exec(markup);
  return src ? absUrl(src[1]) : null;
}

export function extractBlocks(html: string): ArticleBlock[] {
  const nested = ARTICLE.exec(html)?.[1];
  const source = nested && nested.length >= 80 ? nested : html;
  const out: ArticleBlock[] = [];
  const seen = new Set<string>();
  let cursor = 0;
  for (const match of source.matchAll(IMG)) {
    const start = match.index ?? 0;
    const text = htmlToText(source.slice(cursor, start));
    if (text) out.push({ kind: "text", value: text });
    const url = bestFromImageElement(match[0]);
    if (url && !seen.has(url)) {
      seen.add(url);
      out.push({ kind: "image", url });
    }
    cursor = start + match[0].length;
  }
  const tail = htmlToText(source.slice(cursor));
  if (tail) out.push({ kind: "text", value: tail });
  if (out.length === 0) {
    const fallback = htmlToText(html);
    if (fallback) out.push({ kind: "text", value: fallback });
  }
  return out;
}

/** Safe inner markup for the in-page reader (no nested iframe document). */
export function blocksToReadingHtml(
  blocks: ArticleBlock[],
  opts: { showImages: boolean; bodyScale?: number },
): string {
  void opts.bodyScale;
  return blocks
    .map((block) => {
      if (block.kind === "text") {
        return `<p>${escapeHtml(block.value).replace(/\n\n/g, "</p><p>")}</p>`;
      }
      if (!opts.showImages) return "";
      return `<p><img src="${escapeHtml(block.url)}" alt=""></p>`;
    })
    .join("");
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}
