import type { ReadState } from "@aetherfeed/domain";

export function totalUnread(states: ReadState[]): number {
  return states.filter((state) => state.status !== "read").length;
}
