import { t } from "../i18n";
import {
  type ArticleTarget,
  type ImagePolicy,
  loadReaderSettings,
  saveReaderSettings,
} from "./readerPrefs";

export function appendReaderSettings(panel: HTMLElement): void {
  const settings = loadReaderSettings();
  const wrap = document.createElement("div");
  wrap.className = "af-settings-field";
  wrap.dataset.testid = "settings-article";
  wrap.innerHTML = `
    <strong>${t("feed.opener.title")}</strong>
    <p class="af-settings-hint">${t("feed.opener.body")}</p>
    <select data-reader-target aria-label="${t("feed.opener.title")}">
      <option value="LAUNCHER">${t("feed.opener.launcher")}</option>
      <option value="BROWSER">${t("feed.opener.browser")}</option>
      <option value="CUSTOM_TAB">${t("feed.opener.custom_tab")}</option>
    </select>
    <label>
      <span>${t("feed.reader.scale")}</span>
      <input type="range" min="0.85" max="1.6" step="0.05" data-reader-scale />
    </label>
    <strong>${t("feed.images.title")}</strong>
    <p class="af-settings-hint">${t("feed.images.body")}</p>
    <select data-reader-images aria-label="${t("feed.images.title")}">
      <option value="ALWAYS">${t("feed.images.always")}</option>
      <option value="WIFI">${t("feed.images.wifi")}</option>
      <option value="NEVER">${t("feed.images.never")}</option>
    </select>
  `;
  const target = wrap.querySelector<HTMLSelectElement>("[data-reader-target]");
  const scale = wrap.querySelector<HTMLInputElement>("[data-reader-scale]");
  const images = wrap.querySelector<HTMLSelectElement>("[data-reader-images]");
  if (target) target.value = settings.target;
  if (scale) scale.value = String(settings.bodyScale);
  if (images) images.value = settings.imagePolicy;
  target?.addEventListener("change", () => {
    saveReaderSettings({ target: target.value as ArticleTarget });
  });
  scale?.addEventListener("change", () => {
    saveReaderSettings({ bodyScale: Number.parseFloat(scale.value) });
  });
  images?.addEventListener("change", () => {
    saveReaderSettings({ imagePolicy: images.value as ImagePolicy });
  });
  panel.append(wrap);
}
