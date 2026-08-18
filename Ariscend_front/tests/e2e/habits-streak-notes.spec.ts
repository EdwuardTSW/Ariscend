import { randomUUID } from "node:crypto";
import { expect, test } from "@playwright/test";

test("habit celebration, streak, reusable category, and mobile quick note", async ({ page }) => {
  const id = randomUUID();
  const email = `habits-${id}@example.test`;

  await page.goto("/habitos");
  const accessMode = page.getByRole("group", { name: "Tipo de acceso" });
  await accessMode.getByRole("button", { name: "Registrarme", exact: true }).click();
  await page.getByLabel("Nombre", { exact: true }).fill(`Ares ${id.slice(0, 6)}`);
  await page.getByLabel("Correo electrónico", { exact: true }).fill(email);
  await page.locator('input[autocomplete="new-password"]').fill(`Ares-${id}`);
  await page.locator("form").getByRole("button", { name: "Crear cuenta", exact: true }).click();
  await expect(page).toHaveURL(/\/habitos$/);

  async function createHabit(name: string, emoji: string) {
    await page.getByRole("button", { name: "Nuevo hábito", exact: true }).click();
    await page.getByLabel("Emoji del hábito").fill(emoji);
    await page.getByLabel("Hábito", { exact: true }).fill(name);
    await page.getByLabel("Categoría", { exact: true }).selectOption("Salud");
    await page.locator("form").getByRole("button", { name: "Crear", exact: true }).click();
    await expect(page.getByRole("heading", { name, level: 2 })).toBeVisible();
  }

  const firstHabit = `Entrenar ${id.slice(0, 5)}`;
  const secondHabit = `Caminar ${id.slice(0, 5)}`;
  await createHabit(firstHabit, "💪");
  await createHabit(secondHabit, "🏃");

  await page.getByRole("button", { name: `Completar ${firstHabit}`, exact: true }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);

  await page.getByRole("button", { name: `Completar ${secondHabit}`, exact: true }).click();
  const celebration = page.getByRole("dialog");
  await expect(celebration.getByRole("heading", { name: "Misión cumplida." })).toBeVisible();
  await expect(celebration.getByText(/disciplina ya está hablando por ti/i)).toBeVisible();
  await celebration.getByRole("button", { name: "Cerrar celebración" }).click();
  await expect(page.getByRole("link", { name: "Racha activa de 1 día" })).toBeVisible();

  await page.setViewportSize({ width: 390, height: 844 });
  await page.getByRole("button", { name: "Crear nueva nota" }).click();
  await expect(page).toHaveURL(/\/notas\/\d+$/);
  await expect(page.getByLabel("Título de la nota")).toBeFocused();
});
