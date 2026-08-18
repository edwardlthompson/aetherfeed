# Shared domain

Canonical models for AetherFeed. Android mirrors these types in Kotlin.
The Windows desktop UI imports the TypeScript module via the web example.

## Why a schema lock

Unread, stars, tags, and playback position must mean the same thing on every
device before sync envelopes exist. Platforms implement repositories; they do
not invent parallel field names.

## Layout

| Path | Role |
|------|------|
| `typescript/` | Shared TypeScript models and sync primitives |
| `schema/` | JSON Schema used as the cross-language contract |

Do not add network or UI code here.
