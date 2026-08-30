import { searchDirectory } from "./search";

export function createDirectoryPane(root: HTMLElement): HTMLElement {
  const pane = document.createElement("section");
  pane.dataset.testid = "podindex-pane";
  pane.innerHTML = `
    <form data-directory-form>
      <label>Directory <input type="search" data-directory-q /></label>
      <button type="submit">Search</button>
    </form>
    <ul data-directory-hits></ul>
  `;
  pane.querySelector("[data-directory-form]")?.addEventListener("submit", (event) => {
    event.preventDefault();
    const q = pane.querySelector<HTMLInputElement>("[data-directory-q]")?.value ?? "";
    const list = pane.querySelector("[data-directory-hits]");
    if (!list) return;
    void searchDirectory(q)
      .then((hits) => {
        list.innerHTML = hits
          .slice(0, 8)
          .map((hit) => `<li>${hit.title}</li>`)
          .join("");
      })
      .catch(() => {
        list.innerHTML = "";
      });
  });
  root.replaceChildren(pane);
  return pane;
}
