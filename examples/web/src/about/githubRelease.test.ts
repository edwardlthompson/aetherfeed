import { describe, expect, it } from "vitest";
import { GITHUB_RELEASES_API } from "./donate";
import { fetchLatestGithubRelease, parseGithubRelease } from "./githubRelease";

describe("parseGithubRelease", () => {
  it("keeps named installer assets and ignores junk", () => {
    const parsed = parseGithubRelease({
      html_url: "https://github.com/edwardlthompson/aetherfeed/releases/tag/v1",
      assets: [
        { name: "notes.md" },
        { name: "AetherFeed-0.2.0-x64-setup.exe", browser_download_url: "https://ex/setup.exe" },
      ],
    });
    expect(parsed?.assets).toEqual([
      { name: "AetherFeed-0.2.0-x64-setup.exe", url: "https://ex/setup.exe" },
    ]);
  });

  it("returns null for non-objects", () => {
    expect(parseGithubRelease(null)).toBeNull();
    expect(parseGithubRelease("x")).toBeNull();
  });
});

describe("fetchLatestGithubRelease", () => {
  it("sends a User-Agent and treats failures as silent", async () => {
    const fetchImpl: typeof fetch = async (url, init) => {
      expect(String(url)).toBe(GITHUB_RELEASES_API);
      expect(new Headers(init?.headers).get("User-Agent")).toBe("AetherFeed/0.1.0");
      return new Response("nope", { status: 503 });
    };
    await expect(fetchLatestGithubRelease("0.1.0", fetchImpl)).resolves.toBeNull();
  });

  it("parses a successful release payload", async () => {
    const fetchImpl: typeof fetch = async () =>
      new Response(
        JSON.stringify({
          html_url: "https://ex",
          assets: [
            {
              name: "AetherFeed-1.0.0-x64-setup.exe",
              browser_download_url: "https://ex/setup.exe",
            },
          ],
        }),
        { status: 200 },
      );
    await expect(fetchLatestGithubRelease("1.0.0", fetchImpl)).resolves.toEqual({
      htmlUrl: "https://ex",
      assets: [{ name: "AetherFeed-1.0.0-x64-setup.exe", url: "https://ex/setup.exe" }],
    });
  });

  it("returns null on timeout or throw", async () => {
    const fetchImpl: typeof fetch = async () => {
      throw new Error("network");
    };
    await expect(fetchLatestGithubRelease("0.1.0", fetchImpl)).resolves.toBeNull();
  });
});
