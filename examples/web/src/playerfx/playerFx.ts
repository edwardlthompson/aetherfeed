import { parseChapters } from "./chapters";
import { playerFxCopy as copy } from "./copy";
import "./playerfx.css";
import { SleepTimer } from "./sleepTimer";

export type PlayerFxOptions = {
  chapters?: unknown;
  onSleepDone?: () => void;
  presetsMs?: readonly number[];
};

const PRESETS = [15 * 60_000, 30 * 60_000, 45 * 60_000, 60 * 60_000] as const;

function esc(value: string): string {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");
}

function clock(ms: number): string {
  const total = Math.max(0, Math.floor(ms / 1000));
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const seconds = String(total % 60).padStart(2, "0");
  if (hours > 0) return `${hours}:${String(minutes).padStart(2, "0")}:${seconds}`;
  return `${minutes}:${seconds}`;
}

export function createPlayerFx(root: HTMLElement, options?: PlayerFxOptions): HTMLElement {
  const pane = document.createElement("section");
  pane.className = "af-playerfx";
  pane.dataset.testid = "playerfx";
  pane.setAttribute("aria-label", copy.title);
  const chapters = parseChapters(options?.chapters);
  const presets = options?.presetsMs ?? PRESETS;
  const presetBtns = presets
    .map((ms) => {
      const mins = Math.round(ms / 60_000);
      return `<button type="button" data-sleep-ms="${ms}">${mins} ${esc(copy.minutes)}</button>`;
    })
    .join("");
  const chapterItems = chapters
    .map(
      (chapter) =>
        `<li><button type="button" data-chapter data-start-ms="${chapter.startMs}">${esc(chapter.title)} (${clock(chapter.startMs)})</button></li>`,
    )
    .join("");
  const chapterBody = chapterItems
    ? `<ol class="af-playerfx-chapters" data-chapter-list>${chapterItems}</ol>`
    : `<p class="af-playerfx-empty" data-chapters-empty>${esc(copy.chaptersEmpty)}</p>`;
  pane.innerHTML = `
    <div class="af-playerfx-sleep" data-sleep-timer>
      <h2>${esc(copy.sleep)}</h2>
      <div class="af-playerfx-presets">${presetBtns}
        <button type="button" data-sleep-off>${esc(copy.off)}</button></div>
      <p data-sleep-remaining>${esc(copy.remaining)}: 0:00</p>
    </div>
    <div class="af-playerfx-chapter-block">
      <h2>${esc(copy.chapters)}</h2>
      ${chapterBody}
    </div>
  `;
  const remainingEl = pane.querySelector<HTMLElement>("[data-sleep-remaining]");
  const timer = new SleepTimer({
    onDone: () => {
      paintRemaining();
      try {
        options?.onSleepDone?.();
      } catch {
        // View already reset remaining; ignore listener failures.
      }
    },
  });
  const paintRemaining = (): void => {
    if (remainingEl) remainingEl.textContent = `${copy.remaining}: ${clock(timer.remainingMs())}`;
  };
  pane.querySelectorAll<HTMLButtonElement>("[data-sleep-ms]").forEach((button) => {
    button.addEventListener("click", () => {
      timer.start(Number(button.dataset.sleepMs));
      paintRemaining();
    });
  });
  pane.querySelector("[data-sleep-off]")?.addEventListener("click", () => {
    timer.stop();
    paintRemaining();
  });
  root.replaceChildren(pane);
  return pane;
}
