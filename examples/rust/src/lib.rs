//! Argon2id + XChaCha20-Poly1305 helpers for AetherFeed sync envelopes.

#![deny(unsafe_code)]

use argon2::{Algorithm, Argon2, Params, Version};
use chacha20poly1305::aead::{Aead, KeyInit};
use chacha20poly1305::{Key, XChaCha20Poly1305, XNonce};
use rand::RngCore;

const SALT_LEN: usize = 16;
const NONCE_LEN: usize = 24;
const KEY_LEN: usize = 32;

pub fn random_salt() -> [u8; SALT_LEN] {
    let mut salt = [0u8; SALT_LEN];
    rand::thread_rng().fill_bytes(&mut salt);
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
    let cipher = XChaCha20Poly1305::new(Key::from_slice(key));
    let mut nonce = [0u8; NONCE_LEN];
    rand::thread_rng().fill_bytes(&mut nonce);
    let ciphertext = cipher
        .encrypt(XNonce::from_slice(&nonce), plaintext)
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
    let cipher = XChaCha20Poly1305::new(Key::from_slice(key));
    cipher
        .decrypt(XNonce::from_slice(nonce), ciphertext)
        .map_err(|_| "decrypt failed".to_string())
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
    }

    #[test]
    fn rejects_empty_passphrase() {
        let salt = random_salt();
        assert!(derive_key(b"", &salt).is_err());
    }
}
