import type { SortOrder } from "../browse/contentFilter";
import type { ReaderMode } from "./readerPrefs";

export type BrowseModule = "news" | "podcasts" | "booru";

export type BrowseModulePrefs = {
  unreadOnly: boolean;
  nodeId: string;
  sort: SortOrder;
};

const MODULE_KEY = "af-active-module";
const READER_MODE_KEY = "af-reader-mode";
const PREFIX = "af-browse";

const DEFAULT_BROWSE: BrowseModulePrefs = {
  unreadOnly: false,
  nodeId: "",
  sort: "newest",
};

function parseSort(raw: string | null): SortOrder {
  return raw === "oldest" ? "oldest" : "newest";
}

function parseModule(raw: string | null): BrowseModule {
  if (raw === "podcasts" || raw === "booru") return raw;
  return "news";
}

function parseReaderMode(raw: string | null): ReaderMode {
  if (raw === "FULL" || raw === "WEB") return raw;
  return "READING";
}

export function loadActiveModule(storage: Pick<Storage, "getItem"> = localStorage): BrowseModule {
  return parseModule(storage.getItem(MODULE_KEY));
}

export function saveActiveModule(
  module: BrowseModule,
  storage: Pick<Storage, "setItem"> = localStorage,
): void {
  storage.setItem(MODULE_KEY, module);
}

export function loadReaderMode(storage: Pick<Storage, "getItem"> = localStorage): ReaderMode {
  return parseReaderMode(storage.getItem(READER_MODE_KEY));
}

export function saveReaderMode(
  mode: ReaderMode,
  storage: Pick<Storage, "setItem"> = localStorage,
): void {
  storage.setItem(READER_MODE_KEY, mode);
}

export function loadBrowsePrefs(
  module: BrowseModule,
  storage: Pick<Storage, "getItem"> = localStorage,
): BrowseModulePrefs {
  const unread = storage.getItem(`${PREFIX}-${module}-unread`);
  const node = storage.getItem(`${PREFIX}-${module}-node`);
  const sort = storage.getItem(`${PREFIX}-${module}-sort`);
  return {
    unreadOnly: unread === "1",
    nodeId: node ?? DEFAULT_BROWSE.nodeId,
    sort: parseSort(sort),
  };
}

export function saveBrowsePrefs(
  module: BrowseModule,
  prefs: Partial<BrowseModulePrefs>,
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): BrowseModulePrefs {
  const merged: BrowseModulePrefs = {
    ...loadBrowsePrefs(module, storage),
    ...prefs,
  };
  storage.setItem(`${PREFIX}-${module}-unread`, merged.unreadOnly ? "1" : "0");
  storage.setItem(`${PREFIX}-${module}-node`, merged.nodeId);
  storage.setItem(`${PREFIX}-${module}-sort`, merged.sort);
  return merged;
}
