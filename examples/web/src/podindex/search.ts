export type DirectoryHit = {
  title: string;
  feedUrl: string;
  author?: string;
};

export class DirectoryError extends Error {
  constructor(
    message: string,
    readonly kind: "empty" | "auth" | "network",
  ) {
    super(message);
    this.name = "DirectoryError";
  }
}

export async function searchDirectory(
  query: string,
  fetchImpl: typeof fetch = fetch,
): Promise<DirectoryHit[]> {
  const q = query.trim();
  if (!q) return [];
  const url = `https://itunes.apple.com/search?media=podcast&term=${encodeURIComponent(q)}`;
  try {
    const res = await fetchImpl(url, { signal: AbortSignal.timeout(8_000) });
    if (!res.ok) throw new DirectoryError("directory unavailable", "network");
    const body = (await res.json()) as {
      results?: { collectionName?: string; feedUrl?: string; artistName?: string }[];
    };
    return (body.results ?? [])
      .filter((row) => row.feedUrl)
      .map((row) => ({
        title: row.collectionName ?? row.feedUrl ?? "",
        feedUrl: row.feedUrl ?? "",
        author: row.artistName,
      }));
  } catch (err) {
    if (err instanceof DirectoryError) throw err;
    throw new DirectoryError("directory request aborted or failed", "network");
  }
}

export function isPrivateRss(url: string): boolean {
  return /:(?:[^/@]+)@/.test(url) || /[?&](token|auth|key)=/i.test(url);
}
