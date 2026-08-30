export const PRIVACY_COVER_ATTR = "data-privacy-cover";

let stopBound: (() => void) | undefined;

export function shouldCoverPreview(hidden: boolean, blurred: boolean): boolean {
  return hidden || blurred;
}

function ensureCover(host: HTMLElement): HTMLElement {
  const found = host.querySelector<HTMLElement>(`[${PRIVACY_COVER_ATTR}]`);
  if (found) return found;
  const el = document.createElement("div");
  el.setAttribute(PRIVACY_COVER_ATTR, "");
  el.setAttribute("aria-hidden", "true");
  el.style.cssText =
    "position:fixed;inset:0;background:#000;z-index:2147483647;pointer-events:none;";
  host.append(el);
  return el;
}

function pageHidden(): boolean {
  return document.visibilityState === "hidden" || document.hidden;
}

export function bindPrivacyCover(host: HTMLElement = document.body): () => void {
  stopBound?.();
  const cover = ensureCover(host);
  let blurred = false;
  const sync = (): void => {
    const show = shouldCoverPreview(pageHidden(), blurred);
    cover.hidden = !show;
    cover.style.display = show ? "block" : "none";
  };
  const onVis = (): void => {
    if (!pageHidden()) blurred = false;
    sync();
  };
  const onBlur = (): void => {
    blurred = true;
    sync();
  };
  const onFocus = (): void => {
    blurred = false;
    sync();
  };
  document.addEventListener("visibilitychange", onVis);
  window.addEventListener("blur", onBlur);
  window.addEventListener("focus", onFocus);
  sync();
  const stop = (): void => {
    document.removeEventListener("visibilitychange", onVis);
    window.removeEventListener("blur", onBlur);
    window.removeEventListener("focus", onFocus);
    cover.remove();
    if (stopBound === stop) stopBound = undefined;
  };
  stopBound = stop;
  return stop;
}
