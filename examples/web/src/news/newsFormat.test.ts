import { describe, expect, it } from "vitest";
import { ageLabel, listSnippet, plainSnippet } from "./newsFormat";

describe("newsFormat", () => {
  it("strips html and caps the snippet", () => {
    const snippet = plainSnippet(`<p>${"word ".repeat(80)}</p>`, 40);
    expect(snippet.startsWith("word")).toBe(true);
    expect(snippet.endsWith("…")).toBe(true);
    expect(snippet.includes("<")).toBe(false);
  });

  it("drops link stubs from list snippets", () => {
    expect(listSnippet("<p>Article URL: https://ex.example/a</p>")).toBe("");
    expect(listSnippet("<p>Hello world</p>")).toBe("Hello world");
  });

  it("buckets age like ReadYou/Feeder metadata", () => {
    const now = 100_000_000;
    expect(ageLabel(undefined, now)).toBe("");
    expect(ageLabel(now - 10 * 60_000, now)).toBe("now");
    expect(ageLabel(now - 5 * 3_600_000, now)).toBe("5h");
    expect(ageLabel(now - 3 * 86_400_000, now)).toBe("3d");
  });
});
