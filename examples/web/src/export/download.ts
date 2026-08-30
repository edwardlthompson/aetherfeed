export function downloadText(filename: string, contents: string): void {
  const anchor = document.createElement("a");
  anchor.download = filename;
  anchor.rel = "noopener";
  const canBlob = typeof URL !== "undefined" && typeof URL.createObjectURL === "function";
  if (canBlob) {
    const url = URL.createObjectURL(new Blob([contents], { type: "text/xml;charset=utf-8" }));
    anchor.href = url;
    anchor.click();
    URL.revokeObjectURL(url);
    return;
  }
  anchor.href = `data:text/xml;charset=utf-8,${encodeURIComponent(contents)}`;
  anchor.click();
}
