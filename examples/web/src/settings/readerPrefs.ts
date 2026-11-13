export type ArticleTarget = "LAUNCHER" | "BROWSER" | "CUSTOM_TAB";
export type ReaderMode = "READING" | "FULL" | "WEB";
export type ImagePolicy = "ALWAYS" | "WIFI" | "NEVER";

export type ReaderSettings = {
  target: ArticleTarget;
  bodyScale: number;
  imagePolicy: ImagePolicy;
};

const TARGET_KEY = "af-article-target";
const SCALE_KEY = "af-body-scale";
const POLICY_KEY = "af-image-policy";

export const DEFAULT_READER_MODE: ReaderMode = "READING";

export function parseArticleTarget(raw: string | null): ArticleTarget {
  const value = raw?.trim().toUpperCase();
  if (value === "BROWSER" || value === "CUSTOM_TAB") return value;
  return "LAUNCHER";
}

export function parseImagePolicy(raw: string | null): ImagePolicy {
  const value = raw?.trim().toUpperCase();
  if (value === "WIFI" || value === "NEVER") return value;
  return "ALWAYS";
}

export function clampScale(raw: number): number {
  return Math.min(1.6, Math.max(0.85, raw));
}

export function loadReaderSettings(): ReaderSettings {
  const scale = Number.parseFloat(localStorage.getItem(SCALE_KEY) || "1");
  return {
    target: parseArticleTarget(localStorage.getItem(TARGET_KEY)),
    bodyScale: Number.isFinite(scale) ? clampScale(scale) : 1,
    imagePolicy: parseImagePolicy(localStorage.getItem(POLICY_KEY)),
  };
}

export function saveReaderSettings(next: Partial<ReaderSettings>): ReaderSettings {
  const current = loadReaderSettings();
  const merged: ReaderSettings = {
    target: next.target ?? current.target,
    bodyScale: clampScale(next.bodyScale ?? current.bodyScale),
    imagePolicy: next.imagePolicy ?? current.imagePolicy,
  };
  localStorage.setItem(TARGET_KEY, merged.target);
  localStorage.setItem(SCALE_KEY, String(merged.bodyScale));
  localStorage.setItem(POLICY_KEY, merged.imagePolicy);
  return merged;
}

export function allowImages(policy: ImagePolicy): boolean {
  return policy !== "NEVER";
}
