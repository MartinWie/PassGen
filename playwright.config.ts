import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for PassGen E2E tests.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  /* Start the dev server before running tests.
   * Rate limits are raised so that parallel E2E tests don't exhaust the
   * per-IP token buckets (production defaults are much lower). */
  webServer: {
    command: './gradlew run',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120000, // 2 minutes for Gradle to compile and start
    env: {
      RATE_LIMIT_CREATE_SHARE: '500',
      RATE_LIMIT_COMPLETE_SHARE: '500',
      RATE_LIMIT_VIEW_SHARE: '500',
      RATE_LIMIT_GENERATE: '5000',
      RATE_LIMIT_REFILL_SECONDS: '60',
    },
  },
});
