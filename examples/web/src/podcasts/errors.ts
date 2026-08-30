import { podcastsCopy } from "./copy";

export type PlayerErrorKind = "network" | "enclosure";

export class PlayerError extends Error {
  readonly kind: PlayerErrorKind;
  constructor(kind: PlayerErrorKind, message: string) {
    super(message);
    this.name = "PlayerError";
    this.kind = kind;
  }
}

export function classifyAudioError(audio: HTMLAudioElement, err?: unknown): PlayerErrorKind {
  const code = audio.error?.code ?? 0;
  if (code === 1 || code === 2) return "network";
  if (code === 3 || code === 4) return "enclosure";
  if (err instanceof PlayerError) return err.kind;
  const name = err instanceof DOMException ? err.name : "";
  if (name === "NetworkError" || name === "AbortError") return "network";
  if (err instanceof TypeError) return "network";
  return "enclosure";
}

export function kindOfError(err: unknown): PlayerErrorKind {
  return err instanceof PlayerError ? err.kind : "enclosure";
}

export function messageFor(kind: PlayerErrorKind): string {
  return kind === "network" ? podcastsCopy.network : podcastsCopy.enclosure;
}
