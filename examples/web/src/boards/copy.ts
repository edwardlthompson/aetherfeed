/** In-scope designed-empty and chrome copy (locales stay untouched). */

export const boardCopy = {
  title: "Boards",
  searchLabel: "Tags",
  searchPlaceholder: "tag1 tag2",
  searchSubmit: "Search",
  emptySources: "No board sources yet. Add a public media-board URL to browse tags.",
  addSource: "Add source",
  sourceUrl: "Board URL",
  emptySearch: "No posts match these tags. Try a different search.",
  favoriteAdd: "Save favorite",
  favoriteRemove: "Remove favorite",
  retry: "Retry",
  errorTimeout: "Search timed out.",
  errorAborted: "Search was cancelled.",
  errorNetwork: "Could not reach the board.",
  errorInvalid: "The board returned an unexpected response.",
} as const;

export function messageForError(code: string): string {
  if (code === "timeout") return boardCopy.errorTimeout;
  if (code === "aborted") return boardCopy.errorAborted;
  if (code === "invalid") return boardCopy.errorInvalid;
  return boardCopy.errorNetwork;
}
