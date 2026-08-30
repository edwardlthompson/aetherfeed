/** Locked public API for RSS, Atom, JSON Feed, and OPML. */

import type { Article, Feed } from "./models";

export type FeedFormat = "rss" | "atom" | "jsonfeed";

export type ParsedArticle = {
  id: string;
  title: string;
  url: string;
  publishedAt?: number;
  summary?: string;
  contentHtml?: string;
};

export type ParsedFeed = {
  format: FeedFormat;
  title: string;
  siteUrl?: string;
  items: ParsedArticle[];
};

export type OpmlOutline = {
  title: string;
  xmlUrl?: string;
  htmlUrl?: string;
  folder?: string;
  type?: string;
  children?: OpmlOutline[];
};

export type NewsRepository = {
  subscribe(url: string): Promise<Feed>;
  updateFeed(feed: Feed): Promise<void>;
  unsubscribe(feedId: string): Promise<void>;
  importOpml(xml: string): Promise<Feed[]>;
  exportOpml(): Promise<string>;
  refresh(feedId: string): Promise<Article[]>;
  articles(feedId?: string): Promise<Article[]>;
};

export function isFeedFormat(value: string): value is FeedFormat {
  return value === "rss" || value === "atom" || value === "jsonfeed";
}

export function flattenOpml(outlines: OpmlOutline[]): OpmlOutline[] {
  const found: OpmlOutline[] = [];
  const walk = (nodes: OpmlOutline[]) => {
    for (const node of nodes) {
      if (node.xmlUrl) found.push(node);
      if (node.children?.length) walk(node.children);
    }
  };
  walk(outlines);
  return found;
}
