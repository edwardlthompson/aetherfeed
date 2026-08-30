import { tauriInvoke } from "../sync/tauriInvoke";
import { loadImportedFeeds } from "./importedStore";
import { applyReaderImportFile } from "./importFlow";

export async function seedImportIfEmpty(): Promise<number> {
  if (loadImportedFeeds().length) return 0;
  const path = (import.meta.env as { VITE_SEED_OPML_PATH?: string }).VITE_SEED_OPML_PATH?.trim();
  const invoke = tauriInvoke();
  if (!path || !invoke) return 0;
  const text = String(await invoke("read_text_path", { path }));
  if (!text.trim()) return 0;
  const result = await applyReaderImportFile({ name: path, text });
  return result.feedsAdded;
}
