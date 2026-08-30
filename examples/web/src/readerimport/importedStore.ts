import type { Feed } from "@aetherfeed/domain";
import {
  FEEDS_KEY,
  loadEncryptedJson,
  openUtf8,
  saveEncryptedJson,
  vaultKeyB64,
  webAppLock,
  wrapUtf8,
} from "../lock";
import { tauriInvoke } from "../sync/tauriInvoke";

export const IMPORTED_FEEDS_KEY = "af-imported-feeds";

let memory: Feed[] | null = null;

function parseFeeds(raw: string): Feed[] {
  try {
    const parsed = JSON.parse(raw) as Feed[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function loadPlain(): Feed[] {
  const raw = localStorage.getItem(IMPORTED_FEEDS_KEY);
  return raw ? parseFeeds(raw) : [];
}

export function loadImportedFeeds(): Feed[] {
  if (webAppLock.state() === "locked") return [];
  if (memory) return memory;
  if (webAppLock.state() === "unlocked") return [];
  return loadPlain();
}

async function persistDesktop(raw: string): Promise<void> {
  const invoke = tauriInvoke();
  if (!invoke) return;
  const vault = vaultKeyB64();
  const payload = vault ? await wrapUtf8(raw, vault) : raw;
  await invoke("library_write", { json: payload });
}

export function saveImportedFeeds(feeds: Feed[]): void {
  memory = feeds;
  const raw = JSON.stringify(feeds);
  if (webAppLock.state() === "unlocked") {
    localStorage.removeItem(IMPORTED_FEEDS_KEY);
    void saveEncryptedJson(FEEDS_KEY, feeds);
    void persistDesktop(raw);
    const invoke = tauriInvoke();
    if (invoke) void invoke("seed_write", { json: raw });
    return;
  }
  localStorage.setItem(IMPORTED_FEEDS_KEY, raw);
}

export function clearImportedFeeds(): void {
  memory = [];
  localStorage.removeItem(IMPORTED_FEEDS_KEY);
  localStorage.removeItem(FEEDS_KEY);
  const invoke = tauriInvoke();
  if (invoke) void invoke("library_write", { json: "[]" });
}

async function readDesktop(): Promise<Feed[]> {
  const invoke = tauriInvoke();
  if (!invoke) return [];
  const raw = String((await invoke("library_read")) ?? "");
  if (!raw.trim() || raw === "[]") return [];
  const vault = vaultKeyB64();
  if (vault && raw.includes("ciphertextB64")) {
    try {
      return parseFeeds(await openUtf8(raw, vault));
    } catch {
      return [];
    }
  }
  return parseFeeds(raw);
}

export async function hydrateImportedFeeds(): Promise<void> {
  if (webAppLock.state() === "locked") {
    memory = null;
    return;
  }
  if (webAppLock.state() === "unlocked") {
    const enc = await loadEncryptedJson<Feed[]>(FEEDS_KEY, []);
    const desktop = await readDesktop();
    memory = enc.length ? enc : desktop.length ? desktop : loadPlain();
    if (memory.length) {
      await saveEncryptedJson(FEEDS_KEY, memory);
      const raw = JSON.stringify(memory);
      await persistDesktop(raw);
      const invoke = tauriInvoke();
      if (invoke) await invoke("seed_write", { json: raw });
    }
    localStorage.removeItem(IMPORTED_FEEDS_KEY);
    return;
  }
  const desktop = await readDesktop();
  memory = desktop.length ? desktop : loadPlain();
  if (memory.length) {
    localStorage.setItem(IMPORTED_FEEDS_KEY, JSON.stringify(memory));
  }
}
