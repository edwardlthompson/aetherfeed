import { describe, expect, it } from "vitest";
import { applyRules } from "./applyRules";
import type { Rule, RuleArticle } from "./types";

const rust: RuleArticle = {
  title: "Rust 1.80 release",
  summary: "The rustc compiler ships new diagnostics.",
};

const gossip: RuleArticle = {
  title: "Rust gossip mill",
  summary: "Rumor and gossip from the rustc hallway.",
};

const sports: RuleArticle = { title: "Local sports scores" };

describe("applyRules", () => {
  it("keeps every article when no rules are set", () => {
    expect(applyRules(rust, [])).toEqual({ keep: true, tags: [] });
    expect(applyRules(rust, null)).toEqual({ keep: true, tags: [] });
  });

  it("requires an include keyword when include is set", () => {
    const rules: Rule[] = [{ include: ["rust"], exclude: [] }];
    expect(applyRules(sports, rules).keep).toBe(false);
    expect(applyRules(rust, rules).keep).toBe(true);
  });

  it("lets exclude win over a matching include", () => {
    const rules: Rule[] = [{ include: ["rust"], exclude: ["gossip"], tag: "dev" }];
    expect(applyRules(gossip, rules)).toEqual({ keep: false, tags: [] });
    expect(applyRules(rust, rules)).toEqual({ keep: true, tags: ["dev"] });
  });

  it("auto-tags articles that match include and are not excluded", () => {
    const rules: Rule[] = [
      { include: ["rust"], exclude: [], tag: "lang" },
      { include: ["compiler"], exclude: [], tag: "tooling" },
    ];
    expect(applyRules(rust, rules)).toEqual({ keep: true, tags: ["lang", "tooling"] });
  });

  it("matches keywords case-insensitively across title and body", () => {
    const rules: Rule[] = [{ include: ["RUSTC"], exclude: ["HALLWAY"] }];
    expect(applyRules(rust, rules).keep).toBe(true);
    expect(applyRules(gossip, rules).keep).toBe(false);
  });

  it("treats empty or missing article fields as no match", () => {
    const rules: Rule[] = [{ include: ["rust"], exclude: [] }];
    expect(applyRules({}, rules).keep).toBe(false);
    expect(applyRules(undefined, rules).keep).toBe(false);
  });
});
