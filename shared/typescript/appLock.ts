/** Locked public API for the local PIN / passphrase gate. */

export type AppLockSecretKind = "pin" | "passphrase";

export type AppLockState = "unset" | "locked" | "unlocked";

export type AppLock = {
  setSecret(secret: string, kind: AppLockSecretKind): Promise<void>;
  unlock(secret: string): Promise<boolean>;
  lock(): void;
  wipe(): Promise<void>;
  state(): AppLockState;
};

export const APP_LOCK_TIMEOUT_MS = 120_000;
export const APP_LOCK_PIN_MIN = 6;
export const APP_LOCK_PASSPHRASE_MIN = 8;
export const APP_LOCK_BACKOFF_AFTER = 5;

export function classifyLockSecret(secret: string): AppLockSecretKind | null {
  const trimmed = secret.trim();
  if (/^\d+$/.test(trimmed) && trimmed.length >= APP_LOCK_PIN_MIN) return "pin";
  if (trimmed.length >= APP_LOCK_PASSPHRASE_MIN) return "passphrase";
  return null;
}
