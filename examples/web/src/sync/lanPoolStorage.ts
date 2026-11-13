/** Local persistence for the LAN pool document. */

import type { LanPoolDocument } from "./lanPoolTypes";

const LOCAL_KEY = "af-lan-pool-v2";

export function emptyPoolDocument(): LanPoolDocument {
  return { version: 2, updatedAt: 0, subs: [], reads: [], stars: [], playback: [] };
}

export function loadLocalPool(storage: Pick<Storage, "getItem"> = localStorage): LanPoolDocument {
  const raw = storage.getItem(LOCAL_KEY);
  if (!raw) return emptyPoolDocument();
  try {
    const parsed = JSON.parse(raw) as LanPoolDocument;
    return {
      ...emptyPoolDocument(),
      ...parsed,
      version: 2,
      subs: Array.isArray(parsed.subs) ? parsed.subs : [],
      reads: Array.isArray(parsed.reads) ? parsed.reads : [],
      stars: Array.isArray(parsed.stars) ? parsed.stars : [],
      playback: Array.isArray(parsed.playback) ? parsed.playback : [],
    };
  } catch {
    return emptyPoolDocument();
  }
}

export function saveLocalPool(
  doc: LanPoolDocument,
  storage: Pick<Storage, "setItem"> = localStorage,
): void {
  storage.setItem(LOCAL_KEY, JSON.stringify({ ...doc, version: 2 }));
}
