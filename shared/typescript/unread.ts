import type { ReadState } from "./models";

/** Count items that are not fully read — keep in sync with Android LibraryRepository.unreadCount. */
export function totalUnread(states: ReadState[]): number {
  return states.filter((state) => state.status !== "read").length;
}
