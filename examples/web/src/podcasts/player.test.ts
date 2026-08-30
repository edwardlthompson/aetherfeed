import type { Episode } from "@aetherfeed/domain";
import { afterEach, describe, expect, it, vi } from "vitest";
import { HtmlAudioPodcastPlayer, NoopPodcastPlayer } from "./player";
import { clearPositions, getPosition, savePosition } from "./positionStore";

const episode: Episode = {
  id: "ep-1",
  showId: "show-1",
  title: "Pilot",
  enclosureUrl: "https://example.invalid/pilot.mp3",
};

function mockAudio() {
  const listeners = new Map<string, Set<EventListener>>();
  const raw = {
    src: "",
    currentTime: 0,
    duration: 120,
    playbackRate: 1,
    paused: true,
    error: null as { code: number } | null,
    readyState: 2,
    preload: "metadata",
    play: vi.fn(async () => {
      raw.paused = false;
    }),
    pause: vi.fn(() => {
      raw.paused = true;
      const set = listeners.get("pause");
      set?.forEach((fn) => {
        fn(new Event("pause"));
      });
    }),
    addEventListener(type: string, fn: EventListener) {
      const set = listeners.get(type) ?? new Set();
      set.add(fn);
      listeners.set(type, set);
    },
    removeEventListener(type: string, fn: EventListener) {
      listeners.get(type)?.delete(fn);
    },
  };
  return raw as unknown as HTMLAudioElement & typeof raw;
}

describe("HtmlAudioPodcastPlayer", () => {
  afterEach(() => {
    clearPositions();
  });

  it("seeks and persists position for the active episode", async () => {
    const audio = mockAudio();
    const player = new HtmlAudioPodcastPlayer(audio, (id) =>
      id === episode.id ? episode : undefined,
    );
    await player.play(episode.id);
    await player.seekTo(45_000);
    expect(audio.currentTime).toBe(45);
    expect(player.position(episode.id)?.positionMs).toBe(45_000);
    expect(getPosition(episode.id)?.positionMs).toBe(45_000);
  });

  it("restores saved position when play loads an episode", async () => {
    const audio = mockAudio();
    savePosition({ episodeId: episode.id, positionMs: 30_000, updatedAt: 1 });
    const player = new HtmlAudioPodcastPlayer(audio, () => episode);
    await player.play(episode.id);
    expect(audio.currentTime).toBe(30);
    expect(audio.src).toBe(episode.enclosureUrl);
    expect(player.playbackState).toBe("playing");
  });

  it("maps media network failures to a typed PlayerError", async () => {
    const audio = mockAudio();
    audio.error = { code: 2 } as MediaError;
    audio.play = vi.fn(async () => {
      throw new TypeError("failed to fetch");
    });
    const player = new HtmlAudioPodcastPlayer(audio, () => episode);
    await expect(player.play(episode.id)).rejects.toMatchObject({
      name: "PlayerError",
      kind: "network",
    });
  });

  it("NoopPodcastPlayer stays hermetic", async () => {
    const noop = new NoopPodcastPlayer();
    await noop.play("ep-1");
    await noop.pause();
    await noop.seekTo(12);
    expect(noop.position("ep-1")).toBeNull();
  });
});
