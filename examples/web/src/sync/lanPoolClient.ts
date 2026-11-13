/** Multi-device LAN pool client (desktop Tauri seal/open + HTTP hub). */

export {
  isArticleRead,
  isArticleStarred,
  markArticleRead,
  markArticleUnread,
  setArticleRead,
  setArticleStarred,
} from "./lanPoolArticles";
export { mergePoolDocuments } from "./lanPoolMerge";
export { emptyPoolDocument, loadLocalPool, saveLocalPool } from "./lanPoolStorage";
export { syncLanPool } from "./lanPoolSync";
export type { LanPoolDocument, LanPoolSub } from "./lanPoolTypes";
