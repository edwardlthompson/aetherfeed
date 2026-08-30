type Invoke = (cmd: string, args?: Record<string, unknown>) => Promise<unknown>;

export function isTauriRuntime(): boolean {
  const win = window as { __TAURI__?: unknown; __TAURI_INTERNALS__?: unknown };
  return Boolean(win.__TAURI_INTERNALS__ || win.__TAURI__);
}

export function tauriInvoke(): Invoke | null {
  if (!isTauriRuntime()) return null;
  return async (cmd, args) => {
    const mod = await import("@tauri-apps/api/core");
    return mod.invoke(cmd, args);
  };
}

export async function waitForOauthCode(port: number): Promise<string> {
  const invoke = tauriInvoke();
  if (!invoke) throw new Error("Desktop loopback is only available in the AetherFeed Windows app");
  return String(await invoke("oauth_wait", { port }));
}
