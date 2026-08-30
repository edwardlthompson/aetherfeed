import { t } from "../i18n";
import type { NewsFetchError } from "./fetchError";

export function refreshNotice(error: NewsFetchError | null | undefined, title: string): string {
  if (!error) return "";
  if (error === "gone" || error === "missing") {
    return t("news.feed_gone").replace("{title}", title);
  }
  if (error === "timeout") return t("news.feed_timeout").replace("{title}", title);
  if (error === "parse") return t("news.feed_parse").replace("{title}", title);
  return t("news.feed_unreachable").replace("{title}", title);
}

export function emptyFeedNotice(title: string): string {
  return t("news.feed_empty").replace("{title}", title);
}
