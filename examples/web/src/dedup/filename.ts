export type FilenameMeta = {
  id?: string;
  md5?: string;
  tags?: string;
  ext?: string;
};

export function formatFilename(pattern: string, meta: FilenameMeta): string {
  const tokens: Record<string, string> = {
    id: meta.id ?? "file",
    md5: meta.md5 ?? "",
    tags: (meta.tags ?? "").replace(/[^\w.-]+/g, "_"),
    ext: meta.ext ?? "bin",
  };
  return pattern.replace(/\{(id|md5|tags|ext)\}/g, (_, key: string) => tokens[key] ?? "");
}
