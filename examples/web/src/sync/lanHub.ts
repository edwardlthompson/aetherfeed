/** Desktop LAN pool hub bridge (Tauri invoke). */

import { detectTauriInvoke, type TauriInvoke } from "./tauriInvoke";

export type LanHubStatus = {
  running: boolean;
  port: number;
  etag: string;
  authSaltB64: string;
  lanUrls: string[];
};

function invokeFn(): TauriInvoke | null {
  return detectTauriInvoke();
}

export async function lanHubStatus(
  invoke: TauriInvoke | null = invokeFn(),
): Promise<LanHubStatus | null> {
  if (!invoke) return null;
  return invoke<LanHubStatus>("lan_hub_status");
}

export async function lanHubStart(
  passphrase: string,
  port?: number,
  invoke: TauriInvoke | null = invokeFn(),
): Promise<LanHubStatus> {
  if (!invoke) throw new Error("Tauri invoke unavailable");
  if (!passphrase.trim()) throw new Error("passphrase must not be empty");
  return invoke<LanHubStatus>("lan_hub_start", { passphrase, port: port ?? null });
}

export async function lanHubStop(invoke: TauriInvoke | null = invokeFn()): Promise<LanHubStatus> {
  if (!invoke) throw new Error("Tauri invoke unavailable");
  return invoke<LanHubStatus>("lan_hub_stop");
}
