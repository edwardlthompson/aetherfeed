export type SleepTimerOptions = {
  onDone?: () => void;
  now?: () => number;
};

export class SleepTimer {
  onDone?: () => void;
  private readonly now: () => number;
  private endsAt = 0;
  private handle: ReturnType<typeof setTimeout> | undefined;

  constructor(options?: SleepTimerOptions) {
    this.onDone = options?.onDone;
    this.now = options?.now ?? Date.now;
  }

  start(durationMs: number): void {
    this.stop();
    if (!Number.isFinite(durationMs) || durationMs <= 0) return;
    const ms = Math.floor(durationMs);
    this.endsAt = this.now() + ms;
    this.handle = setTimeout(() => this.finish(), ms);
  }

  stop(): void {
    if (this.handle !== undefined) {
      clearTimeout(this.handle);
      this.handle = undefined;
    }
    this.endsAt = 0;
  }

  remainingMs(): number {
    if (this.endsAt <= 0) return 0;
    return Math.max(0, this.endsAt - this.now());
  }

  private finish(): void {
    this.handle = undefined;
    this.endsAt = 0;
    try {
      this.onDone?.();
    } catch {
      // Timer already elapsed; ignore listener failures.
    }
  }
}
