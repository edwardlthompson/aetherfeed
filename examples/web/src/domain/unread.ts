import type { ModuleKind, ReadState } from "@aetherfeed/domain";

export function totalUnread(states: ReadState[]): number {
  return states.filter((state) => state.status !== "read").length;
}

export function unreadByModule(states: ReadState[]): Record<ModuleKind, number> {
  const counts: Record<ModuleKind, number> = { news: 0, podcast: 0, booru: 0 };
  for (const state of states) {
    if (state.status !== "read") counts[state.module] += 1;
  }
  return counts;
}
