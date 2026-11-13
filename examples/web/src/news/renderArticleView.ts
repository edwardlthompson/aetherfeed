import type { Article } from "@aetherfeed/domain";
import { allowImages, type ReaderMode, type ReaderSettings } from "../settings/readerPrefs";
import { blocksToReadingHtml, extractBlocks } from "./articleExtract";
import { prepareArticleHtml } from "./prepareArticleHtml";

export function htmlForReaderMode(
  article: Article,
  pageHtml: string,
  mode: ReaderMode,
  settings: ReaderSettings,
): string {
  const showImages = allowImages(settings.imagePolicy);
  if (mode === "WEB") {
    return prepareArticleHtml(pageHtml, article.url);
  }
  const source =
    mode === "READING" && (article.summary?.length ?? 0) >= 40 && pageHtml.length < 80
      ? article.summary || pageHtml
      : pageHtml || article.summary || "";
  return blocksToReadingHtml(extractBlocks(source), {
    showImages,
    bodyScale: settings.bodyScale,
  });
}

export function isWebReaderHtml(html: string): boolean {
  return /<!DOCTYPE html|<html[\s>]/i.test(html);
}
