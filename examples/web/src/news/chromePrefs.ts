export const NEWS_CHROME_KEY = "af-news-chrome";

export type NewsChromePrefs = {
  expanded: string[];
  sidebarHidden: boolean;
  oldestFirst: boolean;
  source: number;
  timeline: number;
  reader: number;
  folder: string;
  feedId: string;
};

export const NEWS_CHROME_DEFAULT: NewsChromePrefs = {
  expanded: [],
  sidebarHidden: false,
  oldestFirst: false,
  source: 0.28,
  timeline: 0.34,
  reader: 0.38,
  folder: "",
  feedId: "",
};

export function clampPanes(
  source: number,
  timeline: number,
  reader: number,
): Pick<NewsChromePrefs, "source" | "timeline" | "reader"> {
  const min = 0.14;
  let src = Math.max(min, source);
  let mid = Math.max(min, timeline);
  let end = Math.max(min, reader);
  const sum = src + mid + end;
  if (sum <= 0) {
    return {
      source: NEWS_CHROME_DEFAULT.source,
      timeline: NEWS_CHROME_DEFAULT.timeline,
      reader: NEWS_CHROME_DEFAULT.reader,
    };
  }
  src /= sum;
  mid /= sum;
  end /= sum;
  return { source: src, timeline: mid, reader: end };
}

export function parseNewsChrome(raw: unknown): NewsChromePrefs {
  if (!raw || typeof raw !== "object") return { ...NEWS_CHROME_DEFAULT };
  const rec = raw as Record<string, unknown>;
  const expanded = Array.isArray(rec.expanded)
    ? rec.expanded.filter((row): row is string => typeof row === "string" && row.trim().length > 0)
    : [];
  const panes = clampPanes(
    typeof rec.source === "number" ? rec.source : NEWS_CHROME_DEFAULT.source,
    typeof rec.timeline === "number" ? rec.timeline : NEWS_CHROME_DEFAULT.timeline,
    typeof rec.reader === "number" ? rec.reader : NEWS_CHROME_DEFAULT.reader,
  );
  return {
    ...panes,
    expanded,
    sidebarHidden: rec.sidebarHidden === true,
    oldestFirst: rec.oldestFirst === true,
    folder: typeof rec.folder === "string" ? rec.folder.trim() : "",
    feedId: typeof rec.feedId === "string" ? rec.feedId.trim() : "",
  };
}

export function withLocation(
  prefs: NewsChromePrefs,
  folder: string | null,
  feedId: string | null,
): NewsChromePrefs {
  const name = folder?.trim() ?? "";
  return {
    ...prefs,
    folder: name,
    feedId: feedId?.trim() ?? "",
  };
}

export function restoreNewsLocation(
  groups: [string, { id: string }[]][],
  prefs: NewsChromePrefs,
): { folder: string | null; feedId: string | null } {
  const feeds = groups.flatMap(([, rows]) => rows);
  const byId = prefs.feedId ? feeds.find((row) => row.id === prefs.feedId) : undefined;
  if (byId) {
    const folder = groups.find(([, rows]) => rows.some((row) => row.id === byId.id))?.[0] ?? null;
    return { folder, feedId: byId.id };
  }
  if (prefs.folder) {
    const group = groups.find(([name]) => name === prefs.folder);
    if (group) return { folder: group[0], feedId: group[1][0]?.id ?? null };
  }
  const first = groups[0];
  return { folder: first?.[0] ?? null, feedId: first?.[1][0]?.id ?? null };
}

export function loadNewsChrome(): NewsChromePrefs {
  try {
    return parseNewsChrome(JSON.parse(localStorage.getItem(NEWS_CHROME_KEY) ?? "null"));
  } catch {
    return { ...NEWS_CHROME_DEFAULT };
  }
}

export function saveNewsChrome(prefs: NewsChromePrefs): void {
  localStorage.setItem(NEWS_CHROME_KEY, JSON.stringify(prefs));
}

export function toggleExpanded(prefs: NewsChromePrefs, name: string): NewsChromePrefs {
  const key = name.trim();
  if (!key) return prefs;
  const has = prefs.expanded.includes(key);
  return {
    ...prefs,
    expanded: has ? prefs.expanded.filter((row) => row !== key) : [...prefs.expanded, key],
  };
}
