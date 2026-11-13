import { afterEach, beforeEach, describe, expect, it } from "vitest";
import en from "../locales/en.json";
import { createNewsChrome } from "./readerChrome";

const messages = en as Record<string, string>;

describe("createNewsChrome", () => {
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement("div");
    document.body.append(root);
  });

  afterEach(() => {
    root.remove();
  });

  it("renders unread total and empty body", () => {
    createNewsChrome(root, { unreadCount: 4 });

    const pane = root.querySelector<HTMLElement>("[data-testid='news-pane']");
    expect(pane).not.toBeNull();
    expect(pane?.getAttribute("aria-label")).toBe(messages["news.title"]);
    expect(root.querySelector("[data-testid='news-unread']")?.textContent).toBe(
      messages["news.unread_total"].replace("{count}", "4"),
    );
    expect(root.querySelector("[data-testid='news-body']")?.textContent).toBe(
      messages["news.pane.body"],
    );
  });

  it("formats zero unread via the i18n placeholder", () => {
    createNewsChrome(root, { unreadCount: 0 });
    expect(root.querySelector("[data-testid='news-unread']")?.textContent).toBe(
      messages["news.unread_total"].replace("{count}", "0"),
    );
  });

  it("replaces prior content when re-rendered", () => {
    createNewsChrome(root, { unreadCount: 1 });
    createNewsChrome(root, { unreadCount: 9 });
    expect(root.querySelectorAll("[data-testid='news-pane']")).toHaveLength(1);
    expect(root.querySelector("[data-testid='news-unread']")?.textContent).toBe(
      messages["news.unread_total"].replace("{count}", "9"),
    );
  });

  it("renders article titles in the news list", () => {
    createNewsChrome(root, {
      unreadCount: 1,
      articles: [
        {
          id: "g1",
          feedId: "f",
          title: "Desk Story",
          url: "https://ex.test/story",
          summary: "Lead",
        },
      ],
    });
    const list = root.querySelector("[data-testid='news-article-list']");
    expect(list?.textContent).toContain("Desk Story");
    expect(root.querySelector(".af-news-card")).not.toBeNull();
    expect(root.querySelector(".af-news-card-thumb")).not.toBeNull();
    expect(root.querySelector("[data-testid='news-body']")).toBeNull();
  });

  it("renders an in-app reader frame for the opened article", () => {
    createNewsChrome(root, {
      unreadCount: 0,
      openArticle: {
        id: "g1",
        feedId: "f",
        title: "Desk Story",
        url: "https://ex.test/story",
      },
      readerHtml: "<p>Hello <img src='https://ex.test/p.jpg' alt=''></p>",
      starred: true,
      read: true,
    });
    expect(root.querySelector("[data-testid='article-reader-body']")).not.toBeNull();
    expect(root.querySelector("iframe")).toBeNull();
    expect(root.querySelector(".af-reader-toolbar")).not.toBeNull();
    expect(root.querySelector("[data-testid='article-reader-title']")?.textContent).toBe(
      "Desk Story",
    );
    expect(root.querySelector("[data-testid='article-star']")?.getAttribute("aria-pressed")).toBe(
      "true",
    );
    expect(root.querySelector("[data-testid='article-park-unread']")).not.toBeNull();
    expect(root.querySelector("[data-testid='article-open-browser']")).not.toBeNull();
    expect(
      root.querySelector("[data-testid='reader-mode-reading']")?.getAttribute("aria-pressed"),
    ).toBe("true");
  });

  it("exposes previous and next controls for the open article", () => {
    const seen: string[] = [];
    createNewsChrome(root, {
      unreadCount: 0,
      articles: [
        { id: "g1", feedId: "f", title: "One", url: "https://ex.test/1" },
        { id: "g2", feedId: "f", title: "Two", url: "https://ex.test/2" },
      ],
      openArticle: { id: "g1", feedId: "f", title: "One", url: "https://ex.test/1" },
      readerHtml: "<p>Hi</p>",
      canPrev: false,
      canNext: true,
      onNext: () => seen.push("next"),
    });
    const next = root.querySelector<HTMLButtonElement>("[data-testid='article-next']");
    expect(next?.disabled).toBe(false);
    expect(root.querySelector<HTMLButtonElement>("[data-testid='article-prev']")?.disabled).toBe(
      true,
    );
    next?.click();
    expect(seen).toEqual(["next"]);
  });
});
