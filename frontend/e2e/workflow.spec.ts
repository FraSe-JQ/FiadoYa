import { test, expect } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
const { Client } = require("../../scripts/demo-seed.cjs");
const root = path.resolve(__dirname, "../..");
const evidence = path.join(root, "docs/evidencias");
const environment = Object.fromEntries(
  fs
    .readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => line.includes("="))
    .map((line) => {
      const i = line.indexOf("=");
      return [line.slice(0, i), line.slice(i + 1)];
    }),
);
test("registro, comercio, productos, cliente, compra francesa y pago con mora", async ({
  page,
}) => {
  const errors: string[] = [];
  page.on("pageerror", (error) => errors.push(error.message));
  const screenshot = async (name: string) =>
    page.screenshot({
      path: path.join(evidence, name + ".png"),
      fullPage: true,
    });
  await page.goto("/");
  await expect(
    page.getByRole("heading", { name: "Bienvenido de nuevo" }),
  ).toBeVisible();
  await screenshot("01-login");
  const suffix = Date.now();
  const email = `e2e-${suffix}@fiadoya.test`,
    password = crypto.randomBytes(20).toString("base64url");
  await page.getByRole("button", { name: "Regístrate", exact: true }).click();
  await page
    .getByLabel("Nombre completo", { exact: true })
    .fill("Comerciante de prueba");
  await page.getByLabel("Correo electrónico", { exact: true }).fill(email);
  await page.getByLabel("Contraseña", { exact: true }).fill(password);
  await page
    .getByRole("button", { name: "Registrar usuario", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Tu espacio está listo" }),
  ).toBeVisible();
  await screenshot("02-registro");
  const platform = new Client();
  await platform.login(
    environment.BOOTSTRAP_EMAIL,
    environment.BOOTSTRAP_PASSWORD,
  );
  const registered = (await platform.request("/users")).find(
    (user: any) => user.email === email,
  );
  const store = await platform.request("/stores", {
    name: `Bodega Prueba ${suffix}`,
    businessType: "Bodega",
    address: "Dirección ficticia",
    phone: "",
    active: true,
  });
  await platform.request(`/stores/${store.id}/admins/${registered.id}`, {});
  await platform.request("/clock", { dateTime: "2026-10-06T10:38:00" });
  await page.getByRole("button", { name: "Salir", exact: true }).click();
  await page.getByLabel("Correo electrónico", { exact: true }).fill(email);
  await page.getByLabel("Contraseña", { exact: true }).fill(password);
  await page
    .getByRole("button", { name: "Iniciar sesión", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Así va tu comercio" }),
  ).toBeVisible();
  await page
    .locator("nav")
    .getByRole("button", { name: /Establecimientos/ })
    .click();
  await expect(
    page.getByText(store.name, { exact: true }).last(),
  ).toBeVisible();
  await screenshot("03-establecimiento");
  await page
    .locator("nav")
    .getByRole("button", { name: /Catálogo/ })
    .click();
  for (const [code, name, price] of [
    ["AR-01", "Arroz extra", 52],
    ["GA-01", "Balón de gas", 60],
  ]) {
    await page.getByRole("button", { name: /Registrar producto/ }).click();
    await page.locator("#edit-code").fill(String(code));
    await page.locator("#edit-name").fill(String(name));
    await page.locator("#edit-cashPrice").fill(String(price));
    await page.locator("#edit-creditPrice").fill(String(Number(price) + 3));
    await page.getByRole("button", { name: "Guardar", exact: true }).click();
    await expect(
      page.getByRole("heading", { name: "Catálogo", exact: true }),
    ).toBeVisible();
  }
  await screenshot("04-catalogo");
  await page
    .locator("nav")
    .getByRole("button", { name: /Clientes/ })
    .click();
  await page.getByRole("button", { name: /Registrar cliente/ }).click();
  await page.locator("#edit-documentNumber").fill("72839401");
  await page.locator("#edit-firstName").fill("Juan Alberto");
  await page.locator("#edit-lastName").fill("Pérez Gómez");
  await page.locator("#edit-penaltyPercent").fill("35");
  await page
    .locator('summary[aria-label="Ayuda: Día mensual de corte"]')
    .click();
  expect(
    await page
      .locator("fy-editor input,fy-editor select,fy-editor textarea")
      .evaluateAll((elements) =>
        elements.every(
          (element) =>
            !!element
              .closest("fy-field")
              ?.querySelector("details p")
              ?.textContent?.trim(),
        ),
      ),
  ).toBeTruthy();
  await screenshot("05-ayuda-por-campo");
  await page.getByRole("button", { name: "Guardar", exact: true }).click();
  await expect(
    page.getByRole("heading", { name: "Clientes", exact: true }),
  ).toBeVisible();
  await screenshot("06-clientes");
  await page
    .locator("nav")
    .getByRole("button", { name: /Nueva venta/ })
    .click();
  await page
    .locator("#sale-customer")
    .selectOption({ label: "Juan Alberto Pérez Gómez · PEN" });
  await page
    .locator(".catalog-list")
    .getByRole("button", { name: /Arroz extra/ })
    .click();
  await page
    .locator(".catalog-list")
    .getByRole("button", { name: /Arroz extra/ })
    .click();
  await page
    .locator(".catalog-list")
    .getByRole("button", { name: /Balón de gas/ })
    .click();
  await page
    .getByRole("button", { name: "Simular financiamiento", exact: true })
    .click();
  await expect(
    page.getByRole("heading", {
      name: "Revisa el cronograma antes de confirmar",
    }),
  ).toBeVisible();
  await expect(page.getByText("S/ 57.58", { exact: true }))
    .toBeVisible()
    .catch(
      async () =>
        await expect(page.getByText(/57[,.]58/).first()).toBeVisible(),
    );
  await screenshot("07-simulacion-cronograma");
  await page
    .getByRole("button", { name: "Confirmar y registrar compra", exact: true })
    .click();
  await expect(
    page.getByRole("status").filter({ hasText: /Compra #/ }),
  ).toBeVisible();
  await platform.request("/clock", { dateTime: "2026-12-06T10:00:00" });
  await page
    .locator("nav")
    .getByRole("button", { name: /Cuentas y pagos/ })
    .click();
  await expect(
    page.getByRole("heading", {
      name: "Liquidación de obligaciones exigibles",
    }),
  ).toBeVisible();
  await expect(page.locator(".settlement-total")).toContainText(/58[,.]12/);
  await screenshot("08-cuenta-mora");
  await page
    .getByRole("button", { name: "Registrar pago completo", exact: true })
    .click();
  await page
    .getByRole("button", { name: "Confirmar pago recibido", exact: true })
    .click();
  await expect(
    page.getByRole("status").filter({ hasText: /Pago #/ }),
  ).toBeVisible();
  await expect(page.locator(".metrics article").first()).toContainText(
    /111[,.]80/,
  );
  await screenshot("09-pago-saldo");
  const downloadPromise = page.waitForEvent("download");
  await page.getByRole("link", { name: "Exportar CSV" }).click();
  const download = await downloadPromise;
  await download.saveAs(path.join(evidence, "cuenta-e2e.csv"));
  await page
    .locator("nav")
    .getByRole("button", { name: /Reportes/ })
    .click();
  await page
    .locator("#yield-customer")
    .selectOption({ label: "Juan Alberto Pérez Gómez" });
  await expect(page.locator("#yield-purchase option")).toHaveCount(2);
  await page.locator("#yield-purchase").selectOption({ index: 1 });
  await expect(
    page.getByText("Rendimiento anual efectivo contractual", { exact: true }),
  ).toBeVisible();
  await page
    .getByRole("button", { name: "Generar cierres pendientes", exact: true })
    .click();
  await expect(page.getByRole("status")).toContainText("Cierres generados");
  await screenshot("15-reportes-rendimiento");
  await page
    .locator("nav")
    .getByRole("button", { name: /Cuentas y pagos/ })
    .click();
  await expect(page.locator(".cycle")).toHaveCount(2);
  await page.locator(".cycle summary").first().click();
  await expect(
    page
      .locator(".cycle")
      .first()
      .getByText("Compras incluidas", { exact: true }),
  ).toBeVisible();
  await screenshot("16-cierre-historico");
  await page
    .locator("nav")
    .getByRole("button", { name: /Ayuda y soporte/ })
    .click();
  await page
    .getByLabel("Asunto", { exact: true })
    .fill("Consulta de demostración");
  await page
    .getByLabel("Descripción", { exact: true })
    .fill("Solicitud ficticia generada durante la prueba E2E.");
  await page
    .getByRole("button", { name: "Guardar solicitud", exact: true })
    .click();
  await expect(page.getByRole("status")).toContainText("Solicitud guardada");
  await screenshot("10-ayuda-soporte");
  await page
    .locator("nav")
    .getByRole("button", { name: /Resumen/ })
    .click();
  await expect(page.locator(".metrics article").nth(1)).toContainText(
    /111[,.]80/,
  );
  await expect(page.locator(".metrics article").nth(3)).toContainText(
    /58[,.]12/,
  );
  await screenshot("11-dashboard-desktop");
  await page.setViewportSize({ width: 390, height: 844 });
  await screenshot("12-dashboard-mobile");
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBeTruthy();
  const apiResult = await platform.request(
    `/stores/${store.id}/accounts/${(await platform.request(`/stores/${store.id}/customers`))[0].account.id}`,
  );
  expect(Number(apiResult.account.used)).toBe(111.8);
  expect(errors).toEqual([]);
});
test("cliente consulta únicamente sus cuentas en dos comercios", async ({
  page,
}) => {
  const credentials = JSON.parse(
    fs.readFileSync(path.join(root, ".local/demo-credentials.json"), "utf8"),
  ).find((entry: any) => entry.email === "juan@fiadoya.test");
  await page.goto("/");
  await page
    .getByLabel("Correo electrónico", { exact: true })
    .fill(credentials.email);
  await page
    .getByLabel("Contraseña", { exact: true })
    .fill(credentials.password);
  await page
    .getByRole("button", { name: "Iniciar sesión", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Cuenta corriente", exact: true }),
  ).toBeVisible();
  await expect(page.locator("#store-switch option")).toHaveCount(2);
  await expect(page.locator("nav button")).toHaveCount(2);
  const originalAccount = await page.locator("#account-select").inputValue();
  await page.locator("#store-switch").selectOption({ index: 1 });
  await expect(page.locator("#account-select")).not.toHaveValue(
    originalAccount,
  );
  await page.screenshot({
    path: path.join(evidence, "13-portal-cliente.png"),
    fullPage: true,
  });
});
test("Swagger expone la API implementada", async ({ page }) => {
  await page.goto("/swagger-ui/index.html");
  await expect(page.locator(".swagger-ui").first()).toBeVisible();
  await expect(page.locator(".opblock").first()).toBeVisible();
  await page.screenshot({
    path: path.join(evidence, "14-swagger.png"),
    fullPage: true,
  });
});

test("administrador de un comercio consulta solo su cuenta como cliente en otro", async ({
  page,
}) => {
  const credentials = JSON.parse(
    fs.readFileSync(path.join(root, ".local/demo-credentials.json"), "utf8"),
  ).find((entry: any) => entry.email === "bodega@fiadoya.test");
  const demo = JSON.parse(
    fs.readFileSync(path.join(root, ".local/demo.json"), "utf8"),
  );
  const platform = new Client();
  await platform.login(
    environment.BOOTSTRAP_EMAIL,
    environment.BOOTSTRAP_PASSWORD,
  );
  const existing = await platform.request(
    `/stores/${demo.stores[1].id}/customers`,
  );
  if (
    !existing.some((entry: any) => entry.customer.userId === credentials.userId)
  ) {
    await platform.request(`/stores/${demo.stores[1].id}/customers`, {
      userId: credentials.userId,
      documentType: "DNI",
      documentNumber: "77999999",
      firstName: "Ana",
      lastName: "Torres",
      email: credentials.email,
      phone: "",
      address: "",
      consent: true,
      active: true,
      terms: existing[0].terms,
    });
  }
  await page.goto("/");
  await page
    .getByLabel("Correo electrónico", { exact: true })
    .fill(credentials.email);
  await page
    .getByLabel("Contraseña", { exact: true })
    .fill(credentials.password);
  await page
    .getByRole("button", { name: "Iniciar sesión", exact: true })
    .click();
  await expect(
    page.getByRole("heading", { name: "Así va tu comercio" }),
  ).toBeVisible();
  await page
    .locator("#store-switch")
    .selectOption({ label: "Salón Nueva Imagen" });
  await expect(
    page.getByRole("heading", { name: "Cuenta corriente", exact: true }),
  ).toBeVisible();
  await expect(page.locator("nav button")).toHaveCount(2);
  await expect(page.locator("#account-select option")).toHaveCount(2);
  await expect(page.locator(".account-heading h3")).toHaveText("Ana Torres");
  await page.screenshot({
    path: path.join(evidence, "17-roles-mixtos.png"),
    fullPage: true,
  });
});
