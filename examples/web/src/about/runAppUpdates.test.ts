import { beforeEach, describe, expect, it, vi } from "vitest";
import { MS_DAY } from "./productUpdate";
import { decideLaunchPrompt } from "./runAppUpdates";
import { loadUpdatePrefs, markUpdateChecked, markVersionSeen } from "./updatePrefs";

describe("decideLaunchPrompt", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("records the first version without a donate note", async () => {
    const fetchLatest = vi.fn(async () => null);
    await expect(decideLaunchPrompt("0.1.0", 0, fetchLatest)).resolves.toBeNull();
    expect(loadUpdatePrefs().lastSeenVersion).toBe("0.1.0");
    expect(fetchLatest).toHaveBeenCalled();
  });

  it("nudges donate only after a version change", async () => {
    markVersionSeen("0.1.0");
    const fetchLatest = vi.fn(async () => {
      throw new Error("should not fetch");
    });
    await expect(decideLaunchPrompt("0.2.0", 0, fetchLatest)).resolves.toEqual({ kind: "donate" });
    expect(fetchLatest).not.toHaveBeenCalled();
  });

  it("prompts install for a newer undismissed exe and silences Later", async () => {
    markVersionSeen("0.1.0");
    const fetchLatest = vi.fn(async () => ({
      htmlUrl: "https://github.com/edwardlthompson/aetherfeed/releases/latest",
      assets: [
        {
          name: "AetherFeed-0.2.0-x64-setup.exe",
          url: "https://ex/AetherFeed-0.2.0-x64-setup.exe",
        },
      ],
    }));
    await expect(decideLaunchPrompt("0.1.0", 0, fetchLatest)).resolves.toEqual({
      kind: "update",
      version: "0.2.0",
      url: "https://ex/AetherFeed-0.2.0-x64-setup.exe",
    });
    markUpdateChecked(0, "0.2.0");
    await expect(decideLaunchPrompt("0.1.0", MS_DAY, fetchLatest)).resolves.toBeNull();
  });

  it("skips GitHub when the daily window has not elapsed", async () => {
    markVersionSeen("0.1.0");
    markUpdateChecked(0);
    const fetchLatest = vi.fn(async () => null);
    await expect(decideLaunchPrompt("0.1.0", MS_DAY - 1, fetchLatest)).resolves.toBeNull();
    expect(fetchLatest).not.toHaveBeenCalled();
  });

  it("skips GitHub when checks are disabled", async () => {
    markVersionSeen("0.1.0");
    const fetchLatest = vi.fn(async () => null);
    await expect(decideLaunchPrompt("0.1.0", 0, fetchLatest, false)).resolves.toBeNull();
    expect(fetchLatest).not.toHaveBeenCalled();
  });
});
