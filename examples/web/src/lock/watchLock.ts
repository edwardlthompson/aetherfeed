import { APP_LOCK_TIMEOUT_MS } from "@aetherfeed/domain";
import { touchUnlock, webAppLock } from "./session";
import { listenWindowFocus } from "./windowFocus";

function remountIfLocked(onLock: () => void): void {
  if (webAppLock.state() === "unlocked") return;
  if (document.querySelector("[data-lock-form]")) return;
  onLock();
}

export function watchBackgroundLock(onLock: () => void): () => void {
  let awayAt = 0;
  let stopFocus = (): void => undefined;
  const considerAway = (): void => {
    if (!awayAt || Date.now() - awayAt < APP_LOCK_TIMEOUT_MS) return;
    if (webAppLock.state() === "unlocked") webAppLock.lock();
    awayAt = 0;
    remountIfLocked(onLock);
  };
  const onVis = (): void => {
    if (document.hidden) {
      awayAt = Date.now();
      return;
    }
    considerAway();
    remountIfLocked(onLock);
    awayAt = 0;
  };
  const onBlur = (): void => {
    awayAt = Date.now();
  };
  const onFocus = (): void => {
    considerAway();
    remountIfLocked(onLock);
    awayAt = 0;
  };
  const onInput = (): void => {
    remountIfLocked(onLock);
    touchUnlock();
  };
  const tick = (): void => {
    considerAway();
    remountIfLocked(onLock);
  };
  document.addEventListener("visibilitychange", onVis);
  window.addEventListener("blur", onBlur);
  window.addEventListener("focus", onFocus);
  document.addEventListener("pointerdown", onInput, true);
  document.addEventListener("keydown", onInput, true);
  const timer = window.setInterval(tick, 1_000);
  void listenWindowFocus((focused) => {
    if (focused) onFocus();
    else onBlur();
  }).then((stop) => {
    stopFocus = stop;
  });
  return () => {
    document.removeEventListener("visibilitychange", onVis);
    window.removeEventListener("blur", onBlur);
    window.removeEventListener("focus", onFocus);
    document.removeEventListener("pointerdown", onInput, true);
    document.removeEventListener("keydown", onInput, true);
    window.clearInterval(timer);
    stopFocus();
  };
}
