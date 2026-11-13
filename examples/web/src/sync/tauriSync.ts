import { detectTauriInvoke, type TauriInvoke } from "./tauriInvoke";

export type { TauriInvoke };

/** Desktop bridge: delegates to Rust `sync_pull` / `sync_push` when `window.__TAURI__` is present. */
export async function tauriSyncPull(
  endpoint: string,
  invoke?: TauriInvoke,
): Promise<Uint8Array | null> {
  const run = resolveInvoke(invoke);
  const bytes = await run<number[] | null>("sync_pull", { endpoint });
  if (!bytes || bytes.length === 0) {
    return null;
  }
  return Uint8Array.from(bytes);
}

export async function tauriSyncPush(
  endpoint: string,
  blob: Uint8Array,
  invoke?: TauriInvoke,
): Promise<void> {
  const run = resolveInvoke(invoke);
  await run("sync_push", { endpoint, blob: Array.from(blob) });
}

function resolveInvoke(invoke?: TauriInvoke): TauriInvoke {
  if (invoke) {
    return invoke;
  }
  const coreInvoke = detectTauriInvoke();
  if (!coreInvoke) {
    throw new Error("Tauri invoke unavailable — use memory WebDAV in tests");
  }
  return coreInvoke;
}
