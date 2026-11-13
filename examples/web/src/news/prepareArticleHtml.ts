/** Make fetched HTML safe enough to srcdoc and let images resolve. */
export function prepareArticleHtml(html: string, pageUrl: string): string {
  let out = html.replace(/<script\b[\s\S]*?<\/script>/gi, "");
  out = out.replace(/\son\w+\s*=\s*("[^"]*"|'[^']*')/gi, "");
  out = out.replace(/data-src=/gi, "src=");
  out = out.replace(/data-lazy-src=/gi, "src=");
  out = out.replace(/srcset="data:image\/svg\+xml[^"]*"\s*/gi, "");
  const base = `<base href="${pageUrl.replace(/"/g, "")}">`;
  if (/<head[\s>]/i.test(out)) {
    return out.replace(/<head([^>]*)>/i, `<head$1>${base}`);
  }
  return `<!DOCTYPE html><html><head>${base}<meta charset="utf-8"></head><body>${out}</body></html>`;
}
