export const SILENCE_RMS = 0.02;
export const VOICE_GAIN = 1.4;

export function rms(samples: ArrayLike<number>): number {
  if (!samples.length) return 0;
  let sum = 0;
  for (let i = 0; i < samples.length; i += 1) {
    const n = samples[i] ?? 0;
    sum += n * n;
  }
  return Math.sqrt(sum / samples.length);
}

export function skipSilentPrefix(samples: Float32Array, threshold = SILENCE_RMS): number {
  const window = 128;
  for (let i = 0; i < samples.length; i += window) {
    if (rms(samples.subarray(i, i + window)) > threshold) return i;
  }
  return samples.length;
}

export function boostVoice(samples: Float32Array, gain = VOICE_GAIN): Float32Array {
  const out = new Float32Array(samples.length);
  for (let i = 0; i < samples.length; i += 1) {
    out[i] = Math.max(-1, Math.min(1, (samples[i] ?? 0) * gain));
  }
  return out;
}
