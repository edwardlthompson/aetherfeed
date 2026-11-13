import { t } from "./i18n";

export type AppModuleTab = "news" | "podcasts" | "booru";

function tabButton(id: AppModuleTab, selected: AppModuleTab): string {
  return `<button type="button" role="tab" class="af-module-tab" data-module-tab="${id}" aria-selected="${selected === id}">${t(`nav.${id}`)}</button>`;
}

export function moduleNavMarkup(activeModule: AppModuleTab): string {
  return `<nav class="af-module-nav" role="tablist" aria-label="${t("app.title")}">${tabButton("news", activeModule)}${tabButton("podcasts", activeModule)}${tabButton("booru", activeModule)}</nav>`;
}

export function moduleHomeMarkup(activeModule: AppModuleTab): string {
  return `
      <div data-news-mount class="af-news-mount"${activeModule !== "news" ? " hidden" : ""}></div>
      <div data-podcasts-mount class="af-podcasts-mount"${activeModule !== "podcasts" ? " hidden" : ""}></div>
      <div data-booru-mount class="af-booru-mount"${activeModule !== "booru" ? " hidden" : ""}></div>`;
}
