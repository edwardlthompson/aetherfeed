<p align="center">
  <img src="../../branding/assets/logo-mark.svg" alt="AetherFeed" width="64" />
</p>

# AetherFeed web / desktop UI

FOSS apps with a clear path from idea to release — Vite + TypeScript PWA stub (offline service worker, Vitest, Playwright, Lighthouse CI). Brand kit: [`branding/BRANDING.md`](../../branding/BRANDING.md).

## Repository layout

```text
examples/web/
  src/
    locales/en.json     # user-visible strings (English default)
    i18n/index.ts       # t() helper — import strings from locales/
    style.css           # layout only — no user-facing copy
    design-tokens.css   # generated colors/spacing (run sync-design-tokens.py)
    theme.ts            # theme preference (system/light/dark)
    components/         # ThemeToggle, etc. — labels via t()
  public/               # manifest, service worker, static assets
  dist/                 # npm run build output for local desktop packaging (do not commit)

```

**`docs/` is agent documentation, not this app.** This tree is the shared UI for the **local** desktop (Tauri) client — AetherFeed does **not** publish a public Pages/PWA demo. See [`docs/WEB_PROJECT_LAYOUT.md`](../../docs/WEB_PROJECT_LAYOUT.md).

## Commands

```bash
npm ci
npm test          # Vitest unit tests
npm run build     # Production build → dist/
npm run preview   # Preview server
npx playwright test  # E2E + axe + visual snapshots
npm run lighthouse   # Lighthouse CI budgets

```

Optional task runner (not required for CI): install [just](https://github.com/casey/just), then `just test`.

## Why these tools?

Vite + TypeScript keep the desktop webview UI small and typed. Vitest covers units, Playwright covers the browser, and Lighthouse CI enforces a real performance budget so “it builds” is not the only bar.

## Offline shell (local desktop only)

- `manifest.webmanifest` for the Tauri/webview shell
- Offline-first service worker (`public/sw.js`) for local caching — not a public installable PWA host
- Responsive layout with theme-color meta

## Localization

- All UI strings in `src/locales/en.json`
- Use `t("key")` from `src/i18n/index.ts` in TypeScript and markup
- Never put copy in `style.css` or hardcode strings in `main.ts`
- Add `src/locales/{lang}.json` when shipping translations

See [`docs/DESIGN_GUIDE.md`](../../docs/DESIGN_GUIDE.md) for shared key naming with Android.

## CI integration

Runs in the root `.github/workflows/ci.yml` web matrix job. Public Pages deploy is disabled for AetherFeed (`pages_demo: false`).
