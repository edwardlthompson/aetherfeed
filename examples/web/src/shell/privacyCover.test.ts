import { afterEach, describe, expect, it } from "vitest";
import { bindPrivacyCover, PRIVACY_COVER_ATTR, shouldCoverPreview } from "./privacyCover";

function setHidden(hidden: boolean): void {
  Object.defineProperty(document, "hidden", { configurable: true, get: () => hidden });
  Object.defineProperty(document, "visibilityState", {
    configurable: true,
    get: () => (hidden ? "hidden" : "visible"),
  });
}

describe("privacyCover", () => {
  let stop: (() => void) | undefined;
  let host: HTMLElement | undefined;

  afterEach(() => {
    stop?.();
    stop = undefined;
    host?.remove();
    host = undefined;
    setHidden(false);
  });

  it("covers when hidden or blurred", () => {
    expect(shouldCoverPreview(true, false)).toBe(true);
    expect(shouldCoverPreview(false, true)).toBe(true);
    expect(shouldCoverPreview(false, false)).toBe(false);
  });

  it("blacks the host when the document is hidden", () => {
    host = document.createElement("div");
    document.body.append(host);
    stop = bindPrivacyCover(host);
    const cover = host.querySelector<HTMLElement>(`[${PRIVACY_COVER_ATTR}]`);
    expect(cover).not.toBeNull();
    expect(cover?.hidden).toBe(true);
    setHidden(true);
    document.dispatchEvent(new Event("visibilitychange"));
    expect(cover?.hidden).toBe(false);
    expect(cover?.style.background).toBe("rgb(0, 0, 0)");
  });

  it("blacks the host on window blur and clears on focus", () => {
    host = document.createElement("div");
    document.body.append(host);
    stop = bindPrivacyCover(host);
    const cover = host.querySelector<HTMLElement>(`[${PRIVACY_COVER_ATTR}]`);
    window.dispatchEvent(new Event("blur"));
    expect(cover?.hidden).toBe(false);
    window.dispatchEvent(new Event("focus"));
    expect(cover?.hidden).toBe(true);
  });
});
