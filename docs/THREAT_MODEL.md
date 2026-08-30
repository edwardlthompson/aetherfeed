# Threat Model

## Scope

| Item | Value |
|------|-------|
| Project | AetherFeed |
| Stack | Android + Windows desktop (Tauri), shared TypeScript/Kotlin models |
| Methodology | STRIDE + MASVS for Android + desktop local-store threats |
## Trust Boundaries

```text
[User] --> [AetherFeed client]
              |-- SQLCipher vault (device)
              |-- OS keystore (credentials / sync key)
              |-- App-private downloads
              |-- Optional SyncProvider
                    |-- Google Drive appdata (ciphertext only)
                    |-- WebDAV (ciphertext only)

```

The provider is untrusted for confidentiality. The local OS is trusted for
keystore availability but not for an unlocked backup of the passphrase.

## STRIDE Summary

| Threat | Example | Mitigation | Owner |
|--------|---------|------------|-------|
| Spoofing | Fake Drive app | Restricted `drive.appdata` scope, user-visible provider | AGENT |
| Tampering | Edited `.enc` blob | Authenticated encryption | AGENT |
| Repudiation | Lost passphrase | Honest recovery warning; no silent reset | AGENT |
| Information disclosure | World-readable downloads or recents screenshot | App-private storage; SQLCipher; Android `FLAG_SECURE` | AGENT |
| Denial of service | Huge feed | Input limits on parse/download | AGENT |
| Elevation of privilege | Full Drive access | Never request non-appdata scopes | AGENT |
## Top Abuse Cases

1. Cloud host reads plaintext library state — blocked by envelope encryption
2. Stolen device yields an unlocked vault — PIN or passphrase wrap; 2-minute lock timeout
3. Malicious feed XML / booru payload — parse in isolation, size limits
4. Token theft from disk — keystore + SQLCipher
5. Forgotten passphrase — backup is useless; document this in UI and README
