import type { ReadState } from "@aetherfeed/domain";

/**
 * Widget/tray unread contract — keep identical to shared/typescript/unread.ts
 * and Android LibraryRepository.unreadCount.
 */
export function totalUnread(states: ReadState[]): number {
  return states.filter((state) => state.status !== "read").length;
}
