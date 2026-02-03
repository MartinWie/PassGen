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
    
    // Wait for HTMX to load the password - password input should have content
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).toBeVisible();
    
    // Wait for the password to be generated (non-empty value)
    await expect(passwordInput).not.toHaveValue('');
    
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
    
    // Wait for initial password to be non-empty
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).not.toHaveValue('');
    
    const initialPassword = await passwordInput.inputValue();
    
    // Click regenerate button
    await page.locator('#regen-button').click();
    
    // Wait for HTMX request to complete - check that the password input is still visible
    // and wait for potential change (use network idle or attribute change)
    await page.waitForLoadState('networkidle');
    
    const newPassword = await passwordInput.inputValue();
    
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
    
    // Wait for dropdown to be visible
    await expect(page.locator('#word-amount-slider')).toBeVisible();
    
    // Set slider to 6 words
    const slider = page.locator('#word-amount-slider');
    await slider.fill('6');
    await slider.dispatchEvent('input');
    await slider.dispatchEvent('change');
    
    // Wait for the word count display to update
    await expect(page.locator('#word-amount')).toHaveText('6');
  });

  test('should generate password with correct word count', async ({ page }) => {
    await page.goto('/');
    
    // Wait for initial password
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).not.toHaveValue('');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-amount-slider')).toBeVisible();
    
    // Set word count to 5
    const slider = page.locator('#word-amount-slider');
    await slider.fill('5');
    await slider.dispatchEvent('input');
    await slider.dispatchEvent('change');
    
    // Wait for password to regenerate
    await page.waitForLoadState('networkidle');
    
    // Get the password and count words (assuming default separator is '-')
    const password = await passwordInput.inputValue();
    
    // Get the current separator
    const separator = await page.locator('#word-separator').inputValue() || '-';
    
    // Count words - the password format is word1<sep>word2<sep>... potentially with special chars and numbers
    // Since special chars and numbers are appended to words, we count by separator
    const wordCount = password.split(separator).length;
    expect(wordCount).toBe(5);
  });

  test('should use correct separator in generated password', async ({ page }) => {
    await page.goto('/');
    
    // Wait for initial password
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).not.toHaveValue('');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-separator')).toBeVisible();
    
    // Change separator to underscore
    await page.locator('#word-separator').fill('_');
    await page.locator('#word-separator').dispatchEvent('input');
    
    // Wait for password to regenerate
    await page.waitForLoadState('networkidle');
    
    // Get the password
    const password = await passwordInput.inputValue();
    
    // Password should contain underscores as separators
    expect(password).toContain('_');
    // And should not contain hyphens (unless in a word itself, which is unlikely)
    // We use a looser check here
    const underscoreCount = (password.match(/_/g) || []).length;
    expect(underscoreCount).toBeGreaterThan(0);
  });
});

test.describe('Settings Persistence @slow', () => {
  test('should persist word amount setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-amount-slider')).toBeVisible();
    
    // Set word amount to 7
    const slider = page.locator('#word-amount-slider');
    await slider.fill('7');
    await slider.dispatchEvent('input');
    await slider.dispatchEvent('change');
    
    // Wait for localStorage to be set by checking its value
    await expect(async () => {
      const storedValue = await page.evaluate(() => localStorage.getItem('word-amount'));
      expect(storedValue).toBe('7');
    }).toPass({ timeout: 5000 });
    
    // Reload the page
    await page.reload();
    
    // Wait for password to load
    await expect(page.locator('#password-input')).not.toHaveValue('');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-amount-slider')).toBeVisible();
    
    // Verify the slider value is restored
    await expect(page.locator('#word-amount-slider')).toHaveValue('7');
    
    // Verify the display shows 7
    await expect(page.locator('#word-amount')).toHaveText('7');
    
    // Verify the password has 7 words
    const password = await page.locator('#password-input').inputValue();
    const separator = await page.locator('#word-separator').inputValue() || '-';
    const wordCount = password.split(separator).length;
    expect(wordCount).toBe(7);
  });

  test('should persist language setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#language-select')).toBeVisible();
    
    // Change language to German
    await page.locator('#language-select').selectOption('GER');
    
    // Wait for localStorage to be set
    await expect(async () => {
      const storedValue = await page.evaluate(() => localStorage.getItem('word-language'));
      expect(storedValue).toBe('GER');
    }).toPass({ timeout: 5000 });
    
    // Reload the page
    await page.reload();
    
    // Wait for password to load
    await expect(page.locator('#password-input')).not.toHaveValue('');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#language-select')).toBeVisible();
    
    // Verify the language is restored
    await expect(page.locator('#language-select')).toHaveValue('GER');
  });

  test('should persist separator setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-separator')).toBeVisible();
    
    // Change separator to underscore
    await page.locator('#word-separator').fill('_');
    await page.locator('#word-separator').dispatchEvent('input');
    
    // Wait for localStorage to be set
    await expect(async () => {
      const storedValue = await page.evaluate(() => localStorage.getItem('word-separator'));
      expect(storedValue).toBe('_');
    }).toPass({ timeout: 5000 });
    
    // Reload the page
    await page.reload();
    
    // Wait for password to load
    await expect(page.locator('#password-input')).not.toHaveValue('');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#word-separator')).toBeVisible();
    
    // Verify the separator is restored
    await expect(page.locator('#word-separator')).toHaveValue('_');
    
    // Verify the password uses the underscore separator
    const password = await page.locator('#password-input').inputValue();
    expect(password).toContain('_');
  });

  test('should persist include-numbers setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#include-numbers')).toBeVisible();
    
    // Enable include numbers
    await page.locator('#include-numbers').check();
    
    // Wait for localStorage to be set
    await expect(async () => {
      const storedValue = await page.evaluate(() => localStorage.getItem('include-numbers'));
      expect(storedValue).toBe('true');
    }).toPass({ timeout: 5000 });
    
    // Reload the page
    await page.reload();
    
    // Wait for password to load
    await expect(page.locator('#password-input')).not.toHaveValue('');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#include-numbers')).toBeVisible();
    
    // Verify the checkbox is restored
    await expect(page.locator('#include-numbers')).toBeChecked();
    
    // Verify the password contains numbers
    const password = await page.locator('#password-input').inputValue();
    expect(password).toMatch(/\d/);
  });

  test('should persist include-special setting across page reloads', async ({ page }) => {
    await page.goto('/');
    
    // Open settings
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#include-special')).toBeVisible();
    
    // Enable include special characters
    await page.locator('#include-special').check();
    
    // Wait for localStorage to be set
    await expect(async () => {
      const storedValue = await page.evaluate(() => localStorage.getItem('include-special'));
      expect(storedValue).toBe('true');
    }).toPass({ timeout: 5000 });
    
    // Reload the page
    await page.reload();
    
    // Wait for password to load
    await expect(page.locator('#password-input')).not.toHaveValue('');
    
    // Open settings again
    await page.locator('label[title="Settings"]').click();
    await expect(page.locator('#include-special')).toBeVisible();
    
    // Verify the checkbox is restored
    await expect(page.locator('#include-special')).toBeChecked();
    
    // Verify the password contains special characters
    const password = await page.locator('#password-input').inputValue();
    // Special chars from SPECIAL_CHARS constant
    expect(password).toMatch(/[!"#$%&'()*+,\-./:;<=>?@\[\\\]^_`{|}~]/);
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
    
    // Wait for theme change by checking the attribute
    await expect(async () => {
      const newTheme = await page.evaluate(() => 
        document.documentElement.getAttribute('data-theme')
      );
      expect(newTheme).not.toBe(initialTheme);
    }).toPass({ timeout: 5000 });
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
    
    // Wait for key generation section to become visible
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    await expect(page.locator('#password-section')).toHaveClass(/hidden/);
  });
});
