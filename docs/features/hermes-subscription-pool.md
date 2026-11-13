# Feature: Hermes subscription pool

> Status: ✅ implemented — pull/push news+podcast subscriptions via Hermes ContentProvider

## Acceptance criteria

- ✅ AetherFeed declares `uses-permission` for Hermes `SUBSCRIPTION_POOL` signature permission
- ✅ `SubscriptionPoolClient` reads/writes Hermes authority; graceful skip if Hermes missing or denied
- ✅ News/podcast/booru feed mutations publish pool document; launch/resume pulls and merges (LWW)
- ✅ Booru syncs via the pool for AetherFeed; Hermes stores it but does not show it in the launcher
- ✅ Settings shows sync status string
- ✅ Codec unit tests cover round-trip, booru kind, LWW ties

## Contract (v2; v1 upgrades on decode)

```json
{
  "version": 2,
  "updatedAt": 0,
  "subs": [{ "url": "https://example.test/rss", "title": "Example", "kind": "news", "tag": "", "updatedAt": 0 }],
  "reads": [{ "url": "https://example.test/a", "read": true, "updatedAt": 0 }],
  "stars": [{ "url": "https://example.test/a", "starred": true, "updatedAt": 0 }],
  "playback": [{ "url": "https://example.test/ep.mp3", "positionMs": 0, "updatedAt": 0 }]
}
```

- `kind`: `"news"` | `"podcast"` | `"booru"`
- Merge key: normalized URL (trim, strip trailing `/`); LWW by `updatedAt` (ties prefer read/starred/higher position)
- Hermes applies only `news`/`podcast` into `FeedStore`; `booru` rows stay in the pool blob
- Same-device: ContentProvider; multi-device home Wi-Fi: desktop LAN hub (ADR-0011 / `docs/features/sync.md`)

Keep field names identical with Hermes `SubscriptionPoolCodec` (one-line sync comment in source).

## Android paths

| Piece | Path |
|-------|------|
| Codec | `examples/android/.../sync/SubscriptionPoolCodec.kt` |
| Client | `examples/android/.../sync/SubscriptionPoolClient.kt` |
| Syncer | `examples/android/.../sync/SubscriptionPoolSync.kt` |
| Tests | `examples/android/app/src/test/.../sync/SubscriptionPoolCodecTest.kt` |

## Non-goals

Shared article/read/star/playback; cloud sync for this pool; desktop IPC; showing booru in Hermes launcher UI.

## Smoke scenario

1. Install Hermes + AetherFeed on the same device with matching signing.
2. Add a news feed in Hermes; open AetherFeed and confirm the sub appears after pool pull.
3. Star/read in AetherFeed; relaunch Hermes and confirm the pool blob merges without dropping booru rows.

## Container map

- Feature container: `examples/android/app/src/main/java/org/aetherfeed/app/sync/`
- Spec: `docs/features/hermes-subscription-pool.md`
- Related: `docs/features/sync.md`, ADR-0011

## Tests

Automated: yes

- `./gradlew :app:test --tests "org.aetherfeed.app.sync.SubscriptionPoolCodecTest"`

## Fallback validation

```bash
python3 scripts/agent-run.py feature-gate --stack android

```

