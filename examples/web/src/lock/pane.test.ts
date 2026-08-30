import { afterEach, describe, expect, it, vi } from "vitest";
import { createUnlockPane } from "./pane";
import { webAppLock } from "./session";

afterEach(async () => {
  await webAppLock.wipe();
});

describe("createUnlockPane", () => {
  it("sets a PIN on first run", async () => {
    const root = document.createElement("div");
    let unlocked = false;
    createUnlockPane(root, () => {
      unlocked = true;
    });
    const input = root.querySelector<HTMLInputElement>("[data-lock-secret]");
    expect(input?.inputMode).toBe("numeric");
    expect(input?.dataset.lockKind).toBe("pin");
    if (input) input.value = "123456";
    root
      .querySelector("form")
      ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
    await vi.waitFor(() => {
      expect(unlocked || webAppLock.state() === "unlocked").toBe(true);
    });
  });
});
