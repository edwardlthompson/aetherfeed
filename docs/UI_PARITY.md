# UI parity contract

> Chrome IDs and layout rules that keep Android and Windows twins.
> Enforced by `scripts/check-ui-parity.py` and `.cursor/rules/ui-parity.mdc`.

## Layout

- **Wide (≥600dp / ≥720px):** source tree | timeline | reader. Default weights 0.28 / 0.34 / 0.38; the user can drag the splitters. Remember weights.
- **Left column:** Unified, News, Podcasts, and Boards are top-level roots. News (and other modules) expand to folders, then feeds. Chevron expands; the label selects. Folders start collapsed; expand state and last `LibraryPick` persist. Unread counts sit on roots, folders, and feeds (hidden when zero).
- **Desktop chrome:** No bottom mode bar. A fixed action bar sits at the bottom (prev/next in News, play/skip in Podcasts, favorite/info in Boards, plus Share). Unread counts live on the tree roots. Refresh lives in the top bar. The tagline lives in About, not the News rail.
- **Narrow:** timeline or reader fills the screen. Swipe from the left edge (or the folders button) opens the category drawer. System Back / Headlines pops the reader, then the drawer. Swipe (and desktop `j`/`k`) moves to the previous or next article in the sorted list. Left-edge swipe is for folders; article swipe starts past that edge.
- **Back levels:** item → timeline (keep pick) → parent pick (source → folder → All) → stay. Do not finish the activity while an item is open.
- Unlock is a full-screen gate. News is not a greeting page. Import lives in Settings.
- Android recents uses `FLAG_SECURE` (black thumbnail, including lock). Windows and the browser have no recents card: the shell blacks out on `visibilitychange` / `blur` for tab captures, but an OS Alt-Tab thumbnail can still show a frame taken before that cover paints.

Do not share Compose widgets with the DOM. Share these IDs and this layout.

## Required IDs

| Slot | Web (`data-*`) | Android (`testTag`) |
|------|----------------|---------------------|
| Folders | `data-news-folders` | `news-folders` |
| Feeds | `data-news-feeds` | `news-feeds` |
| Timeline | `data-news-timeline` | `news-timeline` |
| Reader | `data-news-reader` | `news-reader` |
| Article row | `data-article-id` / `data-news-article` | `news-article` |
| Refresh | `data-news-refresh` | `news-refresh` |
| Sort | `data-news-sort` | `news-sort` |
| Nav unread | `data-nav-unread` | `news-nav-unread` |
| Unified unread | `data-unified-unread` | `unified-nav-unread` |
| Cache retain | `data-cache-retain` | `cache-retain` |
| Cache progress | `data-news-cache-progress` | `news-cache-progress` |
| Article progress | `data-news-article-progress` | `news-article-progress` |
| Thumbnail | `data-news-thumb` | `news-thumb` |
| Cached dot | `data-news-cached` | `news-cached` |
| Folders open | `data-news-sources` | `news-sources` |
| Unlock | `data-lock-form` | `unlock-pane` |
| Feed status | `data-news-feed-status` | `news-feed-status` |
| Folder unread | `data-news-folder-unread` | `news-folder-unread` |
| Feed unread | `data-news-feed-unread` | `news-feed-unread` |
| Action bar | `data-action-bar` | `action-bar` |
| Action prev | `data-action-prev` | `action-prev` |
| Action next | `data-action-next` | `action-next` |
| Action star | `data-action-star` | `action-star` |
| Action unread | `data-action-unread` | `action-unread` |
| Action play | `data-action-play` | `action-play` |
| Action skip back | `data-action-skip-back` | `action-skip-back` |
| Action skip forward | `data-action-skip-fwd` | `action-skip-fwd` |
| Action favorite | `data-action-favorite` | `action-favorite` |
| Action info | `data-action-info` | `action-info` |
| Action share | `data-action-share` | `action-share` |

A chrome or layout change is incomplete until **both** trees implement it in the same BUILD_PLAN row.

## Reader load

Cached bodies paint immediately. Until extract finishes, the reader shows the title only — never the article URL, comment thread, or feed HTML with links. A green dot marks a cached headline. RSS teasers (`webfeedsFeaturedVisual` / `link_thumbnail`) are not treated as full text. Already-fetched ids (blob or attempt marker) are not downloaded again on the next launch unless the blob is a feed teaser; opening a story can still retry a short body. Image `src` stays on the tag so the offline cache can fetch it. PIN unlock uses a numeric keyboard; passphrase unlock uses the password keyboard. Opening an uncached article cancels the background queue, loads that story, then continues with the next item in the current sort.
