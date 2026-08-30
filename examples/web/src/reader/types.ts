export type ReadableArticle = {
  title: string;
  bodyHtml: string;
  imageSrcs: string[];
};

export type VaultEntry = {
  articleId: string;
  bodyHtml: string;
  images: Record<string, string>;
};

export type ReaderViewOptions = {
  articleId?: string;
  fetchImpl?: typeof fetch;
  timeoutMs?: number;
  signal?: AbortSignal;
  allowNetwork?: boolean;
  onProgress?: (done: number, total: number) => void;
};

export type ReaderViewHandle = {
  abort: () => void;
  articleId: string;
  ready: Promise<void>;
};

export type ImageFetchOptions = {
  fetchImpl?: typeof fetch;
  timeoutMs?: number;
  signal?: AbortSignal;
  onProgress?: (done: number, total: number) => void;
};

export type ReaderImageCode = "aborted" | "timeout";

export class ReaderImageError extends Error {
  readonly code: ReaderImageCode;

  constructor(code: ReaderImageCode, message = code) {
    super(message);
    this.name = "ReaderImageError";
    this.code = code;
  }
}
