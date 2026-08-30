import "./reader.css";
import { readerCopy } from "./copy";
import { extractReadable } from "./extract";
import { fetchImageBlobs } from "./images";
import type { ReaderViewHandle, ReaderViewOptions } from "./types";
import { loadArticle, saveArticle } from "./vault";

function escapeText(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function articleKey(html: string): string {
  let hash = 2166136261;
  for (let i = 0; i < html.length; i += 1) {
    hash ^= html.charCodeAt(i);
    hash = Math.imul(hash, 16777619);
  }
  return `article:${(hash >>> 0).toString(16)}`;
}

function applyImageBlobs(root: HTMLElement, images: Record<string, string>): void {
  for (const img of Array.from(root.querySelectorAll("img"))) {
    const src = img.getAttribute("src")?.trim() ?? "";
    const blob = images[src];
    if (blob) img.setAttribute("src", blob);
  }
}

function paintArticleProgress(root: HTMLElement, done: number, total: number): void {
  const bar = root.querySelector<HTMLElement>("[data-news-article-progress]");
  if (!bar) return;
  if (total <= 0 || done >= total) {
    bar.hidden = true;
    return;
  }
  bar.hidden = false;
  bar.setAttribute("aria-valuenow", String(done));
  bar.setAttribute("aria-valuemax", String(total));
  const fill = bar.querySelector<HTMLElement>(".af-progress-fill");
  if (fill) fill.style.width = `${Math.round((done / total) * 100)}%`;
}

async function persistOffline(
  articleId: string,
  bodyHtml: string,
  imageSrcs: string[],
  root: HTMLElement,
  options: ReaderViewOptions,
  signal: AbortSignal,
): Promise<void> {
  const have = loadArticle(articleId)?.images ?? {};
  saveArticle(articleId, bodyHtml, have);
  const missing = imageSrcs.filter((src) => !have[src]);
  const fresh =
    missing.length === 0 || options.allowNetwork === false
      ? {}
      : await fetchImageBlobs(missing, {
          fetchImpl: options.fetchImpl,
          timeoutMs: options.timeoutMs,
          signal,
          onProgress: (done, total) => {
            paintArticleProgress(root, done, total);
            options.onProgress?.(done, total);
          },
        });
  const images = { ...have, ...fresh };
  applyImageBlobs(root, images);
  paintArticleProgress(root, 1, 1);
  saveArticle(articleId, bodyHtml, images);
}

export function createReaderView(
  root: HTMLElement,
  html: string,
  options: ReaderViewOptions = {},
): ReaderViewHandle {
  const readable = extractReadable(html);
  const articleId = options.articleId?.trim() || articleKey(html ?? "");
  const ctrl = new AbortController();
  const onAbort = (): void => ctrl.abort();
  if (options.signal?.aborted) ctrl.abort();
  else options.signal?.addEventListener("abort", onAbort, { once: true });

  root.className = "af-reader";
  root.dataset.testid = "reader-view";
  root.dataset.articleId = articleId;

  if (!readable.bodyHtml) {
    root.innerHTML = `<article class="af-reader-article" data-reader-empty><p>${escapeText(readerCopy.empty)}</p></article>`;
    return { abort: () => ctrl.abort(), articleId, ready: Promise.resolve() };
  }

  const hasH1 = /<h1[\s>]/i.test(readable.bodyHtml);
  const heading = readable.title && !hasH1 ? `<h1>${escapeText(readable.title)}</h1>` : "";
  root.innerHTML = `<div class="af-progress" data-news-article-progress role="progressbar" aria-valuemin="0" aria-valuemax="1" aria-valuenow="0" hidden><span class="af-progress-fill" style="width:0%"></span></div><article class="af-reader-article" data-reader-article>${heading}${readable.bodyHtml}</article>`;

  const ready = persistOffline(
    articleId,
    readable.bodyHtml,
    readable.imageSrcs,
    root,
    options,
    ctrl.signal,
  ).catch((err) => {
    if (ctrl.signal.aborted) return;
    throw err;
  });

  return {
    abort: () => {
      ctrl.abort();
      options.signal?.removeEventListener("abort", onAbort);
    },
    articleId,
    ready,
  };
}
