import { describe, expect, it } from "vitest";
import { buildIndex, search } from "./engine";
import type { SearchDoc } from "./types";

const docs: SearchDoc[] = [
  { id: "alpha", title: "Alpha News", body: "local vault article about feeds" },
  { id: "beta", title: "Beta Show", body: "unrelated podcast notes" },
  { id: "gamma", title: "Encrypted Feed", body: "Local FIRST vault backup" },
];

describe("local full-text search index", () => {
  it("matches tokens in title or body", () => {
    buildIndex(docs);
    expect(search("vault").map((hit) => hit.id)).toEqual(["alpha", "gamma"]);
    expect(search("podcast").map((hit) => hit.id)).toEqual(["beta"]);
    expect(search("alpha news").map((hit) => hit.id)).toEqual(["alpha"]);
  });

  it("returns empty results for an empty query without throwing", () => {
    buildIndex(docs);
    expect(() => search("")).not.toThrow();
    expect(search("")).toEqual([]);
    expect(search("   ")).toEqual([]);
    expect(search(null)).toEqual([]);
    expect(search(undefined)).toEqual([]);
  });

  it("is case-insensitive", () => {
    buildIndex(docs);
    expect(search("ENCRYPTED").map((hit) => hit.id)).toEqual(["gamma"]);
    expect(search("Local First").map((hit) => hit.id)).toEqual(["gamma"]);
    expect(search("VAULT").map((hit) => hit.id)).toEqual(search("vault").map((hit) => hit.id));
  });

  it("builds an empty index without throwing", () => {
    expect(() => buildIndex([])).not.toThrow();
    expect(search("vault")).toEqual([]);
    expect(() => buildIndex(null)).not.toThrow();
    expect(search("feeds")).toEqual([]);
  });
});
