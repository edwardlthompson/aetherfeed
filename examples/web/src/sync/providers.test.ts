import { describe, expect, it } from "vitest";
import { createDriveAppDataProvider, createLocalOnlyProvider } from "./providers";

describe("sync providers", () => {
  it("local-only never uploads a blob", async () => {
    const provider = createLocalOnlyProvider();
    expect(provider.id).toBe("local-only");
    expect(await provider.pull()).toBeNull();
  });

  it("drive-appdata no-ops without a token", async () => {
    const provider = createDriveAppDataProvider(() => null);
    expect(provider.id).toBe("drive-appdata");
    expect(await provider.pull()).toBeNull();
  });
});
