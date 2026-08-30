import AxeBuilder from "@axe-core/playwright";
import { expect, test } from "@playwright/test";

async function unlockIfNeeded(page: import("@playwright/test").Page): Promise<void> {
  const pane = page.getByTestId("unlock-pane");
  if ((await pane.count()) === 0) return;
  await page.locator("[data-lock-secret]").fill("123456");
  await page.locator("[data-lock-form]").evaluate((form: HTMLFormElement) => form.requestSubmit());
  await expect(page.getByRole("heading", { name: "AetherFeed" })).toBeVisible();
}

async function openApp(page: import("@playwright/test").Page): Promise<void> {
  await page.addInitScript(() => {
    localStorage.setItem("af.product.lastCheckAt", String(Date.now()));
  });
  await page.goto("/");
  await unlockIfNeeded(page);
}

test("renders golden path heading", async ({ page }) => {
  await openApp(page);
  await expect(page.getByRole("heading", { name: "AetherFeed" })).toBeVisible();
  await expect(page.getByTestId("status")).toContainText("AetherFeed");
});

test("passes accessibility audit", async ({ page }) => {
  await openApp(page);
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with settings panel open", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByTestId("settings-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("passes accessibility audit with about panel open", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByTestId("about-panel")).toBeVisible();
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations).toEqual([]);
});

test("homepage visual snapshot", async ({ page }) => {
  await openApp(page);
  await expect(page.locator("main")).toBeVisible();
  await expect(page).toHaveScreenshot("homepage.png", { maxDiffPixelRatio: 0.02 });
});

test("opens settings panel and toggles theme", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "Settings" }).click();
  await expect(page.getByRole("heading", { name: "Settings" })).toBeVisible();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("persists dark theme after reload", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "Settings" }).click();
  await page.locator("[data-settings-theme]").selectOption("dark");
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
  await page.reload();
  await expect(page.locator("html")).toHaveAttribute("data-theme", "dark");
});

test("toggles update check in settings", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "Settings" }).click();
  const toggle = page.locator("[data-settings-update]");
  await expect(toggle).toBeChecked();
  await toggle.uncheck();
  await expect(toggle).not.toBeChecked();
});

test("opens about panel with version", async ({ page }) => {
  await openApp(page);
  await page.getByRole("button", { name: "About" }).click();
  await expect(page.getByRole("heading", { name: "About" })).toBeVisible();
  await expect(page.getByTestId("about-status")).toBeVisible();
  await expect(page.getByRole("link", { name: "Donate via Venmo" })).toBeVisible();
});

test.describe("update prompt", () => {
  test.use({ serviceWorkers: "block" });

  test("shows install dialog for a newer installer and keeps donate off that dialog", async ({
    page,
  }) => {
    await page.route("**/repos/edwardlthompson/aetherfeed/releases/latest", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify({
          html_url: "https://github.com/edwardlthompson/aetherfeed/releases/latest",
          assets: [
            {
              name: "AetherFeed-99.0.0-x64-setup.exe",
              browser_download_url: "https://example.com/AetherFeed-99.0.0-x64-setup.exe",
            },
          ],
        }),
      });
    });
    await page.goto("/");
    await unlockIfNeeded(page);
    await expect(page.getByTestId("update-prompt")).toBeVisible();
    await expect(page.getByTestId("update-install")).toBeVisible();
    await expect(page.getByTestId("donate-venmo")).toHaveCount(0);
    await page.getByTestId("update-later").click();
    await expect(page.getByTestId("update-prompt")).toHaveCount(0);
  });
});

test.describe("donate nudge", () => {
  test("shows once after a version change", async ({ page }) => {
    await page.addInitScript(() => {
      if (!localStorage.getItem("af.product.lastSeenVersion")) {
        localStorage.setItem("af.product.lastSeenVersion", "0.0.1");
      }
      localStorage.setItem("af.product.lastCheckAt", String(Date.now()));
    });
    await page.goto("/");
    await unlockIfNeeded(page);
    await expect(page.getByTestId("donate-nudge")).toBeVisible();
    await expect(page.getByRole("heading", { name: "Development is still going" })).toBeVisible();
    await page.getByTestId("donate-not-now").click();
    await expect(page.getByTestId("donate-nudge")).toHaveCount(0);
    await page.reload();
    await unlockIfNeeded(page);
    await expect(page.getByTestId("donate-nudge")).toHaveCount(0);
  });
});

test.describe("PWA apply update", () => {
  test.use({ serviceWorkers: "block" });

  test("clears restart guard on load", async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem("af-update-restart-pending", "true");
    });
    await openApp(page);
    const pending = await page.evaluate(() => localStorage.getItem("af-update-restart-pending"));
    expect(pending).toBeNull();
  });
});

test("serves cached shell offline via service worker", async ({ page, context }) => {
  await openApp(page);
  await page.waitForLoadState("networkidle");
  await page.waitForFunction(() => navigator.serviceWorker?.controller != null, null, {
    timeout: 15_000,
  });
  await page.reload();
  await page.waitForLoadState("networkidle");
  await expect(page.getByRole("heading", { name: "AetherFeed" })).toBeVisible();

  await context.setOffline(true);
  await page.reload();
  await expect(page.getByRole("heading", { name: "AetherFeed" })).toBeVisible();
  await expect(page.getByTestId("status")).toBeVisible();
});
