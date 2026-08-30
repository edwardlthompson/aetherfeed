import { describe, expect, it } from "vitest";
import {
  fallbackCommand,
  fossTranscribeAvailable,
  TRANSCRIPT_FALLBACK,
  transcribeLocal,
} from "./fossPath";

describe("on-device transcripts", () => {
  it("documents the FOSS fallback and does not upload audio", () => {
    expect(fossTranscribeAvailable()).toBe(false);
    expect(transcribeLocal(new ArrayBuffer(0))).toBeNull();
    expect(fallbackCommand()).toBe(TRANSCRIPT_FALLBACK);
  });
});
