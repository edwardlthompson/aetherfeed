import { GITHUB_RELEASES_PAGE } from "./donate";
import { fetchLatestGithubRelease } from "./githubRelease";
import {
  selectProductAsset,
  shouldCheckDaily,
  shouldNudgeDonate,
  shouldPromptUpdate,
} from "./productUpdate";
import { loadUpdatePrefs, markUpdateChecked, markVersionSeen } from "./updatePrefs";

export type LaunchPrompt = { kind: "donate" } | { kind: "update"; version: string; url: string };

export async function decideLaunchPrompt(
  currentVersion: string,
  now = Date.now(),
  fetchLatest = fetchLatestGithubRelease,
  checksEnabled = true,
): Promise<LaunchPrompt | null> {
  const prefs = loadUpdatePrefs();
  if (shouldNudgeDonate(prefs.lastSeenVersion, currentVersion)) {
    return { kind: "donate" };
  }
  markVersionSeen(currentVersion);
  if (!checksEnabled || !shouldCheckDaily(prefs.lastCheckAt, now)) return null;
  const release = await fetchLatest(currentVersion);
  markUpdateChecked(now);
  if (!release) return null;
  const asset = selectProductAsset(release.assets, "exe");
  const latest = asset?.version ?? null;
  if (!shouldPromptUpdate(currentVersion, latest, prefs.dismissedVersion) || !latest) {
    return null;
  }
  return {
    kind: "update",
    version: latest,
    url: asset?.url || release.htmlUrl || GITHUB_RELEASES_PAGE,
  };
}
