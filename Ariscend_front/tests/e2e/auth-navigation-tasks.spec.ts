import { randomUUID } from "node:crypto";
import { expect, test } from "@playwright/test";

test("registration, login, navigation, and task CRUD", async ({ page }) => {
  const id = randomUUID();
  const name = `E2E ${id.slice(0, 8)}`;
  const email = `e2e-${id}@example.test`;
  const password = `E2e-${id}`;

  await page.goto("/pendientes");
  await expect(page).toHaveURL(/\/login\?next=%2Fpendientes$/);

  const accessMode = page.getByRole("group", { name: "Tipo de acceso" });
  await accessMode.getByRole("button", { name: "Registrarme", exact: true }).click();
  await page.getByLabel("Nombre", { exact: true }).fill(name);
  await page.getByLabel("Correo electrónico", { exact: true }).fill(email);
  await page.locator('input[autocomplete="new-password"]').fill(password);
  await page.locator("form").getByRole("button", { name: "Crear cuenta", exact: true }).click();

  await expect(page).toHaveURL(/\/pendientes$/);
  await expect(page.getByRole("heading", { name: "Pendientes", level: 1 })).toBeVisible();

  await page.getByRole("link", { name: "Ajustes", exact: true }).click();
  await page.getByRole("button", { name: "Cerrar sesión", exact: true }).click();
  await expect(page).toHaveURL(/\/login$/);
  await page.waitForLoadState("load");

  const emailInput = page.getByLabel("Correo electrónico", { exact: true });
  const passwordInput = page.locator('input[autocomplete="current-password"]');
  await emailInput.fill(email);
  await passwordInput.fill(password);
  await expect(emailInput).toHaveValue(email);
  await expect(passwordInput).toHaveValue(password);
  await page.locator("form").getByRole("button", { name: "Ingresar", exact: true }).click();

  await expect(page).toHaveURL(/\/$/);
  await expect(page.getByRole("heading", { name: new RegExp(name.split(" ")[0]) })).toBeVisible();

  const navigation = page.getByRole("navigation", { name: "Navegación principal" });
  await navigation.getByRole("link", { name: "Hábitos", exact: true }).click();
  await expect(page.getByRole("heading", { name: "Hábitos", level: 1 })).toBeVisible();
  await navigation.getByRole("link", { name: "Finanzas", exact: true }).click();
  await expect(page.getByRole("heading", { name: "Finanzas", level: 1 })).toBeVisible();
  await navigation.getByRole("link", { name: "Notas", exact: true }).click();
  await expect(page.getByRole("heading", { name: "Notas", level: 1 })).toBeVisible();
  await navigation.getByRole("link", { name: "Pendientes", exact: true }).click();

  const taskTitle = `Pendiente ${id.slice(0, 8)}`;
  await page.getByRole("button", { name: "Nuevo", exact: true }).click();
  await page.getByLabel("Título del pendiente").fill(taskTitle);
  await page.getByLabel("Prioridad").selectOption("HIGH");
  await page.locator("form").getByRole("button", { name: "Crear", exact: true }).click();

  await expect(page.getByRole("heading", { name: taskTitle, level: 2 })).toBeVisible();
  await expect(page.getByText("Prioridad Alta", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: `Completar ${taskTitle}`, exact: true }).click();
  await expect(page.getByRole("button", {
    name: `Marcar ${taskTitle} como pendiente`,
    exact: true,
  })).toBeVisible();

  page.once("dialog", async (dialog) => {
    expect(dialog.type()).toBe("confirm");
    expect(dialog.message()).toContain(taskTitle);
    await dialog.accept();
  });
  await page.getByRole("button", { name: `Eliminar ${taskTitle}`, exact: true }).click();
  await expect(page.getByRole("heading", { name: taskTitle, level: 2 })).toHaveCount(0);
});
