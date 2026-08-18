# Privacy Policy

AetherFeed is local-first. No account is required. There is no analytics,
crash reporting, advertising, or other phone-home telemetry.

## Data stored on the device

| Data | Purpose | Retention |
|------|---------|-----------|
| Feed subscriptions, articles, episodes, posts | Offline use | Until the user deletes them |
| Stars, likes, tags, read state, playback positions | Library state | Until the user deletes them |
| API keys / OAuth tokens | Source access | Until the user removes the source |
| Sync passphrase-derived keys | Optional E2E sync | Until the user disables sync |

Downloaded files stay in app-private storage. The vault is encrypted with
SQLCipher. Credentials use the platform keystore when available.

## Optional E2E sync

If the user enables sync, the app uploads opaque `.enc` blobs to Google Drive
Application Data (`drive.appdata` only) or a WebDAV server the user names.
The passphrase never leaves the device. A forgotten passphrase cannot be
recovered. The host must not receive plaintext feeds, tags, stars, or settings.

## App update checks

- Release endpoint: GitHub Releases API or configured manifest URL
- Stored locally: `last_checked`, `installed_artifact_format`, `check_interval`
- No PII transmitted
- Interval can be set to `off`

## Data we do not collect

- No tracking
- No sale of personal data
- No crash phone-home
- No PII in logs

## User rights

- **Access / portability:** OPML export for feeds; JSON export for stars/tags/settings
- **Deletion:** uninstall or wipe the vault
- **Opt-out:** sync and update checks are optional

## DPIA Checklist (`[HUMAN]`)

If processing EU personal data of other people (for example a shared device),
complete a DPIA before adding any network identity feature. None is planned.
