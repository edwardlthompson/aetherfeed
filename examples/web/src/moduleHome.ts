import { type AppModuleTab, moduleHomeMarkup, moduleNavMarkup } from "./moduleHomeNav";
import { mountBooruModule } from "./mountBooruModule";
import { mountNewsModule } from "./mountNewsModule";
import { mountPodcastsModule } from "./mountPodcastsModule";

export type { AppModuleTab };
export { moduleHomeMarkup, moduleNavMarkup };

export function mountActiveModule(
  root: HTMLElement,
  activeModule: AppModuleTab,
  onModuleChange: (next: AppModuleTab) => void,
): void {
  for (const tab of root.querySelectorAll<HTMLButtonElement>("[data-module-tab]")) {
    tab.addEventListener("click", () => {
      const next = tab.dataset.moduleTab as AppModuleTab | undefined;
      if (!next || next === activeModule) return;
      onModuleChange(next);
    });
  }
  if (activeModule === "news") {
    const newsMount = root.querySelector<HTMLElement>("[data-news-mount]");
    if (newsMount) mountNewsModule(newsMount);
  }
  if (activeModule === "podcasts") {
    const podcastsMount = root.querySelector<HTMLElement>("[data-podcasts-mount]");
    if (podcastsMount) mountPodcastsModule(podcastsMount);
  }
  if (activeModule === "booru") {
    const booruMount = root.querySelector<HTMLElement>("[data-booru-mount]");
    if (booruMount) mountBooruModule(booruMount);
  }
}
