import { afterEach, describe, expect, it, vi } from "vitest";
import { SleepTimer } from "./sleepTimer";

afterEach(() => {
  vi.useRealTimers();
});

describe("SleepTimer", () => {
  it("counts down on a fake clock and fires onDone once", () => {
    vi.useFakeTimers();
    const onDone = vi.fn();
    const timer = new SleepTimer({ onDone });
    timer.start(60_000);
    expect(timer.remainingMs()).toBe(60_000);
    vi.advanceTimersByTime(20_000);
    expect(timer.remainingMs()).toBe(40_000);
    expect(onDone).not.toHaveBeenCalled();
    vi.advanceTimersByTime(40_000);
    expect(onDone).toHaveBeenCalledTimes(1);
    expect(timer.remainingMs()).toBe(0);
  });

  it("stop cancels remaining time and does not call onDone", () => {
    vi.useFakeTimers();
    const onDone = vi.fn();
    const timer = new SleepTimer({ onDone });
    timer.start(30_000);
    vi.advanceTimersByTime(5_000);
    timer.stop();
    expect(timer.remainingMs()).toBe(0);
    vi.advanceTimersByTime(30_000);
    expect(onDone).not.toHaveBeenCalled();
  });

  it("ignores empty or invalid durations", () => {
    vi.useFakeTimers();
    const onDone = vi.fn();
    const timer = new SleepTimer({ onDone });
    timer.start(Number.NaN);
    timer.start(-1);
    timer.start(0);
    expect(timer.remainingMs()).toBe(0);
    vi.advanceTimersByTime(1_000);
    expect(onDone).not.toHaveBeenCalled();
  });
});
