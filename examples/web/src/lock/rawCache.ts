import { fromB64, openUtf8, toB64 } from "./crypto";
import { sessionVaultKey, vaultKeyB64 } from "./session";
import { memGet, memSet } from "./sessionMem";

function asBuf(bytes: Uint8Array): ArrayBuffer {
  return bytes.buffer.slice(bytes.byteOffset, bytes.byteOffset + bytes.byteLength) as ArrayBuffer;
}

async function importAes(key: Uint8Array): Promise<CryptoKey> {
  return crypto.subtle.importKey("raw", key.subarray(0, 32), "AES-GCM", false, [
    "encrypt",
    "decrypt",
  ]);
}

export async function wrapRawUtf8(plain: string, key: Uint8Array): Promise<string> {
  const nonce = crypto.getRandomValues(new Uint8Array(12));
  const ct = new Uint8Array(
    await crypto.subtle.encrypt(
      { name: "AES-GCM", iv: asBuf(nonce) },
      await importAes(key),
      new TextEncoder().encode(plain),
    ),
  );
  const out = new Uint8Array(16 + ct.length);
  out.set([0x41, 0x46, 0x33, 3], 0);
  out.set(nonce, 4);
  out.set(ct, 16);
  return toB64(out);
}

export async function openStoredUtf8(stored: string, key: Uint8Array): Promise<string> {
  if (stored.startsWith("{")) {
    const vault = vaultKeyB64();
    if (!vault) throw new Error("locked");
    return openUtf8(stored, vault);
  }
  const blob = fromB64(stored);
  if (blob.length > 16 && blob[0] === 0x41 && blob[1] === 0x46 && blob[2] === 0x33) {
    const plain = await crypto.subtle.decrypt(
      { name: "AES-GCM", iv: asBuf(blob.subarray(4, 16)) },
      await importAes(key),
      asBuf(blob.subarray(16)),
    );
    return new TextDecoder().decode(plain);
  }
  throw new Error("unsupported cache");
}

export async function readItem(key: string): Promise<string | null> {
  const hit = memGet(key);
  if (hit !== undefined) return hit;
  const raw = localStorage.getItem(key);
  const vault = sessionVaultKey();
  if (!raw || !vault) return null;
  const plain = await openStoredUtf8(raw, vault);
  memSet(key, plain);
  if (raw.startsWith("{")) localStorage.setItem(key, await wrapRawUtf8(plain, vault));
  return plain;
}

export async function writeItem(key: string, plain: string): Promise<void> {
  const vault = sessionVaultKey();
  if (!vault) return;
  memSet(key, plain);
  localStorage.setItem(key, await wrapRawUtf8(plain, vault));
}
