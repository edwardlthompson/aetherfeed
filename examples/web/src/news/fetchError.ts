export type NewsFetchError = "aborted" | "timeout" | "network" | "parse" | "missing" | "gone";

export function classifyFetchError(err: unknown, timedOut: boolean): NewsFetchError {
  if (timedOut) return "timeout";
  const name = err instanceof Error || err instanceof DOMException ? err.name : "";
  if (name === "AbortError") return "aborted";
  return classifyHttpMessage(String(err)) ?? "network";
}

export function classifyHttpStatus(status: number): NewsFetchError {
  return status === 404 || status === 410 ? "gone" : "network";
}

export function classifyHttpMessage(message: string): NewsFetchError | null {
  const match = /HTTP (\d{3})/.exec(message);
  return match ? classifyHttpStatus(Number(match[1])) : null;
}
