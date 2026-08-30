/// <reference types="vite/client" />

declare const __APP_VERSION__: string;

interface ImportMetaEnv {
  readonly VITE_DRIVE_CLIENT_ID?: string;
  readonly VITE_DRIVE_CLIENT_SECRET?: string;
  readonly VITE_DRIVE_PASSPHRASE?: string;
  readonly VITE_SEED_OPML_PATH?: string;
}
