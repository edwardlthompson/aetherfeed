import type { Article } from "@aetherfeed/domain";
import { paintNavUnread, paintUnifiedUnread } from "../shell/navUnread";
import type { NewsChromePrefs } from "./chromePrefs";
import { bindNewsLayout } from "./newsLayout";
import { bindNewsChrome, type NewsAbortPair, queueSelectedReader } from "./newsPaneBind";

export function afterNewsRender(
  root: HTMLElement,
  shown: Article[],
  selected: Article | undefined,
  selectedId: string | null,
  chrome: NewsChromePrefs,
  unreadCount: number,
  onChrome: (next: NewsChromePrefs) => void,
  onOpen: (article: Article) => void,
  onRefresh: () => void,
  onSort: (oldestFirst: boolean) => void,
  onThumbs: (thumbs: Record<string, string>, cached?: string[]) => void,
  pair: NewsAbortPair,
  unread: (id: string) => boolean,
): void {
  bindNewsLayout(root, chrome, onChrome);
  bindNewsChrome(root, shown, selectedId, onOpen, onRefresh, onSort);
  paintNavUnread(document.body, { news: unreadCount, podcast: 0, booru: 0 });
  paintUnifiedUnread(document.body, unreadCount, 0, 0);
  queueSelectedReader(root, selected, shown, unread, onThumbs, pair);
}
