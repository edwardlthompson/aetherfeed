import { describe, expect, it } from "vitest";
import { boostVoice, skipSilentPrefix } from "./skip";

describe("silence skip", () => {
  it("skips a silent prefix and boosts voice", () => {
    const samples = new Float32Array(512);
    for (let i = 256; i < 512; i += 1) samples[i] = 0.4;
    expect(skipSilentPrefix(samples)).toBeGreaterThan(200);
    expect(boostVoice(new Float32Array([0.5]))[0]).toBeGreaterThan(0.5);
  });
});
