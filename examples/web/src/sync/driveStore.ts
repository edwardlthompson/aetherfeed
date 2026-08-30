import type { DriveTokens } from "./driveAuth";

const CLIENT_ID_KEY = "af-drive-client-id";
const CLIENT_SECRET_KEY = "af-drive-client-secret";
const PASSPHRASE_KEY = "af-drive-passphrase";
const TOKENS_KEY = "af-drive-tokens";

function envValue(
  name: "VITE_DRIVE_CLIENT_ID" | "VITE_DRIVE_CLIENT_SECRET" | "VITE_DRIVE_PASSPHRASE",
): string {
  const env = import.meta.env as Record<string, string | undefined>;
  return env[name] ?? "";
}

export function getDriveClientId(): string {
  return localStorage.getItem(CLIENT_ID_KEY) || envValue("VITE_DRIVE_CLIENT_ID");
}

export function setDriveClientId(value: string): void {
  localStorage.setItem(CLIENT_ID_KEY, value.trim());
}

export function getDriveClientSecret(): string {
  return localStorage.getItem(CLIENT_SECRET_KEY) || envValue("VITE_DRIVE_CLIENT_SECRET");
}

export function setDriveClientSecret(value: string): void {
  localStorage.setItem(CLIENT_SECRET_KEY, value.trim());
}

export function getSyncPassphrase(): string {
  return localStorage.getItem(PASSPHRASE_KEY) || envValue("VITE_DRIVE_PASSPHRASE");
}

export function setSyncPassphrase(value: string): void {
  localStorage.setItem(PASSPHRASE_KEY, value);
}

export function ensureSyncPassphrase(): string {
  const existing = getSyncPassphrase();
  if (existing) return existing;
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  const generated = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
  setSyncPassphrase(generated);
  return generated;
}

export function loadDriveTokens(): DriveTokens | null {
  try {
    const raw = localStorage.getItem(TOKENS_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as DriveTokens;
    return parsed.accessToken ? parsed : null;
  } catch {
    return null;
  }
}

export function saveDriveTokens(tokens: DriveTokens): void {
  localStorage.setItem(TOKENS_KEY, JSON.stringify(tokens));
}

export function clearDriveTokens(): void {
  localStorage.removeItem(TOKENS_KEY);
}

export function isDriveConnected(): boolean {
  return Boolean(loadDriveTokens()?.refreshToken || loadDriveTokens()?.accessToken);
}
