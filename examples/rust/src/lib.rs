//! Argon2id + XChaCha20-Poly1305 helpers for AetherFeed sync envelopes.

#![deny(unsafe_code)]

use argon2::{Algorithm, Argon2, Params, Version};
use base64::{engine::general_purpose::STANDARD as B64, Engine as _};
use chacha20poly1305::aead::{Aead, KeyInit};
use chacha20poly1305::{Key, XChaCha20Poly1305, XNonce};
use hmac::{Hmac, Mac};
use rand::Rng;
use serde::{Deserialize, Serialize};
use sha2::Sha256;

const SALT_LEN: usize = 16;
const NONCE_LEN: usize = 24;
const KEY_LEN: usize = 32;

type HmacSha256 = Hmac<Sha256>;

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, Eq)]
pub struct SyncEnvelopeJson {
    pub version: u32,
    pub kdf: String,
    pub aead: String,
    #[serde(rename = "saltB64")]
    pub salt_b64: String,
    #[serde(rename = "nonceB64")]
    pub nonce_b64: String,
    #[serde(rename = "ciphertextB64")]
    pub ciphertext_b64: String,
}

pub fn random_salt() -> [u8; SALT_LEN] {
    let mut salt = [0u8; SALT_LEN];
    rand::rng().fill_bytes(&mut salt);
    salt
}

pub fn derive_key(passphrase: &[u8], salt: &[u8]) -> Result<[u8; KEY_LEN], String> {
    if passphrase.is_empty() {
        return Err("passphrase must not be empty".into());
    }
    let params = Params::new(19_456, 2, 1, Some(KEY_LEN)).map_err(|err| err.to_string())?;
    let argon = Argon2::new(Algorithm::Argon2id, Version::V0x13, params);
    let mut key = [0u8; KEY_LEN];
    argon
        .hash_password_into(passphrase, salt, &mut key)
        .map_err(|err| err.to_string())?;
    Ok(key)
}

pub fn seal(key: &[u8; KEY_LEN], plaintext: &[u8]) -> Result<Vec<u8>, String> {
    let key = Key::try_from(key.as_slice()).map_err(|_| "bad key".to_string())?;
    let cipher = XChaCha20Poly1305::new(&key);
    let mut nonce = [0u8; NONCE_LEN];
    rand::rng().fill_bytes(&mut nonce);
    let nonce_arr = XNonce::try_from(nonce.as_slice()).map_err(|_| "bad nonce".to_string())?;
    let ciphertext = cipher
        .encrypt(&nonce_arr, plaintext)
        .map_err(|_| "encrypt failed".to_string())?;
    let mut out = Vec::with_capacity(NONCE_LEN + ciphertext.len());
    out.extend_from_slice(&nonce);
    out.extend_from_slice(&ciphertext);
    Ok(out)
}

pub fn open(key: &[u8; KEY_LEN], blob: &[u8]) -> Result<Vec<u8>, String> {
    if blob.len() <= NONCE_LEN {
        return Err("blob too short".into());
    }
    let (nonce, ciphertext) = blob.split_at(NONCE_LEN);
    let key = Key::try_from(key.as_slice()).map_err(|_| "bad key".to_string())?;
    let cipher = XChaCha20Poly1305::new(&key);
    let nonce_arr = XNonce::try_from(nonce).map_err(|_| "bad nonce".to_string())?;
    cipher
        .decrypt(&nonce_arr, ciphertext)
        .map_err(|_| "decrypt failed".to_string())
}

pub fn seal_envelope(passphrase: &str, plaintext: &[u8]) -> Result<String, String> {
    let salt = random_salt();
    let key = derive_key(passphrase.as_bytes(), &salt)?;
    let sealed = seal(&key, plaintext)?;
    let (nonce, ciphertext) = sealed.split_at(NONCE_LEN);
    let env = SyncEnvelopeJson {
        version: 1,
        kdf: "argon2id".into(),
        aead: "xchacha20poly1305".into(),
        salt_b64: B64.encode(salt),
        nonce_b64: B64.encode(nonce),
        ciphertext_b64: B64.encode(ciphertext),
    };
    serde_json::to_string(&env).map_err(|e| e.to_string())
}

pub fn open_envelope(passphrase: &str, envelope_json: &str) -> Result<Vec<u8>, String> {
    let env: SyncEnvelopeJson = serde_json::from_str(envelope_json).map_err(|e| e.to_string())?;
    if env.version != 1 || env.kdf != "argon2id" || env.aead != "xchacha20poly1305" {
        return Err("unsupported envelope".into());
    }
    let salt = B64
        .decode(env.salt_b64.as_bytes())
        .map_err(|e| e.to_string())?;
    let nonce = B64
        .decode(env.nonce_b64.as_bytes())
        .map_err(|e| e.to_string())?;
    let ciphertext = B64
        .decode(env.ciphertext_b64.as_bytes())
        .map_err(|e| e.to_string())?;
    let key = derive_key(passphrase.as_bytes(), &salt)?;
    let mut blob = Vec::with_capacity(nonce.len() + ciphertext.len());
    blob.extend_from_slice(&nonce);
    blob.extend_from_slice(&ciphertext);
    open(&key, &blob)
}

/// Bearer token for LAN PUT: HMAC-SHA256(key, "PUT\\n{etag}") as standard base64.
pub fn put_bearer(passphrase: &str, salt_b64: &str, etag: &str) -> Result<String, String> {
    let salt = B64.decode(salt_b64.as_bytes()).map_err(|e| e.to_string())?;
    let key = derive_key(passphrase.as_bytes(), &salt)?;
    let mut mac = HmacSha256::new_from_slice(&key).map_err(|e| e.to_string())?;
    mac.update(format!("PUT\n{etag}").as_bytes());
    Ok(B64.encode(mac.finalize().into_bytes()))
}

pub fn verify_put_bearer(
    passphrase: &str,
    salt_b64: &str,
    etag: &str,
    bearer: &str,
) -> Result<bool, String> {
    let expected = put_bearer(passphrase, salt_b64, etag)?;
    Ok(expected == bearer)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn round_trips_envelope() {
        let salt = random_salt();
        let key = derive_key(b"test-passphrase", &salt).expect("derive");
        let blob = seal(&key, b"opaque-sync").expect("seal");
        assert_eq!(open(&key, &blob).expect("open"), b"opaque-sync");
        let json = seal_envelope("test-passphrase", b"pool-v2").expect("seal env");
        assert_eq!(
            open_envelope("test-passphrase", &json).expect("open env"),
            b"pool-v2"
        );
    }

    #[test]
    fn rejects_empty_passphrase() {
        let salt = random_salt();
        assert!(derive_key(b"", &salt).is_err());
    }

    #[test]
    fn put_bearer_stable() {
        let salt = B64.encode([1u8; 16]);
        let a = put_bearer("secret", &salt, "etag-1").unwrap();
        let b = put_bearer("secret", &salt, "etag-1").unwrap();
        assert_eq!(a, b);
        assert!(verify_put_bearer("secret", &salt, "etag-1", &a).unwrap());
        assert!(!verify_put_bearer("secret", &salt, "etag-2", &a).unwrap());
    }
}
