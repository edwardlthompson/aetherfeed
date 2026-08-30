import type { ReaderImportFile } from "@aetherfeed/domain";
import { createFeedLibrary, renderFeedLibrary } from "../components/FeedLibrary";
import { t } from "../i18n";
import { canPickDesktopFile, pickDesktopImportFile } from "./desktopPick";
import { loadImportedFeeds, saveImportedFeeds } from "./importedStore";
import { fileToReaderImport } from "./importFile";
import { applyReaderImportFile, formatImportResult } from "./importFlow";

export function importedFeedCount(): number {
  return loadImportedFeeds().length;
}

export function resetImportedFeeds(): void {
  saveImportedFeeds([]);
}

async function runImport(
  file: ReaderImportFile,
  resultEl: HTMLElement,
  library: HTMLElement,
): Promise<void> {
  resultEl.textContent = t("readerimport.working");
  await new Promise<void>((resolve) => {
    setTimeout(resolve, 0);
  });
  try {
    resultEl.textContent = formatImportResult(await applyReaderImportFile(file));
    renderFeedLibrary(library);
  } catch (err) {
    resultEl.textContent = err instanceof Error ? err.message : t("readerimport.failed");
  }
}

export function createReaderImportPanel(): HTMLElement {
  const panel = document.createElement("section");
  panel.className = "af-readerimport-panel";
  panel.setAttribute("aria-label", t("readerimport.title"));
  panel.dataset.testid = "readerimport-panel";
  const desktop = canPickDesktopFile();
  panel.innerHTML = `
    <h2>${t("readerimport.title")}</h2>
    <p>${t("readerimport.hint")}</p>
    ${
      desktop
        ? `<button type="button" data-readerimport-desktop>${t("readerimport.pick")}</button>`
        : `<label class="af-readerimport-field">
      <span>${t("readerimport.pick")}</span>
      <input type="file" accept=".opml,.xml,.json,.zip,application/xml,text/xml,application/json,application/zip" data-readerimport-file />
    </label>`
    }
    <p class="af-readerimport-result" data-readerimport-result aria-live="polite"></p>
  `;
  const resultEl = panel.querySelector<HTMLElement>("[data-readerimport-result]");
  const library = createFeedLibrary();
  panel.appendChild(library);
  panel.querySelector("[data-readerimport-desktop]")?.addEventListener("click", () => {
    if (!resultEl) return;
    void (async () => {
      resultEl.textContent = t("readerimport.working");
      const file = await pickDesktopImportFile();
      if (!file) {
        resultEl.textContent = "";
        return;
      }
      await runImport(file, resultEl, library);
    })();
  });
  panel
    .querySelector<HTMLInputElement>("[data-readerimport-file]")
    ?.addEventListener("change", (event) => {
      const input = event.currentTarget as HTMLInputElement;
      const file = input.files?.[0];
      if (!file || !resultEl) return;
      void (async () => {
        await runImport(await fileToReaderImport(file), resultEl, library);
      })();
    });
  return panel;
}
