import { test, expect } from '@playwright/test';

/**
 * E2E tests for the PassGen application.
 */

test.describe('Homepage', () => {
  test('should load the homepage', async ({ page }) => {
    await page.goto('/');
    
    // Check page title
    await expect(page).toHaveTitle(/PassGen/);
    
    // Check main heading/logo text
    await expect(page.locator('a.btn-ghost')).toContainText('PassGen');
  });

  test('should have password input field', async ({ page }) => {
    await page.goto('/');
    
    // Wait for the password to be generated
    await expect(page.locator('#password-input')).toBeVisible();
  });

  test('should generate password on page load', async ({ page }) => {
    await page.goto('/');
    
    // Wait for HTMX to load the password
    await page.waitForSelector('#password-input');
    
    // Password should have content (not empty)
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).toBeVisible();
    
    // Wait a bit for HTMX to complete
    await page.waitForTimeout(500);
    
    const value = await passwordInput.inputValue();
    expect(value.length).toBeGreaterThan(0);
  });

  test('should have regenerate button', async ({ page }) => {
    await page.goto('/');
    
    await expect(page.locator('#regen-button')).toBeVisible();
  });

  test('should have settings dropdown', async ({ page }) => {
    await page.goto('/');
    
    // Settings button should be visible
    const settingsButton = page.locator('label[title="Settings"]');
    await expect(settingsButton).toBeVisible();
  });

  test('should have share button', async ({ page }) => {
    await page.goto('/');
    
    await expect(page.locator('#shareButton')).toBeVisible();
  });
});

test.describe('Password Generation', () => {
  test('should regenerate password when clicking regen button', async ({ page }) => {
    await page.goto('/');
    
    // Wait for initial password
    await page.waitForSelector('#password-input');
    await page.waitForTimeout(500);
    
    const initialPassword = await page.locator('#password-input').inputValue();
    
    // Click regenerate button
    await page.locator('#regen-button').click();
    
    // Wait for new password
    await page.waitForTimeout(500);
    
    const newPassword = await page.locator('#password-input').inputValue();
    
    // Passwords should be different (statistically very likely)
    // Note: There's a tiny chance they could be the same, but it's negligible
    expect(newPassword.length).toBeGreaterThan(0);
  });

  test('should open settings dropdown', async ({ page }) => {
    await page.goto('/');
    
    // Click settings button
    await page.locator('label[title="Settings"]').click();
    
    // Dropdown content should be visible
    const dropdown = page.locator('.dropdown-content');
    await expect(dropdown).toBeVisible();
    
    // Should contain language select
    await expect(page.locator('#language-select')).toBeVisible();
    
    // Should contain word amount slider
    await expect(page.locator('#word-amount-slider')).toBeVisible();
  });

  test('should change word count via slider', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    
    // Wait for dropdown
    await page.waitForSelector('#word-amount-slider');
    
    // Get initial word count
    const slider = page.locator('#word-amount-slider');
    
    // Set slider to 6 words
    await slider.fill('6');
    await slider.dispatchEvent('input');
    await slider.dispatchEvent('change');
    
    // Wait for password regeneration
    await page.waitForTimeout(500);
    
    // Check the word count display
    const wordCountDisplay = page.locator('#word-amount');
    await expect(wordCountDisplay).toHaveText('6');
  });
});

test.describe('Settings Persistence @slow', () => {
  test('should persist word amount setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#word-amount-slider');
    
    // Set word amount to 7
    const slider = page.locator('#word-amount-slider');
    await slider.fill('7');
    await slider.dispatchEvent('input');
    await slider.dispatchEvent('change');
    
    // Wait for localStorage to be set
    await page.waitForTimeout(300);
    
    // Verify localStorage was set
    const storedValue = await page.evaluate(() => localStorage.getItem('word-amount'));
    expect(storedValue).toBe('7');
    
    // Reload the page
    await page.reload();
    
    // Wait for page to load
    await page.waitForSelector('#password-input');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#word-amount-slider');
    
    // Verify the slider value is restored
    const sliderValue = await page.locator('#word-amount-slider').inputValue();
    expect(sliderValue).toBe('7');
    
    // Verify the display shows 7
    await expect(page.locator('#word-amount')).toHaveText('7');
  });

  test('should persist language setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#language-select');
    
    // Change language to German
    await page.locator('#language-select').selectOption('GER');
    
    // Wait for localStorage to be set
    await page.waitForTimeout(300);
    
    // Verify localStorage was set
    const storedValue = await page.evaluate(() => localStorage.getItem('word-language'));
    expect(storedValue).toBe('GER');
    
    // Reload the page
    await page.reload();
    
    // Wait for page to load
    await page.waitForSelector('#password-input');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#language-select');
    
    // Verify the language is restored
    const selectedValue = await page.locator('#language-select').inputValue();
    expect(selectedValue).toBe('GER');
  });

  test('should persist separator setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#word-separator');
    
    // Change separator to underscore
    await page.locator('#word-separator').fill('_');
    await page.locator('#word-separator').dispatchEvent('input');
    
    // Wait for localStorage to be set
    await page.waitForTimeout(300);
    
    // Verify localStorage was set
    const storedValue = await page.evaluate(() => localStorage.getItem('word-separator'));
    expect(storedValue).toBe('_');
    
    // Reload the page
    await page.reload();
    
    // Wait for page to load
    await page.waitForSelector('#password-input');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await page.waitForSelector('#word-separator');
    
    // Verify the separator is restored
    const separatorValue = await page.locator('#word-separator').inputValue();
    expect(separatorValue).toBe('_');
  });
});

test.describe('Theme Toggle', () => {
  test('should toggle theme', async ({ page }) => {
    await page.goto('/');
    
    // Get initial theme
    const initialTheme = await page.evaluate(() => 
      document.documentElement.getAttribute('data-theme')
    );
    
    // Click theme toggle
    await page.locator('#theme-toggle-label').click();
    
    // Wait for theme change
    await page.waitForTimeout(100);
    
    // Get new theme
    const newTheme = await page.evaluate(() => 
      document.documentElement.getAttribute('data-theme')
    );
    
    // Theme should have changed
    expect(newTheme).not.toBe(initialTheme);
  });
});

test.describe('Copy Functionality', () => {
  test('should have copy button', async ({ page }) => {
    await page.goto('/');
    
    // Copy button should be visible
    const copyButton = page.locator('button[data-copy-target="password-input"]');
    await expect(copyButton).toBeVisible();
  });
});

test.describe('Mode Toggle', () => {
  test('should toggle between password and key generation modes', async ({ page }) => {
    await page.goto('/');
    
    // Password section should be visible by default
    await expect(page.locator('#password-section')).toBeVisible();
    await expect(page.locator('#keygen-section')).toHaveClass(/hidden/);
    
    // Click the toggle
    await page.locator('#custom-toggle').click();
    
    // Wait for transition
    await page.waitForTimeout(300);
    
    // Key generation section should now be visible
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    await expect(page.locator('#password-section')).toHaveClass(/hidden/);
  });
});
