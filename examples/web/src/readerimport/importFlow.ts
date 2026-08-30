import {
  createReaderImportRepository,
  type Feed,
  parseReaderImport,
  type ReaderImportFile,
  type ReaderImportResult,
  type ReadState,
  type Star,
} from "@aetherfeed/domain";
import { t } from "../i18n";
import { loadImportedFeeds, saveImportedFeeds } from "./importedStore";

export function formatImportResult(result: ReaderImportResult): string {
  return t("readerimport.result")
    .replace("{added}", String(result.feedsAdded))
    .replace("{news}", String(result.newsAdded))
    .replace("{podcasts}", String(result.podcastsAdded))
    .replace("{skipped}", String(result.feedsSkipped))
    .replace("{errors}", String(result.errors.length));
}

export async function applyReaderImportFile(file: ReaderImportFile): Promise<ReaderImportResult> {
  const buffer = loadImportedFeeds();
  const stars: Star[] = [];
  const reads: ReadState[] = [];
  const result = await createReaderImportRepository({
    feeds: async () => buffer,
    upsertFeed: async (feed: Feed) => {
      buffer.push(feed);
    },
    upsertStar: async (star: Star) => {
      stars.push(star);
    },
    upsertReadState: async (state: ReadState) => {
      reads.push(state);
    },
  }).apply(parseReaderImport(file));
  saveImportedFeeds(buffer);
  return result;
}
