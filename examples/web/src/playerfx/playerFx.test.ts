import { describe, expect, it, vi } from "vitest";
import { playerFxCopy } from "./copy";
import { createPlayerFx } from "./playerFx";

describe("createPlayerFx", () => {
  it("mounts sleep controls and an empty chapter list", () => {
    const root = document.createElement("div");
    const pane = createPlayerFx(root);
    expect(root.contains(pane)).toBe(true);
    expect(pane.querySelector("[data-sleep-timer]")).toBeTruthy();
    expect(pane.querySelector("[data-sleep-off]")?.textContent).toBe(playerFxCopy.off);
    expect(pane.querySelector("[data-chapters-empty]")?.textContent).toBe(
      playerFxCopy.chaptersEmpty,
    );
    expect(pane.querySelector("[data-chapter-list]")).toBeNull();
  });

  it("lists one parsed chapter", () => {
    const root = document.createElement("div");
    const pane = createPlayerFx(root, { chapters: [{ title: "Intro", startMs: 0 }] });
    expect(pane.textContent).toContain("Intro");
    expect(pane.querySelector("[data-chapter]")?.getAttribute("data-start-ms")).toBe("0");
    expect(pane.querySelector("[data-chapters-empty]")).toBeNull();
  });

  it("starts and clears the sleep timer from the chrome", () => {
    vi.useFakeTimers();
    const onSleepDone = vi.fn();
    const root = document.createElement("div");
    const pane = createPlayerFx(root, { onSleepDone, presetsMs: [15_000] });
    pane.querySelector<HTMLButtonElement>("[data-sleep-ms]")?.click();
    expect(pane.querySelector("[data-sleep-remaining]")?.textContent).toMatch(/0:15/);
    vi.advanceTimersByTime(15_000);
    expect(onSleepDone).toHaveBeenCalledTimes(1);
    pane.querySelector<HTMLButtonElement>("[data-sleep-off]")?.click();
    expect(pane.querySelector("[data-sleep-remaining]")?.textContent).toMatch(/0:00/);
    vi.useRealTimers();
  });
});
