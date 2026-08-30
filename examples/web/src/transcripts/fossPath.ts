/** No FOSS on-device speech stack is wired. Fallback validation only. */
export const TRANSCRIPT_FALLBACK = "python scripts/agent-run.py feature-gate --stack web";

export function fossTranscribeAvailable(): boolean {
  return false;
}

export function transcribeLocal(_audio: ArrayBuffer): string | null {
  return fossTranscribeAvailable() ? "" : null;
}

export function fallbackCommand(): string {
  return TRANSCRIPT_FALLBACK;
}
