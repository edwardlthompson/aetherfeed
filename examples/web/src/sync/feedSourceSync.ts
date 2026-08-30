import {
  decodeFeedDocument,
  encodeFeedDocument,
  type Feed,
  mergeFeedSources,
} from "@aetherfeed/domain";
import { loadImportedFeeds, saveImportedFeeds } from "../readerimport/importedStore";
import { pullFeedsBlob, pushFeedsBlob } from "./driveAppData";
import { exchangeAuthCode, refreshDriveToken } from "./driveAuth";
import {
  clearDriveTokens,
  getDriveClientId,
  getDriveClientSecret,
  getSyncPassphrase,
  loadDriveTokens,
  saveDriveTokens,
} from "./driveStore";
import { openJson, sealJson } from "./envelope";

export type FeedSyncResult = {
  count: number;
  pulled: number;
};

async function validAccessToken(): Promise<string> {
  const tokens = loadDriveTokens();
  if (!tokens) throw new Error("Google Drive is not connected");
  if (tokens.expiresAt - 60_000 > Date.now()) return tokens.accessToken;
  if (!tokens.refreshToken) throw new Error("Drive session expired — connect again");
  const next = await refreshDriveToken(
    getDriveClientId(),
    getDriveClientSecret(),
    tokens.refreshToken,
  );
  saveDriveTokens(next);
  return next.accessToken;
}

export async function completeDriveConnect(code: string, verifier: string): Promise<void> {
  const tokens = await exchangeAuthCode(getDriveClientId(), getDriveClientSecret(), code, verifier);
  saveDriveTokens(tokens);
}

export function disconnectDrive(): void {
  clearDriveTokens();
}

export async function syncFeedSources(now = Date.now()): Promise<FeedSyncResult> {
  const passphrase = getSyncPassphrase();
  if (!passphrase) throw new Error("Set the same sync passphrase on phone and PC");
  const token = await validAccessToken();
  const remoteBlob = await pullFeedsBlob(token);
  let remote: Feed[] = [];
  if (remoteBlob) {
    remote = decodeFeedDocument(await openJson(remoteBlob, passphrase)).feeds;
  }
  const merged = mergeFeedSources(loadImportedFeeds(), remote);
  saveImportedFeeds(merged);
  await pushFeedsBlob(token, await sealJson(encodeFeedDocument(merged, now), passphrase));
  return { count: merged.length, pulled: remote.length };
}

export async function syncFeedSourcesIfConnected(): Promise<FeedSyncResult | null> {
  if (!loadDriveTokens()) return null;
  return syncFeedSources();
}
