import { describe, expect, it } from "vitest";
import { b64url, loopbackRedirect, parseTokenResponse } from "./driveAuth";

describe("drive auth helpers", () => {
  it("builds the loopback redirect and parses tokens", () => {
    expect(loopbackRedirect()).toBe("http://127.0.0.1:17890");
    const tokens = parseTokenResponse(
      { access_token: "abc", refresh_token: "r", expires_in: 10 },
      1_000,
    );
    expect(tokens.accessToken).toBe("abc");
    expect(tokens.refreshToken).toBe("r");
    expect(tokens.expiresAt).toBe(11_000);
  });

  it("encodes URL-safe base64", () => {
    expect(b64url(new Uint8Array([251, 255]))).toBe("+/8".replace("+", "-").replace("/", "_"));
  });
});
