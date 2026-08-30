import type { ReaderImportFile } from "@aetherfeed/domain";
import { tauriInvoke } from "../sync/tauriInvoke";

export function canPickDesktopFile(): boolean {
  return tauriInvoke() !== null;
}

export async function pickDesktopImportFile(): Promise<ReaderImportFile | null> {
  const invoke = tauriInvoke();
  if (!invoke) return null;
  const picked = (await invoke("pick_import_file")) as { name: string; text: string } | null;
  if (!picked?.name) return null;
  return { name: picked.name, text: picked.text };
}
