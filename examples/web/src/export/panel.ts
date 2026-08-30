import type { Feed } from "@aetherfeed/domain";
import { loadImportedFeeds, saveImportedFeeds } from "../readerimport/importedStore";
import { exportCopy as copy } from "./copy";
import { downloadText } from "./download";
import { escapeXml } from "./escape";
import { applyFolderRenames, collectFolderRenames, uniqueFolders } from "./folders";
import { buildOpmlXml } from "./opmlXml";

export type ExportPanelOptions = {
  load?: () => Feed[];
  save?: (feeds: Feed[]) => void;
  now?: () => number;
  download?: (filename: string, contents: string) => void;
};

function folderRow(name: string): string {
  const label = name || copy.unfiled;
  return `<li><label>${escapeXml(copy.folder)}
    <input type="text" data-folder-rename data-folder-from="${escapeXml(name)}" value="${escapeXml(name)}" aria-label="${escapeXml(label)}" />
  </label></li>`;
}

export function createExportPanel(
  root: HTMLElement,
  options: ExportPanelOptions = {},
): HTMLElement {
  const load = options.load ?? loadImportedFeeds;
  const save = options.save ?? saveImportedFeeds;
  const now = options.now ?? Date.now;
  const download = options.download ?? downloadText;
  const panel = document.createElement("section");
  panel.className = "af-export";
  panel.dataset.testid = "export-panel";
  panel.setAttribute("aria-label", copy.title);

  const paint = (): void => {
    const feeds = load();
    const folders = uniqueFolders(feeds);
    const list = folders.length
      ? `<ul data-export-folders>${folders.map(folderRow).join("")}</ul>`
      : `<p data-export-empty>${escapeXml(copy.empty)}</p>`;
    panel.innerHTML = `
      <h2>${escapeXml(copy.title)}</h2>
      <button type="button" data-export-download>${escapeXml(copy.download)}</button>
      ${list}
      <button type="button" data-export-save>${escapeXml(copy.save)}</button>
    `;
  };

  panel.addEventListener("click", (event) => {
    const target = event.target;
    if (!(target instanceof HTMLElement)) return;
    if (target.closest("[data-export-download]")) {
      download("aetherfeed.opml", buildOpmlXml(load(), now()));
      return;
    }
    if (target.closest("[data-export-save]")) {
      save(applyFolderRenames(load(), collectFolderRenames(panel)));
      paint();
    }
  });

  paint();
  root.replaceChildren(panel);
  return panel;
}
