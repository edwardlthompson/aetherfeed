export function enclosureFromRss(body: string): string {
  if (!body.trim()) return "";
  const doc = new DOMParser().parseFromString(body, "application/xml");
  if (doc.querySelector("parsererror")) return "";
  return doc.querySelector("enclosure")?.getAttribute("url")?.trim() ?? "";
}

const remembered = new Map<string, string>();

export function rememberEnclosure(showId: string, url: string): void {
  const id = showId.trim();
  const href = url.trim();
  if (id && href) remembered.set(id, href);
}

export function rememberedEnclosure(showId: string): string | undefined {
  return remembered.get(showId.trim());
}
