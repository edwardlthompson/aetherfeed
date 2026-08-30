/** Keyword include/exclude + auto-tag rule contract (Inoreader analog). */

export type Rule = {
  include: string[];
  exclude: string[];
  tag?: string;
};

export type RuleArticle = {
  title?: string;
  summary?: string;
  content?: string;
  contentHtml?: string;
};

export type RuleResult = {
  keep: boolean;
  tags: string[];
};
