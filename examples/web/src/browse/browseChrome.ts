import { t } from "../i18n";
import type { SortOrder, TreeNode } from "./contentFilter";

export type BrowseNavOptions = {
  nodes: TreeNode[];
  selectedId: string;
  onSelect: (id: string) => void;
  unreadOnly: boolean;
  onUnreadOnly: (value: boolean) => void;
  unreadLabel?: string;
  sort: SortOrder;
  onSort: (sort: SortOrder) => void;
};

function treeButton(
  node: TreeNode,
  selectedId: string,
  onSelect: (id: string) => void,
): HTMLElement {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "af-tree-item";
  btn.dataset.testid = `tree-node-${node.id || "all"}`;
  btn.setAttribute("aria-current", selectedId === node.id ? "true" : "false");
  const unread = node.unreadCount ?? 0;
  if (unread > 0) btn.classList.add("is-unread");
  const label = document.createElement("span");
  label.className = "af-tree-label";
  label.textContent = node.label;
  btn.append(label);
  if (unread > 0) {
    const count = document.createElement("span");
    count.className = "af-tree-count";
    count.dataset.testid = `tree-count-${node.id || "all"}`;
    count.textContent = String(unread);
    btn.append(count);
  }
  btn.addEventListener("click", () => onSelect(node.id));
  return btn;
}

function appendNodes(
  parent: HTMLElement,
  nodes: TreeNode[],
  selectedId: string,
  onSelect: (id: string) => void,
): void {
  for (const node of nodes) {
    parent.append(treeButton(node, selectedId, onSelect));
    if (node.children?.length) {
      const group = document.createElement("div");
      group.className = "af-tree-children";
      group.setAttribute("role", "group");
      appendNodes(group, node.children, selectedId, onSelect);
      parent.append(group);
    }
  }
}

export function createBrowseNav(opts: BrowseNavOptions): {
  tree: HTMLElement;
  toolbar: HTMLElement;
} {
  const tree = document.createElement("div");
  tree.className = "af-folder-tree";
  tree.dataset.testid = "folder-tree";
  tree.setAttribute("role", "tree");
  tree.setAttribute("aria-label", t("filter.tree"));
  appendNodes(tree, opts.nodes, opts.selectedId, opts.onSelect);

  const toolbar = document.createElement("div");
  toolbar.className = "af-list-toolbar";
  toolbar.dataset.testid = "list-toolbar";
  const unread = document.createElement("label");
  unread.className = "af-list-unread";
  const box = document.createElement("input");
  box.type = "checkbox";
  box.dataset.testid = "filter-unread";
  box.checked = opts.unreadOnly;
  box.addEventListener("change", () => opts.onUnreadOnly(box.checked));
  unread.append(box, ` ${opts.unreadLabel ?? t("filter.unread")}`);

  const sortWrap = document.createElement("div");
  sortWrap.className = "af-list-sort";
  sortWrap.append(t("filter.sort"));
  const toggle = document.createElement("div");
  toggle.className = "af-sort-toggle";
  toggle.dataset.testid = "filter-sort";
  for (const [value, key] of [
    ["newest", "filter.sort.newest"],
    ["oldest", "filter.sort.oldest"],
  ] as const) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "af-sort-btn";
    btn.dataset.testid = `filter-sort-${value}`;
    btn.setAttribute("aria-pressed", opts.sort === value ? "true" : "false");
    btn.textContent = t(key);
    btn.addEventListener("click", () => opts.onSort(value));
    toggle.append(btn);
  }
  sortWrap.append(toggle);
  toolbar.append(unread, sortWrap);
  return { tree, toolbar };
}

export function wrapBrowse(
  pane: HTMLElement,
  nav: { tree: HTMLElement; toolbar: HTMLElement },
  main: HTMLElement,
): void {
  const layout = document.createElement("div");
  layout.className = "af-browse";
  layout.dataset.testid = "browse-layout";
  const mainCol = document.createElement("div");
  mainCol.className = "af-browse-main";
  mainCol.append(nav.toolbar, main);
  layout.append(nav.tree, mainCol);
  pane.append(layout);
}
