import {
  APP_LOCK_BACKOFF_AFTER,
  APP_LOCK_TIMEOUT_MS,
  type AppLock,
  type AppLockSecretKind,
  type AppLockState,
  classifyLockSecret,
} from "@aetherfeed/domain";
import { openBytes, peekLockKind, toB64, wrapBytes } from "./crypto";
import { clearPlainSession, wipeArticleKeys } from "./sessionMem";

export const WRAP_KEY = "af-lock-wrap";
export const VAULT_BYTES = 32;

let sessionKey: Uint8Array | null = null;
let failures = 0;
let backoffUntil = 0;
let lockedAt = 0;
let expireTimer = 0;
let notifying = false;
const lockListeners = new Set<() => void>();

function randomVaultKey(): Uint8Array {
  return crypto.getRandomValues(new Uint8Array(VAULT_BYTES));
}

function notifyLock(): void {
  if (notifying) return;
  notifying = true;
  try {
    lockListeners.forEach((fn) => fn());
  } finally {
    notifying = false;
  }
}

function clearExpire(): void {
  if (expireTimer) window.clearTimeout(expireTimer);
  expireTimer = 0;
}

function dropSession(): void {
  sessionKey = null;
  lockedAt = 0;
  clearExpire();
  clearPlainSession();
}

function armExpire(): void {
  clearExpire();
  if (!sessionKey || !lockedAt) return;
  const wait = Math.max(250, APP_LOCK_TIMEOUT_MS - (Date.now() - lockedAt));
  expireTimer = window.setTimeout(() => {
    if (!sessionKey || !lockedAt) return;
    if (Date.now() - lockedAt < APP_LOCK_TIMEOUT_MS) {
      armExpire();
      return;
    }
    dropSession();
    notifyLock();
  }, wait);
}

export function subscribeLockChange(listener: () => void): () => void {
  lockListeners.add(listener);
  return () => lockListeners.delete(listener);
}

export function sessionVaultKey(): Uint8Array | null {
  if (!sessionKey) return null;
  if (lockedAt && Date.now() - lockedAt >= APP_LOCK_TIMEOUT_MS) {
    dropSession();
    queueMicrotask(() => notifyLock());
    return null;
  }
  return sessionKey;
}

export function touchUnlock(): void {
  if (!sessionKey) return;
  lockedAt = Date.now();
  armExpire();
}

export const webAppLock: AppLock = {
  async setSecret(secret: string, kind: AppLockSecretKind): Promise<void> {
    if (classifyLockSecret(secret) !== kind) throw new Error("weak secret");
    const vault = randomVaultKey();
    const envelope = await wrapBytes(vault, secret, kind);
    localStorage.setItem(WRAP_KEY, JSON.stringify(envelope));
    sessionKey = vault;
    lockedAt = Date.now();
    failures = 0;
    armExpire();
  },

  async unlock(secret: string): Promise<boolean> {
    if (Date.now() < backoffUntil) return false;
    const raw = localStorage.getItem(WRAP_KEY);
    if (!raw) return false;
    try {
      sessionKey = await openBytes(JSON.parse(raw), secret);
      lockedAt = Date.now();
      failures = 0;
      armExpire();
      return true;
    } catch {
      failures += 1;
      if (failures >= APP_LOCK_BACKOFF_AFTER) {
        backoffUntil = Date.now() + 2 ** Math.min(failures - APP_LOCK_BACKOFF_AFTER, 6) * 1000;
      }
      sessionKey = null;
      return false;
    }
  },

  lock(): void {
    dropSession();
    notifyLock();
  },

  async wipe(): Promise<void> {
    failures = 0;
    dropSession();
    localStorage.removeItem(WRAP_KEY);
    localStorage.removeItem("af-reader-vault");
    localStorage.removeItem("af-imported-feeds");
    localStorage.removeItem("af-lock-feeds");
    localStorage.removeItem("af-lock-articles");
    localStorage.removeItem("af-lock-thumbs");
    wipeArticleKeys();
    notifyLock();
  },

  state(): AppLockState {
    if (!localStorage.getItem(WRAP_KEY)) return "unset";
    return sessionVaultKey() ? "unlocked" : "locked";
  },
};

export function storedLockKind(): AppLockSecretKind | null {
  return peekLockKind(localStorage.getItem(WRAP_KEY));
}

export function vaultKeyB64(): string | null {
  const key = sessionVaultKey();
  return key ? toB64(key) : null;
}
