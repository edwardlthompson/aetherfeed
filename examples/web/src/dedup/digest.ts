export async function fileDigest(bytes: ArrayBuffer | Uint8Array): Promise<string> {
  const view = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes);
  const copy = new Uint8Array(view.byteLength);
  copy.set(view);
  const hash = await crypto.subtle.digest("SHA-256", copy);
  return [...new Uint8Array(hash)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

export function createDigestSet(): Set<string> {
  return new Set<string>();
}

export function rememberDigest(seen: Set<string>, digest: string): boolean {
  if (seen.has(digest)) return false;
  seen.add(digest);
  return true;
}
