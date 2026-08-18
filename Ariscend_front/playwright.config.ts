import path from "node:path";
import { defineConfig, devices } from "@playwright/test";

const backendUrl = "http://127.0.0.1:18080";
const frontendUrl = "http://127.0.0.1:3100";
const backendCommand = process.platform === "win32"
  ? ".\\gradlew.bat bootE2e --no-daemon"
  : "./gradlew bootE2e --no-daemon";

export default defineConfig({
  testDir: "./tests/e2e",
  outputDir: "test-results",
  fullyParallel: false,
  workers: 1,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [
    ["list"],
    ["html", { outputFolder: "playwright-report", open: "never" }],
  ],
  use: {
    baseURL: frontendUrl,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: [
    {
      command: backendCommand,
      cwd: path.resolve(__dirname, "../Ariscend_back"),
      url: `${backendUrl}/api/auth/providers`,
      timeout: 120_000,
      reuseExistingServer: false,
    },
    {
      command: "npm run dev -- --hostname 127.0.0.1 --port 3100",
      cwd: __dirname,
      env: { BACKEND_URL: backendUrl },
      url: `${frontendUrl}/login`,
      timeout: 120_000,
      reuseExistingServer: false,
    },
  ],
});
