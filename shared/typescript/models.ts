/** Shared AetherFeed content and user-state models. */

export type ModuleKind = "news" | "podcast" | "booru";

export type Feed = {
  id: string;
  title: string;
  url: string;
  kind: ModuleKind;
  siteUrl?: string;
  updatedAt: number;
};

export type Article = {
  id: string;
  feedId: string;
  title: string;
  url: string;
  publishedAt?: number;
  summary?: string;
  localPath?: string;
};

export type PodcastShow = {
  id: string;
  feedId: string;
  title: string;
  author?: string;
};

export type Episode = {
  id: string;
  showId: string;
  title: string;
  enclosureUrl: string;
  durationMs?: number;
  publishedAt?: number;
  localPath?: string;
};

export type BooruPost = {
  id: string;
  sourceId: string;
  remoteId: string;
  fileUrl: string;
  previewUrl?: string;
  tags: string[];
  localPath?: string;
};

export type Tag = {
  id: string;
  name: string;
};

export type Star = {
  targetId: string;
  module: ModuleKind;
  createdAt: number;
};

export type Like = {
  targetId: string;
  module: ModuleKind;
  createdAt: number;
};

export type ReadState = {
  targetId: string;
  module: ModuleKind;
  status: "unread" | "in_progress" | "read";
  updatedAt: number;
};

export type PlaybackPosition = {
  episodeId: string;
  positionMs: number;
  durationMs?: number;
  updatedAt: number;
};

export type NotificationChannelPref = {
  id: string;
  module: ModuleKind;
  enabled: boolean;
  label: string;
};
