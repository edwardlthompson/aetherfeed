import { readFileSync, writeFileSync, existsSync } from "node:fs";
import path from "node:path";
import { defineConfig, type Plugin } from "vite";

const pkg = JSON.parse(
  readFileSync(new URL("./package.json", import.meta.url), "utf-8"),
) as { version: string };

function stampServiceWorkerCache(version: string): Plugin {
  return {
    name: "stamp-sw-cache",
    closeBundle() {
      const distSw = path.resolve("dist/sw.js");
      if (!existsSync(distSw)) return;
      const content = readFileSync(distSw, "utf-8");
      writeFileSync(distSw, content.replaceAll("__APP_VERSION__", version));
    },
  };
}

/** Tauri/WebView2 custom protocols reject CORS-mode module loads (`crossorigin`). */
function stripCrossOriginForTauri(): Plugin {
  return {
    name: "strip-crossorigin-for-tauri",
    transformIndexHtml(html) {
      if (!(process.env.TAURI_ENV_PLATFORM || process.env.TAURI_PLATFORM)) {
        return html;
      }
      return html.replace(/\s+crossorigin(?:="[^"]*")?/g, "");
    },
  };
}

export default defineConfig({
  define: {
    __APP_VERSION__: JSON.stringify(pkg.version),
  },
  resolve: {
    alias: {
      "@aetherfeed/domain": path.resolve(__dirname, "../../shared/typescript"),
    },
  },
  plugins: [stampServiceWorkerCache(pkg.version), stripCrossOriginForTauri()],
  // Relative base: required for Tauri custom-protocol loads. GitHub Pages can
  // still override with VITE_BASE_PATH (e.g. "/aetherfeed/").
  base: process.env.VITE_BASE_PATH || "./",
  build: {
    // WebView2 on Windows needs a modern but not bleeding-edge target.
    target: ["es2021", "chrome105"],
    rollupOptions: {
      input: {
        main: "index.html",
      },
    },
  },
});
