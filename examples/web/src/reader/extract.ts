import { dropNonContentImages } from "./imageFilter";
import type { ReadableArticle } from "./types";

const KILL_TAGS = new Set([
  "script",
  "style",
  "iframe",
  "object",
  "embed",
  "link",
  "meta",
  "base",
  "noscript",
  "template",
  "form",
]);

const CHROME_TAGS = new Set(["nav", "header", "footer", "aside"]);

function emptyReadable(): ReadableArticle {
  return { title: "", bodyHtml: "", imageSrcs: [] };
}

function stripNodes(doc: Document): void {
  for (const el of Array.from(doc.querySelectorAll("*"))) {
    const tag = el.tagName.toLowerCase();
    if (KILL_TAGS.has(tag) || CHROME_TAGS.has(tag)) {
      el.remove();
      continue;
    }
    const role = el.getAttribute("role");
    if (role === "navigation" || role === "banner" || role === "contentinfo") {
      el.remove();
      continue;
    }
    if (tag === "a") {
      const span = doc.createElement("span");
      while (el.firstChild) span.appendChild(el.firstChild);
      el.replaceWith(span);
      continue;
    }
    for (const attr of Array.from(el.attributes)) {
      const name = attr.name.toLowerCase();
      if (name === "src" && tag === "img") continue;
      if (name.startsWith("on") || name === "srcdoc" || name === "style" || name === "class") {
        el.removeAttribute(attr.name);
      }
      if (name === "href" || name === "srcset" || name === "target") el.removeAttribute(attr.name);
    }
  }
}

function dropCommentChrome(root: Element): void {
  for (const el of Array.from(root.querySelectorAll("*"))) {
    const id = el.getAttribute("id") ?? "";
    const cls = el.getAttribute("class") ?? "";
    if (/(?:^|[\s_-])(comments?|disqus|discuss)(?:$|[\s_-])/i.test(`${id} ${cls}`)) {
      el.remove();
    }
  }
  for (const el of Array.from(root.querySelectorAll("p, li"))) {
    const text = el.textContent?.toLowerCase() ?? "";
    if (/(article url|comments url|# comments)/.test(text) && (el.textContent?.length ?? 0) < 400) {
      el.remove();
    }
  }
}

function dropBareUrls(root: Element): void {
  const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
  const nodes: Text[] = [];
  while (walker.nextNode()) nodes.push(walker.currentNode as Text);
  for (const node of nodes) {
    node.textContent = (node.textContent ?? "").replace(/https?:\/\/\S+/gi, "");
  }
}

function lastSrcset(srcset: string | null): string {
  if (!srcset) return "";
  const parts = srcset.split(",").map((part) => part.trim().split(/\s+/)[0] ?? "");
  return [...parts].reverse().find((url) => /^https?:\/\//i.test(url)) ?? "";
}

function promoteLazyImages(root: ParentNode): void {
  for (const img of Array.from(root.querySelectorAll("img"))) {
    const src = img.getAttribute("src")?.trim() ?? "";
    if (/^https?:\/\//i.test(src)) continue;
    const lazy =
      img.getAttribute("data-src") ||
      img.getAttribute("data-lazy-src") ||
      img.getAttribute("data-original") ||
      lastSrcset(img.getAttribute("srcset"));
    if (lazy && /^https?:\/\//i.test(lazy.trim())) img.setAttribute("src", lazy.trim());
  }
}

function contentRoot(doc: Document): Element {
  const nodes = Array.from(
    doc.querySelectorAll("article, main, .entry-content, .post-content, .td-post-content"),
  );
  if (nodes.length === 0) return doc.body;
  return nodes.reduce((best, el) => (el.innerHTML.length > best.innerHTML.length ? el : best));
}

function imageSrcs(root: Element): string[] {
  const seen = new Set<string>();
  const out: string[] = [];
  for (const img of Array.from(root.querySelectorAll("img"))) {
    const src = img.getAttribute("src")?.trim() ?? "";
    if (!src || seen.has(src)) continue;
    seen.add(src);
    out.push(src);
  }
  return out;
}

/** Strip chrome/nav/scripts. Empty or whitespace input yields an empty body. */
export function extractReadable(html: string): ReadableArticle {
  if (html == null || !String(html).trim()) return emptyReadable();
  const doc = new DOMParser().parseFromString(String(html), "text/html");
  promoteLazyImages(doc);
  const root = contentRoot(doc);
  dropCommentChrome(root);
  stripNodes(doc);
  dropBareUrls(root);
  dropNonContentImages(root);
  const title =
    root.querySelector("h1")?.textContent?.trim() ||
    doc.querySelector("title")?.textContent?.trim() ||
    "";
  return { title, bodyHtml: root.innerHTML.trim(), imageSrcs: imageSrcs(root) };
}
