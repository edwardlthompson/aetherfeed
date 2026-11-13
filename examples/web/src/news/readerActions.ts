import { t } from "../i18n";
import type { ReaderMode } from "../settings/readerPrefs";
import { READER_ICONS } from "./readerIcons";

export type ReaderActionState = {
  starred: boolean;
  read: boolean;
  readerMode: ReaderMode;
  onToggleStar: () => void;
  onParkUnread: () => void;
  onOpenBrowser: () => void;
  onReaderMode: (mode: ReaderMode) => void;
};

function iconButton(
  testId: string,
  label: string,
  icon: string,
  onClick: () => void,
  opts: { pressed?: boolean; disabled?: boolean; mode?: boolean } = {},
): HTMLButtonElement {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = opts.mode ? "af-reader-icon af-reader-mode" : "af-reader-icon af-reader-action";
  btn.dataset.testid = testId;
  btn.setAttribute("aria-label", label);
  btn.title = label;
  if (opts.pressed != null) btn.setAttribute("aria-pressed", opts.pressed ? "true" : "false");
  btn.disabled = Boolean(opts.disabled);
  btn.innerHTML = icon;
  btn.addEventListener("click", onClick);
  return btn;
}

export function createReaderActions(state: ReaderActionState): HTMLElement {
  const bar = document.createElement("div");
  bar.className = "af-reader-controls";
  bar.dataset.testid = "reader-actions";

  bar.append(
    iconButton(
      "article-star",
      state.starred ? t("feed.reader.unstar") : t("feed.reader.star"),
      state.starred ? READER_ICONS.star : READER_ICONS.starOutline,
      () => state.onToggleStar(),
      { pressed: state.starred },
    ),
    iconButton(
      "article-park-unread",
      t("feed.reader.park_unread"),
      READER_ICONS.parkUnread,
      () => state.onParkUnread(),
      { disabled: !state.read },
    ),
    iconButton("article-open-browser", t("feed.reader.browser"), READER_ICONS.browser, () =>
      state.onOpenBrowser(),
    ),
  );

  const modes: { mode: ReaderMode; testId: string; key: string; icon: string }[] = [
    {
      mode: "READING",
      testId: "reader-mode-reading",
      key: "feed.reader.mode.reading",
      icon: READER_ICONS.reading,
    },
    {
      mode: "FULL",
      testId: "reader-mode-full",
      key: "feed.reader.mode.full",
      icon: READER_ICONS.full,
    },
    { mode: "WEB", testId: "reader-mode-web", key: "feed.reader.mode.web", icon: READER_ICONS.web },
  ];
  for (const entry of modes) {
    bar.append(
      iconButton(entry.testId, t(entry.key), entry.icon, () => state.onReaderMode(entry.mode), {
        pressed: state.readerMode === entry.mode,
        mode: true,
      }),
    );
  }
  return bar;
}
