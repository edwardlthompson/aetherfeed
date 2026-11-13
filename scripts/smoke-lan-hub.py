#!/usr/bin/env python3
"""Minimal home-LAN pool hub for smoke testing Android clients.

Protocol matches desktop lan_hub.rs: GET /health, /v1/meta, GET/PUT /v1/pool
with SyncEnvelope (argon2id + xchacha20poly1305) and HMAC PUT bearer.
"""

from __future__ import annotations

import argparse
import base64
import hashlib
import hmac
import json
import secrets
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

from argon2.low_level import Type, hash_secret_raw
from nacl.bindings import (
    crypto_aead_xchacha20poly1305_ietf_NPUBBYTES,
    crypto_aead_xchacha20poly1305_ietf_decrypt,
    crypto_aead_xchacha20poly1305_ietf_encrypt,
)


EMPTY_DOC = {
    "version": 2,
    "updatedAt": 0,
    "subs": [],
    "reads": [],
    "stars": [],
    "playback": [],
}


def derive_key(passphrase: str, salt: bytes) -> bytes:
    return hash_secret_raw(
        secret=passphrase.encode("utf-8"),
        salt=salt,
        time_cost=2,
        memory_cost=19_456,
        parallelism=1,
        hash_len=32,
        type=Type.ID,
        version=0x13,
    )


def seal_envelope(passphrase: str, plaintext: bytes) -> str:
    salt = secrets.token_bytes(16)
    key = derive_key(passphrase, salt)
    nonce = secrets.token_bytes(crypto_aead_xchacha20poly1305_ietf_NPUBBYTES)
    ciphertext = crypto_aead_xchacha20poly1305_ietf_encrypt(plaintext, b"", nonce, key)
    return json.dumps(
        {
            "version": 1,
            "kdf": "argon2id",
            "aead": "xchacha20poly1305",
            "saltB64": base64.b64encode(salt).decode(),
            "nonceB64": base64.b64encode(nonce).decode(),
            "ciphertextB64": base64.b64encode(ciphertext).decode(),
        },
        separators=(",", ":"),
    )


def open_envelope(passphrase: str, envelope_json: str) -> bytes:
    env = json.loads(envelope_json)
    salt = base64.b64decode(env["saltB64"])
    nonce = base64.b64decode(env["nonceB64"])
    ciphertext = base64.b64decode(env["ciphertextB64"])
    key = derive_key(passphrase, salt)
    return crypto_aead_xchacha20poly1305_ietf_decrypt(ciphertext, b"", nonce, key)


def put_bearer(passphrase: str, auth_salt_b64: str, etag: str) -> str:
    salt = base64.b64decode(auth_salt_b64)
    key = derive_key(passphrase, salt)
    digest = hmac.new(key, f"PUT\n{etag}".encode(), hashlib.sha256).digest()
    return base64.b64encode(digest).decode()


def is_private(host: str) -> bool:
    ip = host.split("%")[0]
    if ip in {"127.0.0.1", "::1", "localhost"}:
        return True
    parts = ip.split(".")
    if len(parts) == 4 and all(p.isdigit() for p in parts):
        a, b = int(parts[0]), int(parts[1])
        return a == 10 or a == 127 or (a == 172 and 16 <= b <= 31) or (a == 192 and b == 168)
    return ip.startswith("fe80:") or ip.startswith("fc") or ip.startswith("fd")


class HubState:
    def __init__(self, passphrase: str) -> None:
        self.lock = threading.Lock()
        self.passphrase = passphrase
        self.auth_salt_b64 = base64.b64encode(secrets.token_bytes(16)).decode()
        self.etag = "1"
        self.blob = seal_envelope(passphrase, json.dumps(EMPTY_DOC).encode())


def make_handler(state: HubState):
    class Handler(BaseHTTPRequestHandler):
        def log_message(self, fmt: str, *args) -> None:  # noqa: A003
            print(f"[hub] {self.address_string()} {fmt % args}")

        def _cors(self) -> None:
            # Browser clients on phone/tablet/laptop share one LAN hub.
            self.send_header("Access-Control-Allow-Origin", "*")
            self.send_header("Access-Control-Allow-Methods", "GET, PUT, OPTIONS")
            self.send_header(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization, If-Match, X-Auth-Salt-B64",
            )
            self.send_header("Access-Control-Expose-Headers", "etag, ETag, X-Auth-Salt-B64")

        def _forbid_public(self) -> bool:
            peer = self.client_address[0]
            if not is_private(peer):
                self.send_response(403)
                self._cors()
                self.end_headers()
                self.wfile.write(b"forbidden: non-private peer")
                return True
            return False

        def do_OPTIONS(self) -> None:  # noqa: N802
            if self._forbid_public():
                return
            self.send_response(204)
            self._cors()
            self.end_headers()

        def do_GET(self) -> None:  # noqa: N802
            if self._forbid_public():
                return
            if self.path == "/health":
                self.send_response(200)
                self._cors()
                self.end_headers()
                self.wfile.write(b"ok")
                return
            with state.lock:
                etag = state.etag
                salt = state.auth_salt_b64
                blob = state.blob
            if self.path == "/v1/meta":
                body = json.dumps(
                    {"etag": etag, "authSaltB64": salt, "port": self.server.server_port}
                ).encode()
                self.send_response(200)
                self._cors()
                self.send_header("Content-Type", "application/json")
                self.send_header("Content-Length", str(len(body)))
                self.end_headers()
                self.wfile.write(body)
                return
            if self.path == "/v1/pool":
                self.send_response(200 if blob else 404)
                self._cors()
                self.send_header("etag", etag)
                self.send_header("X-Auth-Salt-B64", salt)
                if blob:
                    data = blob.encode()
                    self.send_header("Content-Type", "application/json")
                    self.send_header("Content-Length", str(len(data)))
                    self.end_headers()
                    self.wfile.write(data)
                else:
                    self.end_headers()
                return
            self.send_response(404)
            self._cors()
            self.end_headers()

        def do_PUT(self) -> None:  # noqa: N802
            if self._forbid_public():
                return
            if self.path != "/v1/pool":
                self.send_response(404)
                self._cors()
                self.end_headers()
                return
            length = int(self.headers.get("Content-Length", "0"))
            body = self.rfile.read(length).decode("utf-8") if length else ""
            if_match = self.headers.get("If-Match", "")
            auth = self.headers.get("Authorization", "")
            bearer = auth[7:] if auth.startswith("Bearer ") else ""
            with state.lock:
                if if_match != state.etag:
                    self.send_response(412)
                    self._cors()
                    self.send_header("etag", state.etag)
                    self.end_headers()
                    self.wfile.write(b"precondition failed")
                    return
                expected = put_bearer(state.passphrase, state.auth_salt_b64, state.etag)
                if not hmac.compare_digest(expected, bearer):
                    self.send_response(401)
                    self._cors()
                    self.end_headers()
                    self.wfile.write(b"unauthorized")
                    return
                if not body:
                    self.send_response(400)
                    self._cors()
                    self.end_headers()
                    self.wfile.write(b"empty body")
                    return
                # Verify ciphertext opens with passphrase before storing.
                open_envelope(state.passphrase, body)
                state.blob = body
                state.etag = str(int(state.etag) + 1)
                next_etag = state.etag
            self.send_response(200)
            self._cors()
            self.send_header("etag", next_etag)
            self.end_headers()
            self.wfile.write(next_etag.encode())

    return Handler


def self_check(passphrase: str) -> None:
    sealed = seal_envelope(passphrase, b'{"version":2,"subs":[]}')
    plain = open_envelope(passphrase, sealed)
    assert b"version" in plain
    salt = base64.b64encode(b"\x01" * 16).decode()
    a = put_bearer(passphrase, salt, "1")
    b = put_bearer(passphrase, salt, "1")
    assert a == b
    print("self-check ok: seal/open + bearer stable")


def client_roundtrip(base: str, passphrase: str) -> None:
    import urllib.error
    import urllib.request

    def get(path: str):
        req = urllib.request.Request(base + path, method="GET")
        with urllib.request.urlopen(req, timeout=5) as resp:
            headers = {k.lower(): v for k, v in resp.headers.items()}
            return resp.status, headers, resp.read().decode()

    status, _, health = get("/health")
    assert status == 200 and health == "ok", health
    status, headers, body = get("/v1/pool")
    assert status == 200, body
    etag = headers.get("etag", "0")
    salt = headers.get("x-auth-salt-b64") or json.loads(get("/v1/meta")[2])["authSaltB64"]
    plain = json.loads(open_envelope(passphrase, body))
    assert plain["version"] == 2
    plain["subs"] = [
        {
            "url": "https://smoke.example/rss",
            "title": "Smoke",
            "kind": "news",
            "tag": "",
            "updatedAt": 99,
        }
    ]
    plain["updatedAt"] = 99
    envelope = seal_envelope(passphrase, json.dumps(plain).encode())
    bearer = put_bearer(passphrase, salt, etag)
    req = urllib.request.Request(
        base + "/v1/pool",
        data=envelope.encode(),
        method="PUT",
        headers={
            "Content-Type": "application/json",
            "If-Match": etag,
            "Authorization": f"Bearer {bearer}",
        },
    )
    with urllib.request.urlopen(req, timeout=5) as resp:
        assert resp.status == 200
    _, _, body2 = get("/v1/pool")
    plain2 = json.loads(open_envelope(passphrase, body2))
    assert plain2["subs"][0]["url"] == "https://smoke.example/rss"
    print("client-roundtrip ok: get/put/decrypt")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--passphrase", default="smoke-lan-pass")
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=8787)
    parser.add_argument("--self-check", action="store_true")
    parser.add_argument("--client-smoke", action="store_true")
    args = parser.parse_args()
    if args.self_check:
        self_check(args.passphrase)
        return
    state = HubState(args.passphrase)
    server = ThreadingHTTPServer((args.host, args.port), make_handler(state))
    if args.client_smoke:
        thread = threading.Thread(target=server.serve_forever, daemon=True)
        thread.start()
        try:
            client_roundtrip(f"http://127.0.0.1:{args.port}", args.passphrase)
        finally:
            server.shutdown()
        return
    print(f"LAN hub smoke listening on http://{args.host}:{args.port}")
    print(f"passphrase={args.passphrase!r} authSaltB64={state.auth_salt_b64} etag={state.etag}")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("stopped")


if __name__ == "__main__":
    main()
