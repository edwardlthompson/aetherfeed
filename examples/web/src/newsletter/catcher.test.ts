import { describe, expect, it } from "vitest";
import { canCatchMail, catchNewsletters, NEWSLETTER_FALLBACK, skipReason } from "./catcher";

describe("newsletter catcher", () => {
  it("skips when no mailbox is configured", () => {
    expect(canCatchMail()).toBe(false);
    expect(catchNewsletters()).toEqual([]);
    expect(skipReason()).toBe(NEWSLETTER_FALLBACK);
  });
});
