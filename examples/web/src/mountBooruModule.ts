import { createBooruGridChrome } from "./booru/gridChrome";
import { subscriptionTree } from "./browse/wireFilters";
import { loadBrowsePrefs, saveBrowsePrefs } from "./settings/browsePrefs";

export function mountBooruModule(booruMount: HTMLElement): void {
  const booruPrefs = loadBrowsePrefs("booru");
  let booruUnreadOnly = booruPrefs.unreadOnly;
  let booruNode = booruPrefs.nodeId;
  let booruSort = booruPrefs.sort;
  let booruQuery = "";
  const paint = () => {
    createBooruGridChrome(booruMount, {
      searchValue: booruQuery,
      onSearch: (query) => {
        booruQuery = query;
      },
      browse: {
        nodes: subscriptionTree("booru"),
        selectedId: booruNode,
        onSelect: (id) => {
          booruNode = id;
          saveBrowsePrefs("booru", { nodeId: id });
          paint();
        },
        unreadOnly: booruUnreadOnly,
        onUnreadOnly: (value) => {
          booruUnreadOnly = value;
          saveBrowsePrefs("booru", { unreadOnly: value });
          paint();
        },
        sort: booruSort,
        onSort: (nextSort) => {
          booruSort = nextSort;
          saveBrowsePrefs("booru", { sort: nextSort });
          paint();
        },
      },
    });
  };
  paint();
}
