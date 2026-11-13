/** Multi-device LAN pool client (desktop Tauri seal/open + HTTP hub). */

import { detectTauriInvoke, type TauriInvoke } from "./tauriInvoke";

export type LanPoolSub = {
  url: string;
  title: string;
  kind: string;
  tag?: string;
  updatedAt: number;
};

export type LanPoolDocument = {
  version: number;
  updatedAt: number;
  subs: LanPoolSub[];
  reads: Array<{ url: string; read: boolean; updatedAt: number }>;
  stars: Array<{ url: string; starred: boolean; updatedAt: number }>;
  playback: Array<{ url: string; positionMs: number; updatedAt: number }>;
};

const LOCAL_KEY = "af-lan-pool-v2";

function invokeFn(): TauriInvoke | null {
  return detectTauriInvoke();
}

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

function articleKeys(article: { id: string; url: string }): string[] {
  return [article.url, article.id]
    .map((value) => normalizeUrl(value))
    .filter((value, index, all) => value.length > 0 && all.indexOf(value) === index);
}

/** Persist a read receipt for an article URL (and id when different). */
export function markArticleRead(
  article: { id: string; url: string },
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  return setArticleRead(article, true, now, storage);
}

/** Park as unread again (Feedly/Inoreader-style). */
export function markArticleUnread(
  article: { id: string; url: string },
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  return setArticleRead(article, false, now, storage);
}

export function setArticleRead(
  article: { id: string; url: string },
  read: boolean,
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  const pool = loadLocalPool(storage);
  const byUrl = new Map(pool.reads.map((row) => [normalizeUrl(row.url), row]));
  for (const key of articleKeys(article)) {
    byUrl.set(key, { url: key, read, updatedAt: now });
  }
  const next = {
    ...pool,
    version: 2 as const,
    updatedAt: Math.max(pool.updatedAt || 0, now),
    reads: [...byUrl.values()],
  };
  saveLocalPool(next, storage);
  return next;
}

export function isArticleRead(
  article: { id: string; url: string },
  storage: Pick<Storage, "getItem"> = localStorage,
): boolean {
  const reads = new Map(
    loadLocalPool(storage).reads.map((row) => [normalizeUrl(row.url), row.read]),
  );
  return articleKeys(article).some((key) => reads.get(key) === true);
}

export function setArticleStarred(
  article: { id: string; url: string },
  starred: boolean,
  now = Date.now(),
  storage: Pick<Storage, "getItem" | "setItem"> = localStorage,
): LanPoolDocument {
  const pool = loadLocalPool(storage);
  const byUrl = new Map(pool.stars.map((row) => [normalizeUrl(row.url), row]));
  for (const key of articleKeys(article)) {
    byUrl.set(key, { url: key, starred, updatedAt: now });
  }
  const next = {
    ...pool,
    version: 2 as const,
    updatedAt: Math.max(pool.updatedAt || 0, now),
    stars: [...byUrl.values()],
  };
  saveLocalPool(next, storage);
  return next;
}

export function isArticleStarred(
  article: { id: string; url: string },
  storage: Pick<Storage, "getItem"> = localStorage,
): boolean {
  const stars = new Map(
    loadLocalPool(storage).stars.map((row) => [normalizeUrl(row.url), row.starred]),
  );
  return articleKeys(article).some((key) => stars.get(key) === true);
}

function normalizeUrl(url: string): string {
  let t = url.trim();
  while (t.length > 1 && t.endsWith("/")) t = t.slice(0, -1);
  return t;
}

function mergeByUrl<T extends { url: string; updatedAt: number }>(
  local: T[],
  remote: T[],
  prefer?: (a: T, b: T) => T,
): T[] {
  const map = new Map<string, T>();
  for (const row of local) map.set(normalizeUrl(row.url), { ...row, url: normalizeUrl(row.url) });
  for (const row of remote) {
    const url = normalizeUrl(row.url);
    const next = { ...row, url };
    const prior = map.get(url);
    if (!prior || next.updatedAt > prior.updatedAt) map.set(url, next);
    else if (prefer && next.updatedAt === prior.updatedAt) map.set(url, prefer(prior, next));
  }
  return [...map.values()];
}

export function mergePoolDocuments(
  local: LanPoolDocument,
  remote: LanPoolDocument,
): LanPoolDocument {
  return {
    version: 2,
    updatedAt: Math.max(local.updatedAt || 0, remote.updatedAt || 0),
    subs: mergeByUrl(local.subs, remote.subs),
    reads: mergeByUrl(local.reads, remote.reads, (a, b) => (b.read ? b : a)),
    stars: mergeByUrl(local.stars, remote.stars, (a, b) => (b.starred ? b : a)),
    playback: mergeByUrl(local.playback, remote.playback, (a, b) =>
      b.positionMs > a.positionMs ? b : a,
    ),
  };
}

export async function syncLanPool(
  hubUrl: string,
  passphrase: string,
  invoke: TauriInvoke | null = invokeFn(),
  maxAttempts = 3,
): Promise<LanPoolDocument> {
  if (!hubUrl.trim() || !passphrase.trim()) {
    throw new Error("hub URL and passphrase required");
  }
  if (!invoke) {
    throw new Error("Desktop app required for LAN crypto (Tauri invoke missing)");
  }
  const base = hubUrl.replace(/\/$/, "");
  let lastError: Error | null = null;
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      return await syncLanPoolOnce(base, passphrase, invoke);
    } catch (err) {
      lastError = err instanceof Error ? err : new Error(String(err));
      if (!lastError.message.includes("412") || attempt === maxAttempts - 1) {
        throw lastError;
      }
    }
  }
  throw lastError ?? new Error("LAN sync failed");
}

async function syncLanPoolOnce(
  base: string,
  passphrase: string,
  invoke: TauriInvoke,
): Promise<LanPoolDocument> {
  const poolResp = await fetch(`${base}/v1/pool`);
  const etag = poolResp.headers.get("etag") || "0";
  let authSalt = poolResp.headers.get("x-auth-salt-b64") || "";
  if (!authSalt) {
    const meta = (await (await fetch(`${base}/v1/meta`)).json()) as { authSaltB64: string };
    authSalt = meta.authSaltB64;
  }
  let remote = emptyPoolDocument();
  if (poolResp.status === 200) {
    const envelope = await poolResp.text();
    const plain = await invoke<string>("open_pool", { passphrase, envelopeJson: envelope });
    remote = { ...emptyPoolDocument(), ...(JSON.parse(plain) as LanPoolDocument), version: 2 };
  } else if (poolResp.status !== 404) {
    throw new Error(`GET pool HTTP ${poolResp.status}`);
  }
  const local = loadLocalPool();
  const merged = mergePoolDocuments(local, remote);
  const toPush = { ...merged, version: 2, updatedAt: Date.now() };
  const sealed = await invoke<string>("seal_pool", {
    passphrase,
    plaintext: JSON.stringify(toPush),
  });
  const bearer = await invoke<string>("lan_put_bearer", {
    passphrase,
    authSaltB64: authSalt,
    etag,
  });
  const put = await fetch(`${base}/v1/pool`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      "If-Match": etag,
      Authorization: `Bearer ${bearer}`,
    },
    body: sealed,
  });
  if (!put.ok) {
    throw new Error(`PUT pool HTTP ${put.status}`);
  }
  saveLocalPool(toPush);
  return toPush;
}
