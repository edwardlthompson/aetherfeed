import { loadLanClientPrefs } from "./lanClientPrefs";
import { syncLanPool } from "./lanPoolClient";

/** Pull the LAN pool on launch when hub URL + passphrase are already saved. */
export async function syncLanPoolOnStartup(): Promise<boolean> {
  const { hubUrl, passphrase } = loadLanClientPrefs();
  if (!hubUrl || !passphrase.trim()) return false;
  await syncLanPool(hubUrl, passphrase);
  return true;
}
