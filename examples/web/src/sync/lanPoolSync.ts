import { mergePoolDocuments } from "./lanPoolMerge";
import { emptyPoolDocument, loadLocalPool, saveLocalPool } from "./lanPoolStorage";
import type { LanPoolDocument } from "./lanPoolTypes";
import { detectTauriInvoke, type TauriInvoke } from "./tauriInvoke";

function invokeFn(): TauriInvoke | null {
  return detectTauriInvoke();
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
