/** AES-256-GCM envelope. Passphrase stays on device. */

const ITERATIONS = 210_000;

export type FeedEnvelope = {
  version: 1;
  kdf: "pbkdf2";
  aead: "aes-256-gcm";
  saltB64: string;
  nonceB64: string;
  ciphertextB64: string;
};

function asBuffer(bytes: Uint8Array): ArrayBuffer {
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
}

function toB64(bytes: ArrayBuffer | Uint8Array): string {
  const view = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  let bin = "";
  for (const byte of view) bin += String.fromCharCode(byte);
  return btoa(bin);
}

function fromB64(value: string): Uint8Array {
  const bin = atob(value);
  const out = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i += 1) out[i] = bin.charCodeAt(i);
  return out;
}

async function deriveKey(passphrase: string, salt: Uint8Array): Promise<CryptoKey> {
  const material = await crypto.subtle.importKey(
    "raw",
    new TextEncoder().encode(passphrase),
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

export async function sealJson(plain: string, passphrase: string): Promise<Uint8Array> {
  if (!passphrase) throw new Error("sync passphrase required");
  const salt = crypto.getRandomValues(new Uint8Array(16));
  const nonce = crypto.getRandomValues(new Uint8Array(12));
  const key = await deriveKey(passphrase, salt);
  const ciphertext = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv: asBuffer(nonce) },
    key,
    new TextEncoder().encode(plain),
  );
  const envelope: FeedEnvelope = {
    version: 1,
    kdf: "pbkdf2",
    aead: "aes-256-gcm",
    saltB64: toB64(salt),
    nonceB64: toB64(nonce),
    ciphertextB64: toB64(ciphertext),
  };
  return new TextEncoder().encode(JSON.stringify(envelope));
}

export async function openJson(blob: Uint8Array, passphrase: string): Promise<string> {
  if (!passphrase) throw new Error("sync passphrase required");
  const envelope = JSON.parse(new TextDecoder().decode(blob)) as FeedEnvelope;
  if (envelope.version !== 1 || envelope.aead !== "aes-256-gcm") {
    throw new Error("unsupported envelope");
  }
  const key = await deriveKey(passphrase, fromB64(envelope.saltB64));
  const plain = await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: asBuffer(fromB64(envelope.nonceB64)) },
    key,
    asBuffer(fromB64(envelope.ciphertextB64)),
  );
  return new TextDecoder().decode(plain);
}
