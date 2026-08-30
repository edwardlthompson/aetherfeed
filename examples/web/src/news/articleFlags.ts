import type { ReadState, Star } from "@aetherfeed/domain";
import { saveStarredIds } from "./cacheRetain";

export type ArticleFlags = {
  isUnread(id: string): boolean;
  setRead(id: string, read: boolean): void;
  toggleUnread(id: string): void;
  isStarred(id: string): boolean;
  toggleStar(id: string): void;
};

export function createArticleFlags(now: () => number = Date.now): ArticleFlags {
  const reads = new Map<string, ReadState>();
  const stars = new Map<string, Star>();

  return {
    isUnread(id: string): boolean {
      return reads.get(id)?.status !== "read";
    },
    setRead(id: string, read: boolean): void {
      reads.set(id, {
        targetId: id,
        module: "news",
        status: read ? "read" : "unread",
        updatedAt: now(),
      });
    },
    toggleUnread(id: string): void {
      const unread = reads.get(id)?.status !== "read";
      this.setRead(id, unread);
    },
    isStarred(id: string): boolean {
      return stars.has(id);
    },
    toggleStar(id: string): void {
      if (stars.has(id)) stars.delete(id);
      else stars.set(id, { targetId: id, module: "news", createdAt: now() });
      saveStarredIds(stars.keys());
    },
  };
}
