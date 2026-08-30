export type SearchDoc = {
  id: string;
  title: string;
  body: string;
};

export type SearchHit = {
  id: string;
  title: string;
  score: number;
};

export type SearchIndex = {
  byId: Map<string, SearchDoc>;
  postings: Map<string, Map<string, number>>;
};
