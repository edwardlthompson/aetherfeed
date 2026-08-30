import { type AppLockSecretKind, classifyLockSecret } from "@aetherfeed/domain";
import { lockCopy as copy } from "./copy";
import { storedLockKind, webAppLock } from "./session";

export function applyLockInput(input: HTMLInputElement, kind: AppLockSecretKind | null): void {
  const pin = kind !== "passphrase";
  input.inputMode = pin ? "numeric" : "text";
  if (pin) input.pattern = "[0-9]*";
  else input.removeAttribute("pattern");
  input.autocomplete = pin ? "one-time-code" : "current-password";
  input.dataset.lockKind = pin ? "pin" : "passphrase";
}

export function createUnlockPane(root: HTMLElement, onUnlocked: () => void): HTMLElement {
  const pane = document.createElement("section");
  pane.className = "af-lock";
  pane.dataset.testid = "unlock-pane";
  const unset = webAppLock.state() === "unset";
  const stored = storedLockKind();
  const showKind = unset || !stored;
  pane.innerHTML = `
    <h1>${unset ? copy.setTitle : copy.title}</h1>
    <p>${copy.pinHint}</p>
    <p>${copy.wipeWarn}</p>
    ${
      showKind
        ? `<fieldset data-lock-kind>
      <label><input type="radio" name="lock-kind" value="pin" checked /> ${copy.pin}</label>
      <label><input type="radio" name="lock-kind" value="passphrase" /> ${copy.passphrase}</label>
    </fieldset>`
        : ""
    }
    <form data-lock-form>
      <label><input type="password" data-lock-secret autocomplete="current-password" /></label>
      <button type="submit">${unset ? copy.save : copy.unlock}</button>
    </form>
    <p data-lock-error hidden></p>
    ${unset ? "" : `<button type="button" data-lock-wipe>${copy.wipe}</button>`}
  `;
  const input = pane.querySelector<HTMLInputElement>("[data-lock-secret]");
  if (input) applyLockInput(input, stored ?? "pin");
  pane.querySelectorAll<HTMLInputElement>("[data-lock-kind] input").forEach((radio) => {
    radio.addEventListener("change", () => {
      if (input) {
        input.value = "";
        applyLockInput(input, radio.value === "pin" ? "pin" : "passphrase");
        input.focus();
      }
    });
  });
  const error = pane.querySelector<HTMLElement>("[data-lock-error]");
  const show = (text: string): void => {
    if (!error) return;
    error.hidden = !text;
    error.textContent = text;
  };
  pane.querySelector("[data-lock-form]")?.addEventListener("submit", (event) => {
    event.preventDefault();
    const secret = pane.querySelector<HTMLInputElement>("[data-lock-secret]")?.value ?? "";
    void (async () => {
      if (unset) {
        const kind = classifyLockSecret(secret);
        if (!kind) {
          show(copy.weak);
          return;
        }
        await webAppLock.setSecret(secret, kind);
        onUnlocked();
        return;
      }
      const ok = await webAppLock.unlock(secret);
      if (!ok) {
        show(copy.wrong);
        return;
      }
      onUnlocked();
    })();
  });
  pane.querySelector("[data-lock-wipe]")?.addEventListener("click", () => {
    void webAppLock.wipe().then(() => onUnlocked());
  });
  input?.focus();
  root.replaceChildren(pane);
  return pane;
}
