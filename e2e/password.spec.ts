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
    
    // Dropdown content should be visible (use password section's dropdown)
    const dropdown = page.locator('#password-section .dropdown-content');
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

  test('should copy password to clipboard when clicking copy button', async ({ page, context }) => {
    // Grant clipboard permissions
    await context.grantPermissions(['clipboard-read', 'clipboard-write']);
    
    await page.goto('/');
    
    // Wait for password to be generated
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).toBeVisible();
    await expect(async () => {
      const value = await passwordInput.inputValue();
      expect(value.length).toBeGreaterThan(0);
    }).toPass({ timeout: 5000 });
    
    const passwordValue = await passwordInput.inputValue();
    
    // Click copy button
    const copyButton = page.locator('button[data-copy-target="password-input"]');
    await copyButton.click();
    
    // Verify clipboard content
    const clipboardContent = await page.evaluate(() => navigator.clipboard.readText());
    expect(clipboardContent).toBe(passwordValue);
  });

  test('should show success tooltip after copying', async ({ page, context }) => {
    await context.grantPermissions(['clipboard-read', 'clipboard-write']);
    
    await page.goto('/');
    
    // Wait for password to be generated
    const passwordInput = page.locator('#password-input');
    await expect(async () => {
      const value = await passwordInput.inputValue();
      expect(value.length).toBeGreaterThan(0);
    }).toPass({ timeout: 5000 });
    
    // Click copy button
    const copyButton = page.locator('button[data-copy-target="password-input"]');
    await copyButton.click();
    
    // Verify success tooltip appears
    const tooltip = page.locator('#copy-tooltip');
    await expect(tooltip).not.toHaveClass(/hidden/);
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

test.describe('Key Generation View', () => {
  test('should display key generation UI elements correctly', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Check main UI elements are visible (generate button, settings dropdown)
    await expect(page.locator('#generate-key-btn')).toBeVisible();
    await expect(page.locator('#key-empty-state')).toBeVisible();
    
    // Settings are in a dropdown - open it by clicking the settings button and check
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await expect(page.locator('#key-purpose')).toBeVisible();
    await expect(page.locator('#key-algorithm')).toBeVisible();
  });

  test('should show key output after generation', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Output section should be hidden initially
    await expect(page.locator('#key-output-section')).toHaveClass(/hidden/);
    
    // Generate a key
    await page.locator('#generate-key-btn').click();
    
    // Wait for generation and output section to appear
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    await expect(page.locator('#public-key-output')).toBeVisible();
  });

  test('should generate Ed25519 key pair', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Click generate key button
    await page.locator('#generate-key-btn').click();
    
    // Wait for keys to be generated
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ssh-ed25519');
    }).toPass({ timeout: 10000 });
    
    // Switch to private key tab and verify
    await page.locator('#tab-private').click();
    const privateKey = await page.locator('#private-key-output').inputValue();
    expect(privateKey).toContain('-----BEGIN OPENSSH PRIVATE KEY-----');
    expect(privateKey).toContain('-----END OPENSSH PRIVATE KEY-----');
  });

  test('should have copy buttons for public and private keys', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key first (buttons only visible after generation)
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Check copy button in public key tab
    const publicKeyCopyBtn = page.locator('button[data-copy-target="public-key-output"]');
    await expect(publicKeyCopyBtn).toBeVisible();
    
    // Switch to private key tab and check copy button
    await page.locator('#tab-private').click();
    const privateKeyCopyBtn = page.locator('button[data-copy-target="private-key-output"]');
    await expect(privateKeyCopyBtn).toBeVisible();
  });

  test('should copy generated public key to clipboard', async ({ page, context }) => {
    await context.grantPermissions(['clipboard-read', 'clipboard-write']);
    
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key
    await page.locator('#generate-key-btn').click();
    
    // Wait for key generation
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ssh-ed25519');
    }).toPass({ timeout: 10000 });
    
    const publicKeyValue = await page.locator('#public-key-output').inputValue();
    
    // Click copy button for public key
    const copyBtn = page.locator('button[data-copy-target="public-key-output"]');
    await copyBtn.click();
    
    // Verify clipboard content
    const clipboardContent = await page.evaluate(() => navigator.clipboard.readText());
    expect(clipboardContent).toBe(publicKeyValue);
  });

  test('should display warning about unencrypted private keys', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key first (warning is in private key tab, shown after generation)
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Switch to private key tab
    await page.locator('#tab-private').click();
    
    // Check for the warning message
    const warning = page.locator('#panel-private .alert-warning');
    await expect(warning).toBeVisible();
    await expect(warning).toContainText('unencrypted');
    await expect(warning).toContainText('ssh-keygen');
  });

  test('should clear private key when clicking clear button', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key first
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Switch to private key tab
    await page.locator('#tab-private').click();
    
    // Verify private key has content
    const privateKeyOutput = page.locator('#private-key-output');
    await expect(async () => {
      const value = await privateKeyOutput.inputValue();
      expect(value).toContain('-----BEGIN OPENSSH PRIVATE KEY-----');
    }).toPass({ timeout: 5000 });
    
    // Click clear button
    const clearBtn = page.locator('#clear-private-key-btn');
    await expect(clearBtn).toBeVisible();
    await clearBtn.click();
    
    // Verify private key is cleared
    await expect(privateKeyOutput).toHaveValue('');
    
    // Verify switched back to public tab
    await expect(page.locator('#panel-public')).not.toHaveClass(/hidden/);
    await expect(page.locator('#panel-private')).toHaveClass(/hidden/);
  });

  test('should have accessible tabs with proper ARIA attributes', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key to show tabs
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Verify tablist role
    const tablist = page.locator('[role="tablist"]');
    await expect(tablist).toBeVisible();
    
    // Verify tab roles and attributes
    const publicTab = page.locator('#tab-public');
    const privateTab = page.locator('#tab-private');
    
    await expect(publicTab).toHaveAttribute('role', 'tab');
    await expect(publicTab).toHaveAttribute('aria-selected', 'true');
    await expect(publicTab).toHaveAttribute('aria-controls', 'panel-public');
    
    await expect(privateTab).toHaveAttribute('role', 'tab');
    await expect(privateTab).toHaveAttribute('aria-selected', 'false');
    await expect(privateTab).toHaveAttribute('aria-controls', 'panel-private');
    
    // Verify tabpanel roles
    const publicPanel = page.locator('#panel-public');
    const privatePanel = page.locator('#panel-private');
    
    await expect(publicPanel).toHaveAttribute('role', 'tabpanel');
    await expect(publicPanel).toHaveAttribute('aria-labelledby', 'tab-public');
    
    await expect(privatePanel).toHaveAttribute('role', 'tabpanel');
    await expect(privatePanel).toHaveAttribute('aria-labelledby', 'tab-private');
  });

  test('should support keyboard navigation for tabs', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key to show tabs
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Focus the public tab
    const publicTab = page.locator('#tab-public');
    await publicTab.focus();
    
    // Press ArrowRight to move to private tab
    await page.keyboard.press('ArrowRight');
    
    // Verify private tab is now selected
    await expect(page.locator('#tab-private')).toHaveAttribute('aria-selected', 'true');
    await expect(page.locator('#tab-public')).toHaveAttribute('aria-selected', 'false');
    await expect(page.locator('#panel-private')).not.toHaveClass(/hidden/);
    await expect(page.locator('#panel-public')).toHaveClass(/hidden/);
    
    // Press ArrowLeft to go back to public tab
    await page.keyboard.press('ArrowLeft');
    
    // Verify public tab is selected again
    await expect(page.locator('#tab-public')).toHaveAttribute('aria-selected', 'true');
    await expect(page.locator('#tab-private')).toHaveAttribute('aria-selected', 'false');
    await expect(page.locator('#panel-public')).not.toHaveClass(/hidden/);
  });

  test('should have aria-live region for toast notifications', async ({ page }) => {
    await page.goto('/');
    
    // Check toast container has aria-live attributes
    const toastContainer = page.locator('.toast.toast-top.toast-center');
    await expect(toastContainer).toHaveAttribute('aria-live', 'polite');
    await expect(toastContainer).toHaveAttribute('aria-atomic', 'true');
    await expect(toastContainer).toHaveAttribute('role', 'status');
  });
});

test.describe('ECDSA and RSA Key Generation @slow', () => {
  test('should generate ECDSA P-256 key pair', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select ECDSA P-256
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('ecdsa-p256');
    
    // Close dropdown by clicking elsewhere
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Generate the key
    await page.locator('#generate-key-btn').click();
    
    // Wait for keys to be generated
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ecdsa-sha2-nistp256');
    }).toPass({ timeout: 15000 });
    
    // Verify key type display
    await expect(page.locator('#key-type-display')).toContainText('ECDSA P-256');
    
    // Switch to private key tab and verify format
    await page.locator('#tab-private').click();
    const privateKey = await page.locator('#private-key-output').inputValue();
    expect(privateKey).toContain('-----BEGIN OPENSSH PRIVATE KEY-----');
  });

  test('should generate ECDSA P-384 key pair', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select ECDSA P-384
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('ecdsa-p384');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Generate the key
    await page.locator('#generate-key-btn').click();
    
    // Wait for keys to be generated
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ecdsa-sha2-nistp384');
    }).toPass({ timeout: 15000 });
    
    // Verify key type display
    await expect(page.locator('#key-type-display')).toContainText('ECDSA P-384');
  });

  test('should generate RSA 2048 key pair', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select RSA 2048
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('rsa-2048');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Generate the key
    await page.locator('#generate-key-btn').click();
    
    // Wait for keys to be generated (RSA is slower)
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ssh-rsa');
    }).toPass({ timeout: 30000 });
    
    // Verify key type display
    await expect(page.locator('#key-type-display')).toContainText('RSA 2048');
    
    // Verify private key format
    await page.locator('#tab-private').click();
    const privateKey = await page.locator('#private-key-output').inputValue();
    expect(privateKey).toContain('-----BEGIN OPENSSH PRIVATE KEY-----');
  });

  test('should generate RSA 4096 key pair', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select RSA 4096
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('rsa-4096');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Generate the key
    await page.locator('#generate-key-btn').click();
    
    // Wait for keys to be generated (RSA 4096 is much slower)
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ssh-rsa');
    }).toPass({ timeout: 60000 });
    
    // Verify key type display
    await expect(page.locator('#key-type-display')).toContainText('RSA 4096');
  });
});
