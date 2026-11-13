export const LAN_CLIENT_URL_KEY = "af-lan-client-url";
export const LAN_CLIENT_PASS_KEY = "af-lan-client-pass";

export function loadLanClientPrefs(storage: Pick<Storage, "getItem"> = localStorage): {
  hubUrl: string;
  passphrase: string;
} {
  return {
    hubUrl: (storage.getItem(LAN_CLIENT_URL_KEY) || "").trim(),
    passphrase: storage.getItem(LAN_CLIENT_PASS_KEY) || "",
  };
}

export function saveLanClientPrefs(
  hubUrl: string,
  passphrase: string,
  storage: Pick<Storage, "setItem"> = localStorage,
): void {
  storage.setItem(LAN_CLIENT_URL_KEY, hubUrl);
  storage.setItem(LAN_CLIENT_PASS_KEY, passphrase);
}
