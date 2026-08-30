import type { Article } from "@aetherfeed/domain";

export function bindNewsShare(
  shown: Article[],
  selectedId: string | null,
  signal: AbortSignal,
): void {
  window.addEventListener(
    "af-share",
    () => {
      const url = shown.find((row) => row.id === selectedId)?.url.trim();
      if (!url) return;
      if (navigator.share) void navigator.share({ url });
      else void navigator.clipboard?.writeText(url);
    },
    { signal },
  );
}

export function bindShareUrl(getUrl: () => string | undefined): void {
  window.addEventListener("af-share", () => {
    const url = getUrl()?.trim();
    if (!url) return;
    if (navigator.share) void navigator.share({ url });
    else void navigator.clipboard?.writeText(url);
  });
}

export function dispatchLibraryPick(value: string): boolean {
  if (!value) return false;
  window.dispatchEvent(new CustomEvent("af-library-pick", { detail: value }));
  return true;
}
