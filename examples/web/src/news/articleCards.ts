import type { Article } from "@aetherfeed/domain";

export function createArticleCards(
  articles: Article[],
  onOpen?: (article: Article) => void,
): HTMLElement {
  const list = document.createElement("ul");
  list.className = "af-news-list";
  list.dataset.testid = "news-article-list";
  for (const article of articles) {
    const item = document.createElement("li");
    item.className = "af-news-card-wrap";
    item.dataset.testid = `news-article-${article.id}`;
    const card = document.createElement("button");
    card.type = "button";
    card.className = "af-news-card";
    card.addEventListener("click", () => onOpen?.(article));
    let thumb: HTMLElement;
    if (article.imageUrl) {
      const img = document.createElement("img");
      img.className = "af-news-card-thumb";
      img.alt = "";
      img.loading = "lazy";
      img.src = article.imageUrl;
      img.addEventListener("error", () => img.classList.add("is-missing"));
      thumb = img;
    } else {
      thumb = document.createElement("div");
      thumb.className = "af-news-card-thumb is-missing";
    }
    const body = document.createElement("div");
    body.className = "af-news-card-body";
    const headline = document.createElement("span");
    headline.className = "af-news-item-title";
    headline.textContent = article.title;
    body.append(headline);
    if (article.summary) {
      const summary = document.createElement("p");
      summary.className = "af-news-item-summary";
      summary.textContent = article.summary.slice(0, 220);
      body.append(summary);
    }
    card.append(thumb, body);
    item.append(card);
    list.append(item);
  }
  return list;
}
