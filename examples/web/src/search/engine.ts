import { tokenize } from "./tokenize";
import type { SearchDoc, SearchHit, SearchIndex } from "./types";

function emptyIndex(): SearchIndex {
  return { byId: new Map(), postings: new Map() };
}

let current: SearchIndex = emptyIndex();

function addToken(index: SearchIndex, token: string, docId: string): void {
  let posting = index.postings.get(token);
  if (!posting) {
    posting = new Map();
    index.postings.set(token, posting);
  }
  posting.set(docId, (posting.get(docId) ?? 0) + 1);
}

export function buildIndex(docs: readonly SearchDoc[] | null | undefined): SearchIndex {
  const index = emptyIndex();
  for (const doc of docs ?? []) {
    if (!doc?.id) continue;
    const title = doc.title ?? "";
    const body = doc.body ?? "";
    index.byId.set(doc.id, { id: doc.id, title, body });
    for (const token of tokenize(`${title} ${body}`)) {
      addToken(index, token, doc.id);
    }
  }
  current = index;
  return index;
}

function intersectIds(tokens: string[]): Set<string> | null {
  let ids: Set<string> | null = null;
  for (const token of tokens) {
    const posting = current.postings.get(token);
    if (!posting) return new Set();
    const next = new Set(posting.keys());
    if (!ids) {
      ids = next;
      continue;
    }
    ids = new Set([...ids].filter((id) => next.has(id)));
    if (!ids.size) return ids;
  }
  return ids;
}

export function search(query: string | null | undefined): SearchHit[] {
  const tokens = tokenize(query);
  if (!tokens.length) return [];
  const ids = intersectIds(tokens);
  if (!ids?.size) return [];
  const hits: SearchHit[] = [];
  for (const id of ids) {
    const doc = current.byId.get(id);
    if (!doc) continue;
    let score = 0;
    for (const token of tokens) {
      score += current.postings.get(token)?.get(id) ?? 0;
    }
    hits.push({ id: doc.id, title: doc.title, score });
  }
  hits.sort(
    (a, b) => b.score - a.score || a.title.localeCompare(b.title) || a.id.localeCompare(b.id),
  );
  return hits;
}
