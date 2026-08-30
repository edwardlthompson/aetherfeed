const SKIP = [
  "facebook.com",
  "fbcdn",
  "twitter.com",
  "twimg.com",
  "x.com/intent",
  "linkedin.com",
  "instagram.com",
  "pinterest.",
  "reddit.com",
  "sharethis",
  "addthis",
  "addtoany",
  "/share/",
  "pixel",
  "1x1",
  "tracking",
  "beacon",
  "analytics",
  "doubleclick",
  "googletag",
  "gravatar",
  "favicon",
  "apple-touch",
  "whatsapp",
  "telegram",
  "mastodon",
  "/icon.",
  "/icons/",
  "sprite",
  "badge",
  "social-icon",
  "share-icon",
  "sharer",
];

export function isContentImage(src: string, width?: number, height?: number, hint = ""): boolean {
  const url = src.trim();
  if (!url) return false;
  if (width !== undefined && Number.isFinite(width) && width > 0 && width <= 48) return false;
  if (height !== undefined && Number.isFinite(height) && height > 0 && height <= 48) return false;
  const hay = `${url} ${hint}`.toLowerCase();
  return !SKIP.some((needle) => hay.includes(needle));
}

export function dropNonContentImages(root: Element): void {
  for (const img of Array.from(root.querySelectorAll("img"))) {
    const src = img.getAttribute("src")?.trim() ?? "";
    const width = Number(img.getAttribute("width"));
    const height = Number(img.getAttribute("height"));
    const hint = `${img.getAttribute("alt") ?? ""} ${img.getAttribute("class") ?? ""}`;
    if (!isContentImage(src, width, height, hint)) img.remove();
  }
}

export function firstThumb(images: Record<string, string>, order: string[] = []): string {
  for (const src of order) {
    const blob = images[src];
    if (blob?.startsWith("data:")) return blob;
  }
  return Object.values(images).find((blob) => blob.startsWith("data:")) ?? "";
}
