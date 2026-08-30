/** Port of Android BooruSource / TagQuery / BooruPost — generic media-board names. */

export type BoardSourceKind = "danbooru" | "gelbooru" | "generic";

export type BoardSource = {
  id: string;
  kind: BoardSourceKind;
  baseUrl: string;
  label: string;
};

export type TagQuery = {
  tags: string[];
  page: number;
};

export type BoardPost = {
  id: string;
  sourceId: string;
  remoteId: string;
  fileUrl: string;
  previewUrl?: string;
  tags: string[];
};

export type BoardFetchCode = "aborted" | "timeout" | "network" | "invalid";

export class BoardFetchError extends Error {
  readonly code: BoardFetchCode;
  readonly retryable: boolean;

  constructor(code: BoardFetchCode, message: string) {
    super(message);
    this.name = "BoardFetchError";
    this.code = code;
    this.retryable = code === "timeout" || code === "network";
  }
}
