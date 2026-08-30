import { VOICE_GAIN } from "./skip";

export function attachVoiceGain(audio: HTMLAudioElement, gain = VOICE_GAIN): () => void {
  const Ctor =
    window.AudioContext ||
    (window as Window & { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
  if (!Ctor) return () => undefined;
  const ctx = new Ctor();
  const source = ctx.createMediaElementSource(audio);
  const node = ctx.createGain();
  node.gain.value = gain;
  source.connect(node).connect(ctx.destination);
  const resume = (): void => {
    void ctx.resume();
  };
  audio.addEventListener("play", resume);
  return () => {
    audio.removeEventListener("play", resume);
    source.disconnect();
    void ctx.close();
  };
}
