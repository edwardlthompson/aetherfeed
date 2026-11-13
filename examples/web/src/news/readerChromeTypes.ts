import type { Article } from "@aetherfeed/domain";
import type { BrowseNavOptions } from "../browse/browseChrome";

export type ReaderMode = "READING" | "FULL" | "WEB";

export type NewsChromeOptions = {
  unreadCount: number;
  articles?: Article[];
  status?: "loading" | "ready";
  openArticle?: Article | null;
  readerHtml?: string;
  readerError?: string;
  readerMode?: ReaderMode;
  bodyScale?: number;
  onReaderMode?: (mode: ReaderMode) => void;
  onArticleOpen?: (article: Article) => void;
  onReaderBack?: () => void;
  onPrev?: () => void;
  onNext?: () => void;
  canPrev?: boolean;
  canNext?: boolean;
  browse?: BrowseNavOptions;
  sourceCount?: number;
  starred?: boolean;
  read?: boolean;
  onToggleStar?: () => void;
  onParkUnread?: () => void;
  onOpenBrowser?: () => void;
};
