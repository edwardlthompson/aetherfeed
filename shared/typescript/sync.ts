/** Versioned encrypted sync envelope. Provider stores only .enc blobs. */

export const SYNC_DOCUMENT_VERSION = 1;

export type SyncDocument = {
  version: typeof SYNC_DOCUMENT_VERSION;
  deviceId: string;
  updatedAt: number;
  clocks: Record<string, number>;
  payload: Record<string, unknown>;
};

export type SyncEnvelope = {
  version: typeof SYNC_DOCUMENT_VERSION;
  kdf: "argon2id";
  aead: "xchacha20poly1305";
  saltB64: string;
  nonceB64: string;
  ciphertextB64: string;
};

export type SyncProviderId = "drive-appdata" | "webdav" | "local-only";

export type SyncProvider = {
  id: SyncProviderId;
  pull(): Promise<Uint8Array | null>;
  push(blob: Uint8Array): Promise<void>;
};

export function isOpaqueEncName(name: string): boolean {
  return name.endsWith(".enc") && !name.includes("..");
}

export function mergeLww<T extends { updatedAt: number }>(
  local: T | undefined,
  remote: T | undefined,
): T | undefined {
  if (!local) return remote;
  if (!remote) return local;
  return remote.updatedAt >= local.updatedAt ? remote : local;
}
