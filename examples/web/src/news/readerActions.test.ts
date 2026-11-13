import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createReaderActions } from "./readerActions";

describe("createReaderActions", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("wires star, park unread, and browser actions", () => {
    const onToggleStar = vi.fn();
    const onParkUnread = vi.fn();
    const onOpenBrowser = vi.fn();
    const onReaderMode = vi.fn();
    root.append(
      createReaderActions({
        starred: false,
        read: true,
        readerMode: "READING",
        onToggleStar,
        onParkUnread,
        onOpenBrowser,
        onReaderMode,
      }),
    );
    expect(root.querySelector(".af-reader-controls")?.children).toHaveLength(6);
    expect(
      root.querySelector<HTMLButtonElement>('[data-testid="article-star"]')?.title,
    ).toBeTruthy();
    expect(
      root.querySelector<HTMLButtonElement>('[data-testid="article-park-unread"]')?.title,
    ).toBeTruthy();
    expect(
      root.querySelector<HTMLButtonElement>('[data-testid="reader-mode-reading"]')?.title,
    ).toBeTruthy();
    root.querySelector<HTMLButtonElement>('[data-testid="article-star"]')?.click();
    root.querySelector<HTMLButtonElement>('[data-testid="article-park-unread"]')?.click();
    root.querySelector<HTMLButtonElement>('[data-testid="article-open-browser"]')?.click();
    root.querySelector<HTMLButtonElement>('[data-testid="reader-mode-web"]')?.click();
    expect(onToggleStar).toHaveBeenCalledOnce();
    expect(onParkUnread).toHaveBeenCalledOnce();
    expect(onOpenBrowser).toHaveBeenCalledOnce();
    expect(onReaderMode).toHaveBeenCalledWith("WEB");
  });
});
