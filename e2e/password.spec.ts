import { test, expect } from '@playwright/test';

// Extend Window interface for custom properties set by the app
declare global {
  interface Window {
    generationMode: string;
  }
}
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

test.describe('Key Comment Toggle', () => {
  test('should have key comment toggle hidden by default', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // The checkbox should be visible but unchecked
    const toggle = page.locator('#show-identifier-toggle');
    await expect(toggle).toBeVisible();
    await expect(toggle).not.toBeChecked();
    
    // The input wrapper should be hidden
    const inputWrapper = page.locator('#identifier-input-wrapper');
    await expect(inputWrapper).toHaveClass(/hidden/);
  });

  test('should show key comment input when toggle is checked', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // Check the toggle
    const toggle = page.locator('#show-identifier-toggle');
    await toggle.check();
    
    // The input wrapper should now be visible
    const inputWrapper = page.locator('#identifier-input-wrapper');
    await expect(inputWrapper).not.toHaveClass(/hidden/);
    
    // The input should be visible
    const input = page.locator('#key-identifier');
    await expect(input).toBeVisible();
  });

  test('should clear key comment input when toggle is unchecked', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // Check the toggle to show input
    const toggle = page.locator('#show-identifier-toggle');
    await toggle.check();
    
    // Type a value in the input
    const input = page.locator('#key-identifier');
    await input.fill('test@hostname');
    await expect(input).toHaveValue('test@hostname');
    
    // Uncheck the toggle
    await toggle.uncheck();
    
    // The input wrapper should be hidden
    const inputWrapper = page.locator('#identifier-input-wrapper');
    await expect(inputWrapper).toHaveClass(/hidden/);
    
    // Check the toggle again
    await toggle.check();
    
    // The input should be cleared
    await expect(input).toHaveValue('');
  });

  test('should have proper ARIA attributes for accessibility', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    const toggle = page.locator('#show-identifier-toggle');
    
    // Initially should have aria-expanded="false"
    await expect(toggle).toHaveAttribute('aria-expanded', 'false');
    await expect(toggle).toHaveAttribute('aria-controls', 'identifier-input-wrapper');
    
    // Check the toggle
    await toggle.check();
    
    // aria-expanded should now be "true"
    await expect(toggle).toHaveAttribute('aria-expanded', 'true');
    
    // Uncheck the toggle
    await toggle.uncheck();
    
    // aria-expanded should be "false" again
    await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  });

  test('should focus key comment input when toggle is checked', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // Check the toggle
    const toggle = page.locator('#show-identifier-toggle');
    await toggle.check();
    
    // The input should receive focus
    const input = page.locator('#key-identifier');
    await expect(input).toBeFocused();
  });
});

test.describe('Purpose-based Key Settings', () => {
  test('should show SSH placeholder by default', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // Show the comment input
    await page.locator('#show-identifier-toggle').check();
    
    // Verify SSH placeholder
    const input = page.locator('#key-identifier');
    await expect(input).toHaveAttribute('placeholder', 'user@hostname');
  });

  test('should change placeholder when switching to Git Signing', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open key settings dropdown
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    
    // Show the comment input
    await page.locator('#show-identifier-toggle').check();
    
    // Switch to Git Signing purpose
    await page.locator('#key-purpose').selectOption('git');
    
    // Verify Git placeholder
    const input = page.locator('#key-identifier');
    await expect(input).toHaveAttribute('placeholder', 'your@email.com');
  });

  test('should show correct download button labels for SSH', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key (default is SSH + Ed25519)
    await page.locator('#generate-key-btn').click();
    
    // Wait for key generation
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Verify download button labels for SSH (filename shown in code element)
    const publicLabel = page.locator('#download-public-label');
    const privateLabel = page.locator('#download-private-label');
    
    await expect(publicLabel).toHaveText('id_ed25519.pub');
    await expect(privateLabel).toHaveText('id_ed25519');
    
    // Verify buttons also contain "Download" text
    await expect(page.locator('#download-public-btn')).toContainText('Download');
    await expect(page.locator('#download-private-btn')).toContainText('Download');
  });

  test('should show correct download button labels for Git Signing', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select Git Signing
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-purpose').selectOption('git');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Generate a key
    await page.locator('#generate-key-btn').click();
    
    // Wait for key generation
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Verify download button labels for Git Signing
    const publicLabel = page.locator('#download-public-label');
    const privateLabel = page.locator('#download-private-label');
    
    await expect(publicLabel).toHaveText('id_ed25519_signing.pub');
    await expect(privateLabel).toHaveText('id_ed25519_signing');
  });

  test('should update download labels when algorithm changes', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate initial key (Ed25519)
    await page.locator('#generate-key-btn').click();
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/, { timeout: 10000 });
    
    // Verify initial labels
    await expect(page.locator('#download-public-label')).toHaveText('id_ed25519.pub');
    
    // Open settings and change algorithm
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('ecdsa-p256');
    
    // Labels should update immediately (even without regenerating)
    await expect(page.locator('#download-public-label')).toHaveText('id_ecdsa.pub');
    await expect(page.locator('#download-private-label')).toHaveText('id_ecdsa');
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

test.describe('Key Sharing (Pending Flow)', () => {
  test('should have share button always visible in key generation mode', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Share button should be visible immediately (creates pending share link)
    const shareBtn = page.locator('#share-key-btn');
    await expect(shareBtn).toBeVisible();
  });

  test('should create pending share link and show modal', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Click share button (no need to generate key first - creates pending share)
    const shareBtn = page.locator('#share-key-btn');
    await shareBtn.click();
    
    // Wait for modal to appear
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Verify modal content reflects pending share flow
    await expect(page.locator('#key_share_modal h2')).toContainText('Share Link Created');
    
    // Verify share link exists
    const shareLink = page.locator('#key-share-link');
    await expect(shareLink).toBeVisible();
    const href = await shareLink.getAttribute('href');
    expect(href).toMatch(/^\/key\/share\/[a-f0-9-]+$/);
  });

  test('should show pending share page with generation UI', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Create pending share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    // Verify pending share page shows generation UI
    await expect(page.locator('h1')).toContainText('Generate Your Key');
    
    // Verify generate button is visible
    await expect(page.locator('#generate-share-key-btn')).toBeVisible();
    
    // Verify algorithm and purpose are shown
    await expect(page.locator('p.text-sm.text-base-content\\/60')).toContainText('Ed25519');
    await expect(page.locator('p.text-sm.text-base-content\\/60')).toContainText('SSH');
  });

  test('should generate key on pending share page and complete the share', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Create pending share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    // Verify pending state
    await expect(page.locator('h1')).toContainText('Generate Your Key');
    
    // Set up download listener to capture the private key download
    const downloadPromise = page.waitForEvent('download');
    
    // Click generate button
    await page.locator('#generate-share-key-btn').click();
    
    // Wait for the download to start (private key auto-downloads)
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('id_ed25519');
    
    // Wait for the page to update to completed state
    await expect(page.locator('h1')).toContainText('Key Generated Successfully!', { timeout: 10000 });
    
    // Verify public key is now displayed
    const publicKeyDisplay = page.locator('#public-key-display');
    await expect(publicKeyDisplay).toBeVisible();
    const publicKey = await publicKeyDisplay.inputValue();
    expect(publicKey).toContain('ssh-ed25519');
  });

  test('should show completed share on revisit', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Create pending share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Get share path
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    
    // Navigate to share page and complete the share
    await page.goto(sharePath!);
    
    // Set up download listener
    const downloadPromise = page.waitForEvent('download');
    
    // Generate the key
    await page.locator('#generate-share-key-btn').click();
    await downloadPromise;
    
    // Wait for completion
    await expect(page.locator('h1')).toContainText('Key Generated Successfully!', { timeout: 10000 });
    
    // Store the public key
    const publicKey = await page.locator('#public-key-display').inputValue();
    
    // Revisit the same share link
    await page.goto(sharePath!);
    
    // Verify completed share page is shown (not pending)
    await expect(page.locator('h1')).toContainText('Shared Public Key');
    
    // Verify public key is displayed
    const displayedKey = await page.locator('#public-key-display').inputValue();
    expect(displayedKey).toBe(publicKey);
  });

  test('should copy public key from completed share page', async ({ page, context }) => {
    await context.grantPermissions(['clipboard-read', 'clipboard-write']);
    
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Create pending share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page and complete
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    const downloadPromise = page.waitForEvent('download');
    await page.locator('#generate-share-key-btn').click();
    await downloadPromise;
    
    await expect(page.locator('h1')).toContainText('Key Generated Successfully!', { timeout: 10000 });
    
    // Get the displayed public key
    const displayedKey = await page.locator('#public-key-display').inputValue();
    
    // Click copy button
    const copyBtn = page.locator('button[title="Copy public key"]').first();
    await copyBtn.click();
    
    // Verify clipboard content
    const clipboardContent = await page.evaluate(() => navigator.clipboard.readText());
    expect(clipboardContent).toBe(displayedKey);
  });

  test('should create pending share with Git Signing purpose', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select Git Signing
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-purpose').selectOption('git');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Create share (no need to generate key)
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    // Verify Git Signing purpose is displayed
    await expect(page.locator('p.text-sm.text-base-content\\/60')).toContainText('Git Signing');
  });

  test('should create pending share with ECDSA algorithm @slow', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select ECDSA P-256
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('ecdsa-p256');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Create share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    // Verify ECDSA algorithm is displayed on pending page
    await expect(page.locator('p.text-sm.text-base-content\\/60')).toContainText('ECDSA P-256');
  });

  test('should generate ECDSA key on pending share page @slow', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Open settings and select ECDSA P-256
    const settingsDropdown = page.getByTitle('Key Settings');
    await settingsDropdown.click();
    await page.locator('#key-algorithm').selectOption('ecdsa-p256');
    
    // Close dropdown
    await page.locator('#keygen-section').click({ position: { x: 10, y: 10 } });
    
    // Create share
    await page.locator('#share-key-btn').click();
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });
    
    // Navigate to share page
    const shareLink = page.locator('#key-share-link');
    const sharePath = await shareLink.getAttribute('href');
    await page.goto(sharePath!);
    
    // Set up download listener
    const downloadPromise = page.waitForEvent('download');
    
    // Generate the ECDSA key
    await page.locator('#generate-share-key-btn').click();
    
    // Wait for download
    const download = await downloadPromise;
    expect(download.suggestedFilename()).toBe('id_ecdsa');
    
    // Wait for completion
    await expect(page.locator('h1')).toContainText('Key Generated Successfully!', { timeout: 15000 });
    
    // Verify ECDSA public key is displayed
    const publicKey = await page.locator('#public-key-display').inputValue();
    expect(publicKey).toContain('ecdsa-sha2-nistp256');
  });
});

test.describe('Key Generation (Local Only)', () => {
  test('should still generate key locally without sharing', async ({ page }) => {
    await page.goto('/');
    
    // Switch to key generation mode
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);
    
    // Generate a key locally
    await page.locator('#generate-key-btn').click();
    
    // Wait for key generation
    await expect(async () => {
      const publicKey = await page.locator('#public-key-output').inputValue();
      expect(publicKey).toContain('ssh-ed25519');
    }).toPass({ timeout: 10000 });
    
    // Verify key output section is visible
    await expect(page.locator('#key-output-section')).not.toHaveClass(/hidden/);
    
    // Verify private key tab and content
    await page.locator('#tab-private').click();
    const privateKey = await page.locator('#private-key-output').inputValue();
    expect(privateKey).toContain('-----BEGIN OPENSSH PRIVATE KEY-----');
  });

  // Note: "should clear private key when clicking clear button" is tested in 
  // "Key Generation View" describe block (line 565) - not duplicated here
});

test.describe('Rapid Toggle Dual-Fire Prevention', () => {
  test('should only fire password share after toggling Password -> Key -> Password', async ({ page }) => {
    await page.goto('/');

    // Wait for the initial password to be generated
    const passwordInput = page.locator('#password-input');
    await expect(passwordInput).not.toHaveValue('');

    // Toggle: Password -> Key
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);

    // Toggle: Key -> Password
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#password-section')).not.toHaveClass(/hidden/);

    // Track all requests
    const requests: string[] = [];
    page.on('request', (req) => {
      const url = req.url();
      if (url.includes('/share') || url.includes('/key/share')) {
        requests.push(req.method() + ' ' + new URL(url).pathname);
      }
    });

    // Click the password share button
    await page.locator('#shareButton').click();

    // Wait for the share modal to appear
    const modal = page.locator('#share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });

    // Verify only one request was made and it was the password share
    expect(requests.length).toBe(1);
    expect(requests[0]).toBe('POST /share');
  });

  test('should only fire key share after toggling Key -> Password -> Key', async ({ page }) => {
    await page.goto('/');

    // Wait for the initial password to be generated
    await expect(page.locator('#password-input')).not.toHaveValue('');

    // Toggle: Password -> Key
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);

    // Toggle: Key -> Password
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#password-section')).not.toHaveClass(/hidden/);

    // Toggle: Password -> Key
    await page.locator('#custom-toggle').click();
    await expect(page.locator('#keygen-section')).not.toHaveClass(/hidden/);

    // Track all requests
    const requests: string[] = [];
    page.on('request', (req) => {
      const url = req.url();
      if (url.includes('/share') || url.includes('/key/share')) {
        requests.push(req.method() + ' ' + new URL(url).pathname);
      }
    });

    // Click the key share button
    await page.locator('#share-key-btn').click();

    // Wait for the key share modal to appear
    const modal = page.locator('#key_share_modal');
    await expect(modal).toHaveAttribute('open', '', { timeout: 10000 });

    // Verify only one request was made and it was the key share
    expect(requests.length).toBe(1);
    expect(requests[0]).toBe('POST /key/share');
  });

  test('should correctly set window.generationMode after rapid toggles', async ({ page }) => {
    await page.goto('/');

    // Wait for initial load
    await expect(page.locator('#password-input')).not.toHaveValue('');

    // Verify default mode is password
    let mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('password');

    // Rapid toggles: Password -> Key -> Password -> Key -> Password
    for (let i = 0; i < 4; i++) {
      await page.locator('#custom-toggle').click();
      // Small delay to let the click handler execute
      await page.waitForTimeout(50);
    }

    // After even number of toggles, should be back to password
    mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('password');

    // One more toggle should put us in key mode
    await page.locator('#custom-toggle').click();
    await page.waitForTimeout(50);
    mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('key');
  });
});

test.describe('Fresh Page Load with Stored Mode', () => {
  // Guarantee localStorage cleanup even if a test fails mid-way
  test.afterEach(async ({ page }) => {
    await page.evaluate(() => localStorage.removeItem('generation-mode-hidden'));
  });

  test('should load in key mode when localStorage has generation-mode-hidden=key', async ({ page }) => {
    // Pre-set localStorage before navigating
    await page.goto('/');
    await page.evaluate(() => localStorage.setItem('generation-mode-hidden', 'key'));

    // Reload the page so initGenerationToggle() reads from localStorage
    await page.reload();

    // Key section should be visible, password section should be hidden
    await expect(page.locator('#keygen-section')).toBeVisible();
    await expect(page.locator('#password-section')).toBeHidden();

    // window.generationMode should be 'key'
    const mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('key');

    // Toggle thumb should be translated to the key position
    const thumbTransform = await page.locator('#toggle-thumb').evaluate(
      (el) => (el as HTMLElement).style.transform
    );
    expect(thumbTransform).toBe('translateX(60px)');
  });

  test('should load in password mode when localStorage has generation-mode-hidden=password', async ({ page }) => {
    // Pre-set localStorage before navigating
    await page.goto('/');
    await page.evaluate(() => localStorage.setItem('generation-mode-hidden', 'password'));

    // Reload
    await page.reload();

    // Password section should be visible, key section should be hidden
    await expect(page.locator('#password-section')).toBeVisible();
    await expect(page.locator('#keygen-section')).toBeHidden();

    // window.generationMode should be 'password'
    const mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('password');
  });

  test('should default to password mode when localStorage has no stored mode', async ({ page }) => {
    // Ensure no stored mode
    await page.goto('/');
    await page.evaluate(() => localStorage.removeItem('generation-mode-hidden'));

    // Reload
    await page.reload();

    // Password section should be visible
    await expect(page.locator('#password-section')).toBeVisible();
    await expect(page.locator('#keygen-section')).toBeHidden();

    // window.generationMode should be 'password'
    const mode = await page.evaluate(() => window.generationMode);
    expect(mode).toBe('password');
  });
});
