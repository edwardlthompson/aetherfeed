import type { Episode, PlaybackPosition } from "@aetherfeed/domain";
import { classifyAudioError, messageFor, PlayerError } from "./errors";
import { getPosition, savePosition } from "./positionStore";

export type PlaybackState = "stopped" | "playing" | "paused";

export interface PodcastPlayer {
  play(episodeId: string): Promise<void>;
  pause(): Promise<void>;
  seekTo(positionMs: number): Promise<void>;
  position(episodeId: string): PlaybackPosition | null;
}

export class NoopPodcastPlayer implements PodcastPlayer {
  async play(episodeId: string): Promise<void> {
    void episodeId;
  }
  async pause(): Promise<void> {}
  async seekTo(positionMs: number): Promise<void> {
    void positionMs;
  }
  position(episodeId: string): PlaybackPosition | null {
    void episodeId;
    return null;
  }
}

export class HtmlAudioPodcastPlayer implements PodcastPlayer {
  activeId: string | null = null;
  playbackState: PlaybackState = "stopped";
  private lastSave = 0;

  constructor(
    private readonly audio: HTMLAudioElement,
    private readonly resolveEpisode: (episodeId: string) => Episode | undefined,
  ) {
    this.audio.addEventListener("timeupdate", () => this.persist(false));
    this.audio.addEventListener("pause", () => {
      if (this.playbackState === "playing") this.playbackState = "paused";
      this.persist(true);
    });
    this.audio.addEventListener("ended", () => {
      this.playbackState = "stopped";
      this.persist(true);
    });
  }

  position(episodeId: string): PlaybackPosition | null {
    return getPosition(episodeId);
  }

  setSpeed(rate: number): void {
    const next = Number.isFinite(rate) ? Math.min(2, Math.max(0.5, rate)) : 1;
    this.audio.playbackRate = next;
  }

  async pause(): Promise<void> {
    this.audio.pause();
    this.playbackState = "paused";
    this.persist(true);
  }

  async seekTo(positionMs: number): Promise<void> {
    if (!Number.isFinite(positionMs)) return;
    const seconds = positionMs / 1000;
    const duration = this.audio.duration;
    this.audio.currentTime =
      Number.isFinite(duration) && duration > 0
        ? Math.min(Math.max(0, seconds), duration)
        : Math.max(0, seconds);
    this.persist(true);
  }

  async play(episodeId: string): Promise<void> {
    const episode = this.resolveEpisode(episodeId);
    if (!episode?.enclosureUrl) {
      throw new PlayerError("enclosure", messageFor("enclosure"));
    }
    try {
      if (this.activeId !== episodeId || !this.audio.src) {
        this.activeId = episodeId;
        this.audio.src = episode.enclosureUrl;
        this.restorePosition(episodeId);
      }
      await this.audio.play();
      this.playbackState = "playing";
    } catch (err) {
      this.playbackState = "paused";
      const kind = classifyAudioError(this.audio, err);
      throw new PlayerError(kind, messageFor(kind));
    }
  }

  private restorePosition(episodeId: string): void {
    const saved = getPosition(episodeId);
    if (!saved || saved.positionMs <= 0) return;
    const apply = (): void => {
      this.audio.currentTime = saved.positionMs / 1000;
      this.audio.removeEventListener("loadedmetadata", apply);
    };
    if (this.audio.readyState >= 1) apply();
    else this.audio.addEventListener("loadedmetadata", apply);
  }

  private persist(force: boolean): void {
    if (!this.activeId) return;
    const now = Date.now();
    if (!force && now - this.lastSave < 2000) return;
    this.lastSave = now;
    const durationMs = Number.isFinite(this.audio.duration)
      ? Math.floor(this.audio.duration * 1000)
      : undefined;
    savePosition({
      episodeId: this.activeId,
      positionMs: Math.floor(this.audio.currentTime * 1000),
      durationMs,
      updatedAt: now,
    });
  }
}
