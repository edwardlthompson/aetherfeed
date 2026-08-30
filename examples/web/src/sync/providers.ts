import type { SyncProvider, SyncProviderId } from "@aetherfeed/domain";
import { pullFeedsBlob, pushFeedsBlob } from "./driveAppData";

export function createLocalOnlyProvider(): SyncProvider {
  return {
    id: "local-only" satisfies SyncProviderId,
    async pull() {
      return null;
    },
    async push() {
      return;
    },
  };
}

export function createDriveAppDataProvider(accessToken: () => string | null): SyncProvider {
  return {
    id: "drive-appdata" satisfies SyncProviderId,
    pull: async () => {
      const token = accessToken();
      return token ? pullFeedsBlob(token) : null;
    },
    push: async (blob) => {
      const token = accessToken();
      if (!token) return;
      await pushFeedsBlob(token, blob);
    },
  };
}
