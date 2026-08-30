import type { AppLockSecretKind } from "@aetherfeed/domain";

const ITERATIONS = 210_000;

export type LockEnvelope = {
  version: 1;
  kdf: "pbkdf2";
  aead: "aes-256-gcm";
  saltB64: string;
  nonceB64: string;
  ciphertextB64: string;
  kind?: AppLockSecretKind;
};

export function peekLockKind(raw: string | null | undefined): AppLockSecretKind | null {
  if (!raw) return null;
  try {
    const kind = (JSON.parse(raw) as LockEnvelope).kind;
    return kind === "pin" || kind === "passphrase" ? kind : null;
  } catch {
    return null;
  }
}

function asBuffer(bytes: Uint8Array): ArrayBuffer {
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
}

export function toB64(bytes: ArrayBuffer | Uint8Array): string {
  const view = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  let bin = "";
  for (const byte of view) bin += String.fromCharCode(byte);
  return btoa(bin);
}

export function fromB64(value: string): Uint8Array {
  const bin = atob(value);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i += 1) out[i] = bin.charCodeAt(i);
  return out;
}

async function deriveKey(secret: string, salt: Uint8Array): Promise<CryptoKey> {
  const material = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(secret),
    "PBKDF2",
    false,
    ["deriveKey"],
  );
  return crypto.subtle.deriveKey(
    { name: "PBKDF2", salt: asBuffer(salt), iterations: ITERATIONS, hash: "SHA-256" },
    material,
    { name: "AES-GCM", length: 256 },
    false,
    ["encrypt", "decrypt"],
  );
}

export async function wrapBytes(
  plain: Uint8Array,
  secret: string,
  kind?: AppLockSecretKind,
): Promise<LockEnvelope> {
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const nonce = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(secret, salt);
  const ciphertext = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv: asBuffer(nonce) },
    key,
    asBuffer(plain),
  );
  return {
    version: 1,
    kdf: "pbkdf2",
    aead: "aes-256-gcm",
    saltB64: toB64(salt),
    nonceB64: toB64(nonce),
    ciphertextB64: toB64(ciphertext),
    ...(kind ? { kind } : {}),
  };
}

export async function openBytes(envelope: LockEnvelope, secret: string): Promise<Uint8Array> {
  if (envelope.version !== 1 || envelope.aead !== "aes-256-gcm") {
    throw new Error("unsupported envelope");
  }
  const key = await deriveKey(secret, fromB64(envelope.saltB64));
  const plain = await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: asBuffer(fromB64(envelope.nonceB64)) },
    key,
    asBuffer(fromB64(envelope.ciphertextB64)),
  );
  return new Uint8Array(plain);
}

export async function wrapUtf8(plain: string, secret: string): Promise<string> {
  return JSON.stringify(await wrapBytes(new TextEncoder().encode(plain), secret));
}

export async function openUtf8(raw: string, secret: string): Promise<string> {
  const envelope = JSON.parse(raw) as LockEnvelope;
  return new TextDecoder().decode(await openBytes(envelope, secret));
}
