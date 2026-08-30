/** In-scope chrome copy (locales stay untouched). */

export const rulesCopy = {
  title: "Keyword rules",
  hint: "Include keeps matching articles. Exclude always wins. Optional tag is applied when a rule matches.",
  includeLabel: "Include keywords",
  excludeLabel: "Exclude keywords",
  tagLabel: "Auto-tag",
  includePlaceholder: "rust, typescript",
  excludePlaceholder: "gossip, rumor",
  tagPlaceholder: "dev",
  add: "Add rule",
  remove: "Remove",
  empty: "No rules yet. Add include or exclude keywords to filter the feed.",
  includePrefix: "Include",
  excludePrefix: "Exclude",
  tagPrefix: "Tag",
} as const;
