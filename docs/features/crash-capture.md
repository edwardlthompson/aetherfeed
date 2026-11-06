# Feature: crash-capture

## Acceptance criteria

- Local crash payload never includes rejected secrets keys
- Crash inbox stub stays disabled without DPIA

## Smoke scenario

1. Trigger a sanitized crash fixture
2. Confirm rejected keys are stripped
3. Confirm inbox provider remains none

## Container map

- schemas/golden-path/crash-*
- examples/*/crash-capture or crash.rs

## Tests

- Automated: yes — stack allowlist tests and sanitize fixtures

## Fallback validation

`ash
bash scripts/check-crash-payload-allowlist.sh
bash scripts/check-sanitize-fixtures.sh
`
