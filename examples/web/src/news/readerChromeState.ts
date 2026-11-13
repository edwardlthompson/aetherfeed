let detachReaderKeys: (() => void) | undefined;

export function resetDetachReaderKeys(): void {
  detachReaderKeys?.();
  detachReaderKeys = undefined;
}

export function setDetachReaderKeys(fn: () => void): void {
  detachReaderKeys = fn;
}
