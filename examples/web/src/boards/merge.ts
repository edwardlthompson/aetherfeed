import type { BoardPost } from "./types";

export function mergeBoardPosts(posts: BoardPost[]): BoardPost[] {
  const buckets = new Map<string, BoardPost>();
  for (const post of posts) {
    const key = post.remoteId.trim() || post.id;
    if (!buckets.has(key)) buckets.set(key, post);
  }
  return [...buckets.values()];
}
