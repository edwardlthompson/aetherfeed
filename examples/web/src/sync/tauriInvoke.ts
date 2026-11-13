export type TauriInvoke = <T>(cmd: string, args?: Record<string, unknown>) => Promise<T>;

type Internals = {
  invoke?: (cmd: string, args?: Record<string, unknown>) => Promise<unknown>;
};

type GlobalTauri = {
  core?: { invoke?: TauriInvoke };
  invoke?: TauriInvoke;
};

/** Tauri 2 uses `__TAURI_INTERNALS__`; v1 / withGlobalTauri uses `__TAURI__.core`. */
export function detectTauriInvoke(): TauriInvoke | null {
  const g = globalThis as {
    __TAURI_INTERNALS__?: Internals;
    __TAURI__?: GlobalTauri;
  };
  const internals = g.__TAURI_INTERNALS__?.invoke;
  if (internals) {
    return ((cmd, args) => internals(cmd, args ?? {})) as TauriInvoke;
  }
  return g.__TAURI__?.core?.invoke ?? g.__TAURI__?.invoke ?? null;
}
