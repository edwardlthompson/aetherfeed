import { DRIVE_APPDATA_SCOPE, DRIVE_LOOPBACK_PORT } from "@aetherfeed/domain";

export type DriveTokens = {
  accessToken: string;
  refreshToken?: string;
  expiresAt: number;
};

export function b64url(bytes: Uint8Array): string {
  let bin = "";
  for (const byte of bytes) bin += String.fromCharCode(byte);
  return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

export function createPkceVerifier(): string {
  return b64url(crypto.getRandomValues(new Uint8Array(32)));
}

export async function createPkceChallenge(verifier: string): Promise<string> {
  const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier));
  return b64url(new Uint8Array(digest));
}

export function loopbackRedirect(): string {
  return `http://127.0.0.1:${DRIVE_LOOPBACK_PORT}`;
}

export async function buildAuthUrl(clientId: string, verifier: string): Promise<string> {
  const challenge = await createPkceChallenge(verifier);
  const params = new URLSearchParams({
    client_id: clientId,
    redirect_uri: loopbackRedirect(),
    response_type: "code",
    scope: DRIVE_APPDATA_SCOPE,
    code_challenge: challenge,
    code_challenge_method: "S256",
    access_type: "offline",
    prompt: "consent",
  });
  return `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
}

export function parseTokenResponse(data: Record<string, unknown>, now = Date.now()): DriveTokens {
  const access = String(data.access_token ?? "");
  if (!access) throw new Error("Drive token missing");
  return {
    accessToken: access,
    refreshToken: data.refresh_token ? String(data.refresh_token) : undefined,
    expiresAt: now + Number(data.expires_in ?? 3600) * 1000,
  };
}

export async function exchangeAuthCode(
  clientId: string,
  clientSecret: string,
  code: string,
  verifier: string,
): Promise<DriveTokens> {
  const body = new URLSearchParams({
    client_id: clientId,
    code,
    code_verifier: verifier,
    grant_type: "authorization_code",
    redirect_uri: loopbackRedirect(),
  });
  if (clientSecret) body.set("client_secret", clientSecret);
  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });
  if (!res.ok) throw new Error(`token exchange failed (${res.status})`);
  return parseTokenResponse((await res.json()) as Record<string, unknown>);
}

export async function refreshDriveToken(
  clientId: string,
  clientSecret: string,
  refreshToken: string,
): Promise<DriveTokens> {
  const body = new URLSearchParams({
    client_id: clientId,
    grant_type: "refresh_token",
    refresh_token: refreshToken,
  });
  if (clientSecret) body.set("client_secret", clientSecret);
  const res = await fetch("https://oauth2.googleapis.com/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body,
  });
  if (!res.ok) throw new Error(`token refresh failed (${res.status})`);
  const next = parseTokenResponse((await res.json()) as Record<string, unknown>);
  return { ...next, refreshToken: next.refreshToken ?? refreshToken };
}
