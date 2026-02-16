function clearPrivateKey(showToast) {
    // Default to showing toast unless explicitly suppressed (e.g. navigation events)
    if (showToast === undefined) showToast = true;
    const privOut = document.getElementById('private-key-output');
    const tabPublic = document.getElementById('tab-public');
    const tabPrivate = document.getElementById('tab-private');
    const panelPublic = document.getElementById('panel-public');
    const panelPrivate = document.getElementById('panel-private');

    if (privOut) {
        // Overwrite with spaces first (best-effort memory clearing)
        const len = privOut.value.length;
        privOut.value = ' '.repeat(len);
        privOut.value = '';
    }

    // Switch back to public tab
    if (tabPublic && tabPrivate && panelPublic && panelPrivate) {
        tabPublic.classList.add('border-primary', 'bg-base-100');
        tabPublic.classList.remove('border-transparent');
        tabPublic.setAttribute('aria-selected', 'true');
        tabPublic.setAttribute('tabindex', '0');
        tabPrivate.classList.remove('border-primary', 'bg-base-100');
        tabPrivate.classList.add('border-transparent');
        tabPrivate.setAttribute('aria-selected', 'false');
        tabPrivate.setAttribute('tabindex', '-1');
        panelPublic.classList.remove('hidden');
        panelPrivate.classList.add('hidden');
    }

    // Show a toast notification (skip during navigation/unload — user won't see it)
    if (showToast) {
        const tooltip = document.getElementById('copy-tooltip');
        if (tooltip) {
            const span = tooltip.querySelector('span');
            const originalText = span ? span.textContent : '';
            if (span) span.textContent = 'Private key cleared';
            removeHideThenFadeout(tooltip);
            // Restore original text after toast fades
            setTimeout(() => {
                if (span) span.textContent = originalText;
            }, 2500);
        }
    }
}

// Fallback copy method for browsers without clipboard API or insecure contexts
function fallbackCopyToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    // Avoid scrolling to bottom
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    let success = false;
    try {
        success = document.execCommand('copy');
    } catch (err) {
        console.error('Fallback copy failed:', err);
    }

    document.body.removeChild(textArea);
    return success;
}

/**
 * Show the copy-success or copy-failed tooltip.
 * @param {boolean} success - true for success tooltip, false for failure.
 */
function showTooltip(success) {
    const id = success ? 'copy-tooltip' : 'copy-tooltip-failed';
    const tooltip = document.getElementById(id);
    if (tooltip) removeHideThenFadeout(tooltip);
}

/**
 * Copy text to the clipboard using the modern API with a legacy fallback.
 * Shows the appropriate success/failure tooltip automatically.
 * @param {string} text - The text to copy.
 */
function writeToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(
            () => showTooltip(true),
            (err) => {
                console.warn('Clipboard API failed, trying fallback:', err);
                showTooltip(fallbackCopyToClipboard(text));
            },
        );
    } else {
        if (fallbackCopyToClipboard(text)) {
            showTooltip(true);
        } else {
            console.error('Failed to copy to clipboard');
            showTooltip(false);
        }
    }
}

/**
 * Download text content as a file.
 * Ensures a trailing newline (POSIX convention for SSH key files).
 * @param {string} text - File content.
 * @param {string} filename - Name for the downloaded file.
 * @param {string} [mime='application/octet-stream'] - MIME type.
 */
function downloadAsFile(text, filename, mime) {
    mime = mime || 'application/octet-stream';
    const needsNewline = text.endsWith('\n') ? '' : '\n';
    const blob = new Blob([text + needsNewline], {type: mime});
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
        URL.revokeObjectURL(a.href);
        a.remove();
    }, 1500);
}

function copyToClipboard(elementId) {
    const element = document.getElementById(elementId);
    if (!element) {
        console.error('Copy target element not found:', elementId);
        showTooltip(false);
        return;
    }
    let textToCopy;

    // Check if element is an input or textarea
    if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
        element.select();
        element.setSelectionRange(0, 99999);
        textToCopy = element.value;
    } else {
        // For other elements like p, div, etc.
        textToCopy = element.textContent || element.innerText;
    }

    writeToClipboard(textToCopy);
}

function copyShareUrl() {
    // Get the fully-resolved URL from the hidden share link anchor
    const link = document.getElementById('password-share-link');
    const shareUrl = link?.href;

    if (!shareUrl) {
        console.error('No share URL found');
        showTooltip(false);
        return;
    }

    writeToClipboard(shareUrl);
}

function copyKeyShareUrl() {
    // Get the URL from the key share modal link.
    // Using link.href (the resolved property) instead of getAttribute('href')
    // avoids double-origin bugs with manual URL construction.
    const link = document.getElementById('key-share-link');
    if (!link) {
        console.error('No key share link found');
        showTooltip(false);
        return;
    }
    const fullUrl = link.href;
    writeToClipboard(fullUrl);
}

// Track per-element timers to prevent stacking timeouts on rapid calls
const _tooltipTimers = new WeakMap();

function removeHideThenFadeout(element) {
    if (!element) return;

    // Cancel any in-flight show/fade timers for this element
    const prev = _tooltipTimers.get(element);
    if (prev) {
        if (prev.show) clearTimeout(prev.show);
        if (prev.fade) clearTimeout(prev.fade);
    }

    // Show element: remove invisible/opacity-0, add opacity-1
    element.classList.remove('invisible', 'opacity-0');
    element.classList.add('opacity-100');

    // Fade out tooltip after 2 seconds
    const show = setTimeout(() => {
        // Start the fade out transition
        element.classList.remove('opacity-100');
        element.classList.add('opacity-0');

        // Wait for transition to complete before hiding
        const fade = setTimeout(() => {
            element.classList.add('invisible');
            _tooltipTimers.delete(element);
        }, 300); // Match this with CSS transition duration
        _tooltipTimers.set(element, {show: null, fade});
    }, 2000);
    _tooltipTimers.set(element, {show, fade: null});
}

/**
 * Restore landing page settings from localStorage on load.
 * Handles: word-language, word-amount, word-separator,
 * include-numbers, include-special checkboxes, and triggers the initial
 * password generation once settings are applied.
 *
 * No-op on pages that don't have the relevant form elements.
 */
function initLandingPageSettings() {
    // Language select
    const languageSelect = document.getElementById('language-select');
    if (languageSelect) {
        const savedLanguage = localStorage.getItem('word-language');
        languageSelect.value = savedLanguage || 'ENG';
        if (!savedLanguage) localStorage.setItem('word-language', 'ENG');
    }

    // Word amount (slider + number input + display span)
    const wordAmountSlider = document.getElementById('word-amount-slider');
    const wordAmountSpan = document.getElementById('word-amount');
    const wordInput = document.getElementById('word-input');
    if (wordAmountSlider && wordAmountSpan) {
        const savedWordAmount = localStorage.getItem('word-amount');
        const amount = savedWordAmount || '4';
        wordAmountSlider.value = amount;
        wordAmountSpan.textContent = amount;
        if (wordInput) wordInput.value = amount;
        if (!savedWordAmount) localStorage.setItem('word-amount', '4');
    }

    // Word separator
    const wordSeparator = document.getElementById('word-separator');
    if (wordSeparator) {
        const savedSeparator = localStorage.getItem('word-separator');
        wordSeparator.value = savedSeparator || '-';
        if (!savedSeparator) localStorage.setItem('word-separator', '-');
    }

    // Include numbers checkbox
    const includeNumbers = document.getElementById('include-numbers');
    if (includeNumbers) {
        includeNumbers.checked = localStorage.getItem('include-numbers') === 'true';
    }

    // Include special characters checkbox
    const includeSpecial = document.getElementById('include-special');
    if (includeSpecial) {
        includeSpecial.checked = localStorage.getItem('include-special') === 'true';
    }

    // Trigger initial password generation after all settings are restored.
    // We use htmx.ajax() directly instead of dispatching a custom event
    // because the declarative hx-trigger on the textarea may not yet be
    // processed by HTMX at this point, causing a race condition where the
    // event fires but HTMX hasn't attached its listener yet.
    if (languageSelect || wordAmountSlider) {
        const params = {};
        if (languageSelect) params['language-select'] = languageSelect.value;
        if (wordAmountSlider) params['word-amount-slider'] = wordAmountSlider.value;
        if (includeSpecial && includeSpecial.checked) params['include-special'] = 'on';
        if (includeNumbers && includeNumbers.checked) params['include-numbers'] = 'on';
        if (wordSeparator) params['separator'] = wordSeparator.value;
        htmx.ajax('GET', '/word', {target: '#password-input', swap: 'outerHTML', values: params});
    }
}

/**
 * Attach delegated event handlers for landing page setting controls.
 * These were previously inline onEvent handlers; now they are CSP-safe
 * delegated listeners.
 *
 * Handles:
 * - .setting-regen elements: persist to localStorage + trigger regen on change/input
 * - #word-amount-slider: sync display span + number input on input
 * - #word-input: clamp value, sync slider + display, trigger regen on input
 * - #show-identifier-toggle: show/hide identifier input wrapper
 * - .auto-resize-textarea: auto-resize password textarea on input
 *
 * No-op on pages that don't have the relevant elements.
 */
function initSettingHandlers() {
    // --- localStorage key mapping for setting elements ---
    const storageKeyMap = {
        'language-select': 'word-language',
        'word-amount-slider': 'word-amount',
        'word-separator': 'word-separator',
        'include-numbers': 'include-numbers',
        'include-special': 'include-special',
    };

    // Helper: persist a setting element's value to localStorage
    function persistSetting(el) {
        const key = storageKeyMap[el.id];
        if (!key) return;
        if (el.type === 'checkbox') {
            localStorage.setItem(key, el.checked ? 'true' : 'false');
        } else if (el.tagName === 'SELECT') {
            localStorage.setItem(key, el.options[el.selectedIndex].value);
        } else {
            localStorage.setItem(key, el.value);
        }
    }

    // Helper: trigger password regeneration by clicking the regen button
    function triggerRegen() {
        const btn = document.getElementById('regen-button');
        if (btn) btn.click();
    }

    // Delegated change handler for .setting-regen elements
    // (language select, checkboxes, separator, slider on change)
    document.addEventListener('change', (ev) => {
        const el = ev.target.closest && ev.target.closest('.setting-regen');
        if (el) {
            persistSetting(el);
            triggerRegen();
        }

        // Identifier toggle checkbox
        if (ev.target.id === 'show-identifier-toggle') {
            const wrapper = document.getElementById('identifier-input-wrapper');
            if (!wrapper) return;
            if (ev.target.checked) {
                wrapper.classList.remove('hidden');
                ev.target.setAttribute('aria-expanded', 'true');
                const idInput = document.getElementById('key-identifier');
                if (idInput) idInput.focus();
            } else {
                wrapper.classList.add('hidden');
                ev.target.setAttribute('aria-expanded', 'false');
                const idInput = document.getElementById('key-identifier');
                if (idInput) idInput.value = '';
            }
        }
    });

    // Delegated input handler for slider/number sync and separator/textarea
    document.addEventListener('input', (ev) => {
        // Word amount slider: sync display + number input + persist
        if (ev.target.id === 'word-amount-slider') {
            const span = document.getElementById('word-amount');
            const numInput = document.getElementById('word-input');
            if (span) span.textContent = ev.target.value;
            if (numInput) numInput.value = ev.target.value;
            localStorage.setItem('word-amount', ev.target.value);
        }

        // Word number input: clamp, sync slider + display, persist + regen
        if (ev.target.id === 'word-input') {
            let v = Number(ev.target.value);
            if (isNaN(v) || v < 1) ev.target.value = 1;
            else if (v > 50) ev.target.value = 50;
            const span = document.getElementById('word-amount');
            const slider = document.getElementById('word-amount-slider');
            if (span) span.textContent = ev.target.value;
            if (slider) slider.value = ev.target.value;
            localStorage.setItem('word-amount', ev.target.value.toString());
            triggerRegen();
        }

        // Separator input: persist + regen
        if (ev.target.id === 'word-separator') {
            localStorage.setItem('word-separator', ev.target.value);
            triggerRegen();
        }

        // Auto-resize textarea (password input)
        const textarea = ev.target.closest && ev.target.closest('.auto-resize-textarea');
        if (textarea) {
            textarea.parentNode.dataset.clonedVal = textarea.value;
            const lineCount = (textarea.value.match(/\n/g) || []).length + 1;
            textarea.style.height = Math.min(675, Math.max(56, lineCount * 25)) + 'px';
        }
    });
}

/**
 * Initialize theme from localStorage or system preference.
 * No-op on pages without the theme toggle.
 */
function initTheme() {
    const themeToggle = document.getElementById('theme-switcher');
    const themeLabel = document.getElementById('theme-toggle-label');
    if (!themeToggle || !themeLabel) return;

    const savedTheme = localStorage.getItem('theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const activeTheme = savedTheme || (prefersDark ? 'dark' : 'light');

    document.documentElement.setAttribute('data-theme', activeTheme);
    themeToggle.checked = activeTheme === 'dark';

    // Listen on the checkbox change event directly
    themeToggle.addEventListener('change', function () {
        const theme = this.checked ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('theme', theme);
    });
}

/**
 * Initialize the password/key generation mode toggle on the landing page.
 * Handles slide + scale animation, localStorage persistence, and
 * per-element htmx:confirm guards for dual-fire prevention.
 *
 * This is a no-op on pages that don't have the toggle elements.
 */
function initGenerationToggle() {
    const toggle = document.getElementById('custom-toggle');
    const toggleInput = document.getElementById('generation-mode-hidden');
    const thumb = document.getElementById('toggle-thumb');
    const pwd = document.getElementById('password-section');
    const key = document.getElementById('keygen-section');
    const pwdShareBtn = document.getElementById('shareButton');
    const keyShareBtn = document.getElementById('share-key-btn');

    // Bail out early on pages without the toggle (e.g. share view page)
    if (!toggle || !toggleInput || !thumb || !pwd || !key) return;

    // Animation helper — applies transform + opacity in one call
    function setTransform(el, translateX, scale, opacity) {
        el.style.transform = 'translateX(' + translateX + ') scale(' + scale + ')';
        el.style.opacity = opacity;
    }

    // Track which mode is active so we can block HTMX requests from the inactive section.
    // HTMX 2.x caches internal event handlers, so removing hx-post or setting disabled
    // is NOT sufficient to prevent requests. We use htmx:confirm to veto them instead.
    // The canonical source of truth is window.generationMode (set in apply()).

    // Per-element htmx:confirm guards (defense-in-depth alongside the global beforeRequest guard)
    if (pwdShareBtn) {
        pwdShareBtn.addEventListener('htmx:confirm', function (e) {
            if (window.generationMode !== 'password') {
                e.preventDefault();
            }
        });
    }
    if (keyShareBtn) {
        keyShareBtn.addEventListener('htmx:confirm', function (e) {
            if (window.generationMode !== 'key') {
                e.preventDefault();
            }
        });
    }

    let animationTimer = null;

    function apply(mode, animate) {
        const duration = 250;
        // Cancel any in-flight animation to avoid race conditions on rapid toggle
        if (animationTimer) {
            clearTimeout(animationTimer);
            animationTimer = null;
        }
        // Expose to global scope so the global htmx:beforeRequest guard
        // and the per-element htmx:confirm handlers can see the active mode.
        window.generationMode = mode;
        toggle.setAttribute('aria-checked', mode === 'key' ? 'true' : 'false');

        if (mode === 'key') {
            if (animate) {
                // Slide password out to the left + scale down
                setTransform(pwd, '-20px', '0.95', '0');

                animationTimer = setTimeout(() => {
                    animationTimer = null;
                    pwd.classList.add('hidden');
                    // Prepare key section: start from right, scaled down
                    key.classList.remove('hidden');
                    setTransform(key, '20px', '0.95', '0');
                    // Force reflow
                    key.offsetHeight; // eslint-disable-line no-unused-expressions
                    // Animate in
                    setTransform(key, '0px', '1', '1');
                }, duration);
            } else {
                pwd.classList.add('hidden');
                key.classList.remove('hidden');
                setTransform(pwd, '0px', '1', '1');
                setTransform(key, '0px', '1', '1');
            }
            thumb.style.transform = 'translateX(60px)';
        } else {
            if (animate) {
                // Slide key out to the right + scale down
                setTransform(key, '20px', '0.95', '0');

                animationTimer = setTimeout(() => {
                    animationTimer = null;
                    key.classList.add('hidden');
                    // Prepare password section: start from left, scaled down
                    pwd.classList.remove('hidden');
                    setTransform(pwd, '-20px', '0.95', '0');
                    // Force reflow
                    pwd.offsetHeight; // eslint-disable-line no-unused-expressions
                    // Animate in
                    setTransform(pwd, '0px', '1', '1');
                }, duration);
            } else {
                key.classList.add('hidden');
                pwd.classList.remove('hidden');
                setTransform(pwd, '0px', '1', '1');
                setTransform(key, '0px', '1', '1');
            }
            thumb.style.transform = 'translateX(0)';
        }
    }

    const stored = localStorage.getItem('generation-mode-hidden');
    const mode = stored === 'key' ? 'key' : 'password';
    toggleInput.checked = mode === 'key';
    apply(mode, false); // No animation on initial load

    toggle.addEventListener('click', () => {
        const newMode = toggleInput.checked ? 'password' : 'key';
        toggleInput.checked = newMode === 'key';
        localStorage.setItem('generation-mode-hidden', newMode);
        apply(newMode, true);
    });

    // Allow keyboard activation (Enter/Space) for the switch role
    toggle.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            toggle.click();
        }
    });
}

document.addEventListener("DOMContentLoaded", (event) => {
    document.body.addEventListener('htmx:beforeSwap', function (evt) {
        const status = evt.detail.xhr.status;
        if (status === 429 || status === 422 || status === 404 || status === 401 || status === 400) {
            // Allow 429, 422, 404, 401 and 400 responses to swap.
            //
            // set isError to false to avoid error logging in console
            evt.detail.shouldSwap = true;
            evt.detail.isError = false;
        }
    });

    // Auto-dismiss global notifications (e.g. 429 rate-limit alerts) after 5 seconds.
    // Listens for content settling into the #global-notification region and clears it.
    document.body.addEventListener('htmx:afterSettle', function (evt) {
        if (evt.detail.target && evt.detail.target.id === 'global-notification' && evt.detail.target.innerHTML.trim() !== '') {
            setTimeout(function () {
                evt.detail.target.innerHTML = '';
            }, 5000);
        }
    });

    // Centralized modal opening after HTMX swaps in share-result fragments.
    // This is the SINGLE source of truth for opening share modals; the server
    // fragments no longer contain inline showModal() calls.
    // Only react when the swap targeted a share-result container to avoid
    // accidentally opening modals on unrelated swaps.
    document.body.addEventListener('htmx:afterSwap', function (evt) {
        const targetId = evt.detail?.target?.id;
        if (!targetId) return;
        if (targetId === 'key-share-result') {
            const keyShareModal = document.getElementById('key_share_modal');
            if (keyShareModal && !keyShareModal.hasAttribute('open')) {
                keyShareModal.showModal();
            }
        } else if (targetId === 'share-result') {
            const shareModal = document.getElementById('share_modal');
            if (shareModal && !shareModal.hasAttribute('open')) {
                shareModal.showModal();
            }
        }
    });

    // Global guard: block HTMX share requests that don't match the active generation mode.
    // This is a belt-and-suspenders safeguard on top of the per-element htmx:confirm listeners
    // to prevent both share flows from firing after rapid toggling between modes.
    document.body.addEventListener('htmx:beforeRequest', function (evt) {
        const elt = evt.detail?.elt;
        if (!elt?.id) return;
        const mode = window.generationMode || 'password';
        if (elt.id === 'shareButton' && mode !== 'password') {
            console.debug('[htmx guard] Blocking /share because generationMode=', mode);
            evt.preventDefault();
            return;
        }
        if (elt.id === 'share-key-btn' && mode !== 'key') {
            console.debug('[htmx guard] Blocking /key/share because generationMode=', mode);
            evt.preventDefault();
            return;
        }
    });

    // Initialize generation mode toggle (no-op on non-landing pages)
    initGenerationToggle();

    // Restore landing page form settings from localStorage
    initLandingPageSettings();

    // Attach delegated handlers for setting controls, textarea auto-resize, etc.
    initSettingHandlers();

    // Initialize theme from localStorage or system preference
    initTheme();

    // Auto-clear private key when user navigates away or closes the tab.
    // Reduces the time window the key string sits in the textarea DOM.
    // Use both beforeunload (desktop) and pagehide (more reliable on mobile/iOS Safari).
    // Suppress toast — user won't see it during navigation.
    window.addEventListener('beforeunload', () => clearPrivateKey(false));
    window.addEventListener('pagehide', () => clearPrivateKey(false));
});

// Default loading animation for elements that trigger a request (add skeleton class from daisyUI)
document.addEventListener("htmx:configRequest", function (evt) {
    const element = evt.detail.elt;
    element.classList.add('skeleton');

    // Remove the class after the request completes; listener self-removes to prevent memory leaks
    element.addEventListener('htmx:afterRequest', function clearLoading() {
        element.classList.remove('skeleton');
        element.removeEventListener('htmx:afterRequest', clearLoading);
    });
});

function algoBaseName(algo, purpose) {
    // Naming aligned to common OpenSSH conventions, with size/curve suffix
    const suffix = purpose === 'git' ? '_signing' : '';
    if (purpose === 'git' || purpose === 'ssh') {
        if (algo === 'ed25519') return 'id_ed25519' + suffix;
        if (algo.startsWith('ecdsa-')) {
            // ecdsa-p256 → id_ecdsa_p256, ecdsa-p384 → id_ecdsa_p384
            const curve = algo.split('-')[1]; // p256, p384, p521
            return 'id_ecdsa_' + curve + suffix;
        }
        if (algo.startsWith('rsa-')) {
            // rsa-2048 → id_rsa_2048, rsa-4096 → id_rsa_4096, rsa-8192 → id_rsa_8192
            const size = algo.split('-')[1];
            return 'id_rsa_' + size + suffix;
        }
    }
    return 'key';
}

function downloadKey(elementId) {
    const algo = document.getElementById('key-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('key-purpose')?.value || 'ssh';
    const format = document.getElementById('key-format')?.value || 'openssh';
    const isPublic = elementId.includes('public');
    const baseName = algoBaseName(algo, purpose);
    const text = document.getElementById(elementId).value;
    let filename, mime;
    if (format === 'pem') {
        filename = isPublic ? baseName + '.pub.pem' : baseName + '.pem';
        mime = 'application/x-pem-file';
    } else {
        filename = isPublic ? baseName + '.pub' : baseName;
        // Use octet-stream for private key without extension to avoid some browsers (Safari) appending .txt
        mime = isPublic ? 'text/plain' : 'application/octet-stream';
    }
    downloadAsFile(text, filename, mime);
}

// Security helper: best-effort zeroing of Uint8Array buffers
// Note: JavaScript cannot guarantee memory clearing, but this reduces exposure window
function secureZero(arr) {
    if (arr && arr instanceof Uint8Array) {
        arr.fill(0);
    }
}

// Security helper: zero multiple arrays
function secureZeroAll(...arrays) {
    arrays.forEach(secureZero);
}

// Base64 helpers
function bytesToBase64(bytes) {
    let binary = '';
    const len = bytes.length;
    for (let i = 0; i < len; i++) binary += String.fromCharCode(bytes[i]);
    return btoa(binary);
}

function strToBytes(str) {
    return new TextEncoder().encode(str);
}

function base64UrlToBytes(b64url) {
    let b64 = b64url.replace(/-/g, '+').replace(/_/g, '/');
    while (b64.length % 4) b64 += '=';
    const str = atob(b64);
    const bytes = new Uint8Array(str.length);
    for (let i = 0; i < str.length; i++) bytes[i] = str.charCodeAt(i);
    return bytes;
}

function concatArrays(arrs) {
    return arrs.reduce((acc, cur) => {
        const out = new Uint8Array(acc.length + cur.length);
        out.set(acc, 0);
        out.set(cur, acc.length);
        return out;
    }, new Uint8Array());
}

/**
 * Concatenate Uint8Arrays in a single allocation (no intermediate copies).
 * Pushes the result into the tracker array for later zeroing.
 * @param {Uint8Array[]} parts - Arrays to concatenate
 * @param {Uint8Array[]} tracker - Array to push the result into (for zeroing)
 * @returns {Uint8Array}
 */
function concatAndTrack(parts, tracker) {
    const total = parts.reduce((s, b) => s + b.length, 0);
    const result = new Uint8Array(total);
    let offset = 0;
    for (const p of parts) {
        result.set(p, offset);
        offset += p.length;
    }
    tracker.push(result);
    return result;
}

/** Push a UTF-8-encoded length-prefixed string into `arr`. */
function pushText(arr, text) {
    const bytes = new TextEncoder().encode(text);
    arr.push(u32(bytes.length), bytes);
}

/** Push a length-prefixed byte string into `arr`. */
function pushBytes(arr, data) {
    arr.push(u32(data.length), data);
}

function u32(n) {
    const b = new Uint8Array(4);
    new DataView(b.buffer).setUint32(0, n);
    return b;
}

function sshString(bytes) {
    return concatArrays([u32(bytes.length), bytes]);
}

function sshStringFromText(txt) {
    return sshString(new TextEncoder().encode(txt));
}

function mpint(bytes) { // minimal implementation
    // strip leading zeros
    let i = 0;
    while (i < bytes.length - 1 && bytes[i] === 0) i++;
    bytes = bytes.slice(i);
    // if high bit set prepend 0
    if (bytes.length && (bytes[0] & 0x80)) {
        const withZero = new Uint8Array(bytes.length + 1);
        withZero[0] = 0;
        withZero.set(bytes, 1);
        bytes = withZero;
    }
    return concatArrays([u32(bytes.length), bytes]);
}

/**
 * Encode a big integer as SSH mpint (length-prefixed, two's complement),
 * tracking all intermediate Uint8Array copies for later zeroing.
 * Returns [lengthBuf, valueBuf] for use in OpenSSH wire format assembly.
 * NOTE: Does NOT track the input `bytes` — caller is responsible for zeroing it.
 * @param {Uint8Array} bytes - Raw big-endian integer bytes
 * @param {Uint8Array[]} tracker - Array to push intermediates into for zeroing
 * @returns {Uint8Array[]} [length4bytes, valueBytes]
 */
function mpintTracked(bytes, tracker) {
    let i = 0;
    while (i < bytes.length - 1 && bytes[i] === 0) i++;
    const stripped = bytes.slice(i);
    let result = stripped;
    if (result.length && (result[0] & 0x80)) {
        const withZero = new Uint8Array(result.length + 1);
        withZero[0] = 0;
        withZero.set(result, 1);
        tracker.push(stripped);
        result = withZero;
    }
    tracker.push(result);
    return [u32(result.length), result];
}

function buildSshBufferEd25519(pubKey) {
    const algo = strToBytes('ssh-ed25519');
    const totalLen = 4 + algo.length + 4 + pubKey.length;
    const buf = new Uint8Array(totalLen);
    let offset = 0;
    const view = new DataView(buf.buffer);
    view.setUint32(offset, algo.length);
    offset += 4;
    buf.set(algo, offset);
    offset += algo.length;
    view.setUint32(offset, pubKey.length);
    offset += 4;
    buf.set(pubKey, offset);
    return buf;
}

function toPem(label, derBytes) {
    const b64 = bytesToBase64(new Uint8Array(derBytes));
    const lines = b64.match(/.{1,64}/g) || [];
    return `-----BEGIN ${label}-----\n${lines.join('\n')}\n-----END ${label}-----`;
}

// ─── Ed25519 via WebCrypto ──────────────────────────────────────────

/**
 * Generate an Ed25519 key pair using the Web Crypto API.
 * Returns { pub, seed, secretKey64, publicPem, privatePem, spkiDer, pkcs8Der }
 * where pub is 32-byte raw public key, seed is 32-byte private seed,
 * secretKey64 is 64-byte (seed+pub) for OpenSSH format construction,
 * and PEMs/DER are the browser-exported standard formats.
 */
async function generateEd25519WebCrypto() {
    const kp = await crypto.subtle.generateKey({name: 'Ed25519'}, true, ['sign', 'verify']);
    const spkiDer = new Uint8Array(await crypto.subtle.exportKey('spki', kp.publicKey));
    const pkcs8Der = new Uint8Array(await crypto.subtle.exportKey('pkcs8', kp.privateKey));
    try {
        const pub = extractEd25519PublicFromSpki(spkiDer);
        const seed = extractEd25519SeedFromPkcs8(pkcs8Der);
        // OpenSSH private key format stores 64 bytes: seed (32) + public key (32)
        const secretKey64 = concatArrays([seed, pub]);
        const publicPem = toPem('PUBLIC KEY', spkiDer);
        const privatePem = toPem('PRIVATE KEY', pkcs8Der);
        return {pub, seed, secretKey64, publicPem, privatePem, spkiDer, pkcs8Der};
    } catch (e) {
        // Best-effort cleanup of sensitive DER buffers on failure
        pkcs8Der.fill(0);
        throw e;
    }
}

// Ed25519 OID 1.3.101.112 encoded as DER: 06 03 2B 65 70
const ED25519_OID_BYTES = Object.freeze([0x06, 0x03, 0x2b, 0x65, 0x70]);

/**
 * Verify that the Ed25519 OID (1.3.101.112) is present at the expected offset.
 */
function verifyEd25519Oid(der, offset) {
    for (let i = 0; i < ED25519_OID_BYTES.length; i++) {
        if (der[offset + i] !== ED25519_OID_BYTES[i]) {
            throw new Error('DER does not contain Ed25519 OID at expected offset ' + offset);
        }
    }
}

/**
 * Extract the raw 32-byte Ed25519 public key from an SPKI DER structure.
 * Standard Ed25519 SPKI (RFC 8410) is exactly 44 bytes:
 * SEQUENCE { SEQUENCE { OID 1.3.101.112 }, BIT STRING { 0x00, <32 bytes> } }
 * Byte layout: 30 2a 30 05 [06 03 2b 65 70] 03 21 00 <32 bytes>
 */
function extractEd25519PublicFromSpki(spki) {
    if (spki.length !== 44) throw new Error('Unexpected SPKI length for Ed25519: ' + spki.length + ' (expected 44)');
    verifyEd25519Oid(spki, 4); // OID starts at byte 4
    return spki.slice(12); // skip 12-byte header
}

/**
 * Extract the raw 32-byte Ed25519 private seed from a PKCS#8 DER structure.
 * Standard Ed25519 PKCS#8 (RFC 8410) is exactly 48 bytes:
 * SEQUENCE { INTEGER 0, SEQUENCE { OID 1.3.101.112 }, OCTET STRING { OCTET STRING { <32 bytes> } } }
 * Byte layout: 30 2e 02 01 00 30 05 [06 03 2b 65 70] 04 22 04 20 <32 bytes>
 */
function extractEd25519SeedFromPkcs8(pkcs8) {
    if (pkcs8.length !== 48) throw new Error('Unexpected PKCS#8 length for Ed25519: ' + pkcs8.length + ' (expected 48)');
    verifyEd25519Oid(pkcs8, 7); // OID starts at byte 7
    return pkcs8.slice(16); // skip 16-byte header
}

// ─── ECDSA / RSA PEM via WebCrypto ─────────────────────────────────

/**
 * Export a WebCrypto CryptoKey as PEM (PKCS#8 for private, SPKI for public).
 * Works for ECDSA and RSA keys that were generated with extractable=true.
 * @param {CryptoKey} cryptoKey
 * @param {'private'|'public'} keyType
 * @returns {Promise<string>}
 */
async function exportCryptoKeyAsPem(cryptoKey, keyType) {
    if (keyType === 'private') {
        const der = await crypto.subtle.exportKey('pkcs8', cryptoKey);
        const pem = toPem('PRIVATE KEY', der);
        new Uint8Array(der).fill(0); // best-effort zeroing of private DER
        return pem;
    } else {
        const der = await crypto.subtle.exportKey('spki', cryptoKey);
        return toPem('PUBLIC KEY', der);
    }
}

/**
 * Build an OpenSSH-format Ed25519 private key.
 * @param {Uint8Array} secretKey64 - 64-byte secret key (seed || pub), caller owns and zeros
 * @param {Uint8Array} publicKey32 - 32-byte public key
 * @param {string} comment - Key comment
 * @returns {string} OpenSSH private key PEM
 */
function buildOpenSSHPrivateKeyEd25519(secretKey64, publicKey32, comment) {
    // Format: https://github.com/openssh/openssh-portable/blob/master/PROTOCOL.key
    // Collect all intermediate sensitive Uint8Arrays for zeroing before return
    const sensitiveInternal = [];

    const algorithm = 'ssh-ed25519';
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    const parts = [magic];
    pushText(parts, 'none');  // ciphername (unencrypted)
    pushText(parts, 'none');  // kdfname (no key derivation)
    pushText(parts, '');      // kdfoptions (empty)
    parts.push(u32(1));       // number of keys
    // public key (wire format) — public data, no need to track
    const pubWireParts = [];
    pushText(pubWireParts, algorithm);
    pushBytes(pubWireParts, publicKey32);
    const pubWire = concatArrays(pubWireParts);
    parts.push(u32(pubWire.length), pubWire);

    // Private block
    const privParts = [];
    const check = crypto.getRandomValues(new Uint8Array(4));
    privParts.push(check, check.slice()); // two identical check ints
    pushText(privParts, algorithm);
    pushBytes(privParts, publicKey32);
    pushBytes(privParts, secretKey64);
    pushText(privParts, comment || '');
    // padding 1..n
    let totalLen = privParts.reduce((s, b) => s + b.length, 0);
    const padNeeded = (8 - (totalLen % 8)) % 8;
    for (let i = 1; i <= padNeeded; i++) {
        privParts.push(new Uint8Array([i]));
    }
    const privBlock = concatAndTrack(privParts, sensitiveInternal);
    parts.push(u32(privBlock.length), privBlock);

    const full = concatAndTrack(parts, sensitiveInternal);
    const b64 = bytesToBase64(full).match(/.{1,70}/g).join('\n');
    const pem = `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
    // Zero all intermediate buffers that held private key material
    secureZeroAll(...sensitiveInternal);
    return pem;
}

function buildOpenSshEcdsaPublic(jwk, curveName, comment) {
    // curveName: nistp256/nistp384/nistp521
    const algo = 'ecdsa-sha2-' + curveName;
    const x = base64UrlToBytes(jwk.x);
    const y = base64UrlToBytes(jwk.y);
    const point = new Uint8Array(1 + x.length + y.length);
    point[0] = 0x04;
    point.set(x, 1);
    point.set(y, 1 + x.length);
    const blob = concatArrays([
        sshStringFromText(algo),
        sshStringFromText(curveName),
        sshString(point)
    ]);
    return algo + ' ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
}

function buildOpenSshRsaPublic(jwk, comment) {
    const algo = 'ssh-rsa';
    const e = base64UrlToBytes(jwk.e);
    const n = base64UrlToBytes(jwk.n);
    const blob = concatArrays([
        sshStringFromText(algo),
        mpint(e),
        mpint(n)
    ]);
    return algo + ' ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
}

/**
 * Build an OpenSSH-format ECDSA private key.
 * @param {string} curveName - OpenSSH curve name (nistp256, nistp384, nistp521)
 * @param {Uint8Array} publicPoint - Uncompressed EC point (0x04 || x || y)
 * @param {Uint8Array} privateScalarBytes - Raw private scalar bytes (caller owns and zeros)
 * @param {string} comment - Key comment
 * @returns {string} OpenSSH private key PEM
 */
function buildOpenSSHPrivateKeyECDSA(curveName, publicPoint, privateScalarBytes, comment) {
    const algo = 'ecdsa-sha2-' + curveName;
    // Collect all intermediate sensitive Uint8Arrays for zeroing before return
    const sensitiveInternal = [];

    // Public key blob: string alg, string curve, string point (public — no need to track)
    const pointBlobParts = [];
    pushText(pointBlobParts, algo);
    pushText(pointBlobParts, curveName);
    pushBytes(pointBlobParts, publicPoint);
    const pointBlob = concatArrays(pointBlobParts);

    // Outer structure
    const blocks = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    blocks.push(magic);
    pushText(blocks, 'none');  // ciphername
    pushText(blocks, 'none');  // kdfname
    pushText(blocks, '');      // kdfoptions
    blocks.push(u32(1));       // number of keys
    blocks.push(u32(pointBlob.length), pointBlob);
    // private section
    const check = crypto.getRandomValues(new Uint8Array(4));
    const privParts = [];
    privParts.push(check, check.slice());
    pushText(privParts, algo);
    pushText(privParts, curveName);
    pushBytes(privParts, publicPoint);
    // private scalar mpint — caller passes decoded bytes, no base64 decode here
    const mp = mpintTracked(privateScalarBytes, sensitiveInternal);
    privParts.push(...mp);
    pushText(privParts, comment || '');
    // padding to 8
    let privLenCount = privParts.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) privParts.push(new Uint8Array([i]));
    const privBlock = concatAndTrack(privParts, sensitiveInternal);
    blocks.push(u32(privBlock.length), privBlock);
    const full = concatAndTrack(blocks, sensitiveInternal);
    const b64 = bytesToBase64(full).match(/.{1,70}/g).join('\n');
    const pem = `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
    // Zero all intermediate buffers that held private key material
    secureZeroAll(...sensitiveInternal);
    return pem;
}

/**
 * Build an OpenSSH-format RSA private key.
 * All parameters are pre-decoded Uint8Arrays — caller owns and zeros them.
 * @param {Uint8Array} n - RSA modulus
 * @param {Uint8Array} e - Public exponent
 * @param {Uint8Array} d - Private exponent
 * @param {Uint8Array} p - First prime factor
 * @param {Uint8Array} q - Second prime factor
 * @param {Uint8Array} iqmp - CRT coefficient (q^-1 mod p)
 * @param {string} comment - Key comment
 * @returns {string} OpenSSH private key PEM
 */
function buildOpenSSHPrivateKeyRSA(n, e, d, p, q, iqmp, comment) {
    const algo = 'ssh-rsa';
    // Collect all intermediate sensitive Uint8Arrays for zeroing before return
    const sensitiveInternal = [];

    // Public blob: e and n are public, no need to track in sensitiveInternal
    const pubBlobParts = [];
    pushText(pubBlobParts, algo);
    pubBlobParts.push(...mpintTracked(e, []));  // public — discard tracker
    pubBlobParts.push(...mpintTracked(n, []));  // public — discard tracker
    const pubBlob = concatArrays(pubBlobParts);

    const sections = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    sections.push(magic);
    pushText(sections, 'none');  // ciphername
    pushText(sections, 'none');  // kdfname
    pushText(sections, '');      // kdfoptions
    sections.push(u32(1));       // number of keys
    sections.push(u32(pubBlob.length), pubBlob);
    const check = crypto.getRandomValues(new Uint8Array(4));
    const priv = [];
    priv.push(check, check.slice());
    pushText(priv, algo);
    // key components: n,e,d,iqmp,p,q — all passed as Uint8Array
    // n and e are public but appear in the private block per OpenSSH format
    priv.push(...mpintTracked(n, []));  // public — discard tracker
    priv.push(...mpintTracked(e, []));  // public — discard tracker
    // Only sensitive fields tracked for zeroing
    [d, iqmp, p, q].forEach(b => {
        priv.push(...mpintTracked(b, sensitiveInternal));
    });
    pushText(priv, comment || '');
    let privLenCount = priv.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) priv.push(new Uint8Array([i]));
    const privBlock = concatAndTrack(priv, sensitiveInternal);
    sections.push(u32(privBlock.length), privBlock);
    const full = concatAndTrack(sections, sensitiveInternal);
    const b64 = bytesToBase64(full).match(/.{1,70}/g).join('\n');
    const pem = `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
    // Zero all intermediate buffers that held private key material
    secureZeroAll(...sensitiveInternal);
    return pem;
}

// ─── Shared key generation core ─────────────────────────────────────

/**
 * Generate a key pair for the given algorithm and format.
 * Returns { publicKeyText, privateKeyText, sensitiveBuffers }.
 * Caller is responsible for zeroing sensitiveBuffers in a finally block.
 * On internal failure, any accumulated sensitive buffers are zeroed before
 * the error is re-thrown, so the caller does not need to handle cleanup
 * for the error case.
 *
 * @param {string} algo - Algorithm identifier (ed25519, ecdsa-p256, rsa-2048, etc.)
 * @param {string} format - 'pem' or 'openssh'
 * @param {string} comment - Key comment/identifier (may be empty string)
 * @returns {Promise<{publicKeyText: string, privateKeyText: string, sensitiveBuffers: Uint8Array[]}>}
 */
async function generateKeyPair(algo, format, comment) {
    const sensitiveBuffers = [];

    try {
        let publicKeyText = '';
        let privateKeyText = '';

        if (algo === 'ed25519') {
            const ed = await generateEd25519WebCrypto();
            sensitiveBuffers.push(ed.seed, ed.secretKey64, ed.pkcs8Der);
            if (format === 'pem') {
                publicKeyText = ed.publicPem;
                privateKeyText = ed.privatePem;
            } else {
                const blob = buildSshBufferEd25519(ed.pub);
                publicKeyText = 'ssh-ed25519 ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
                privateKeyText = buildOpenSSHPrivateKeyEd25519(ed.secretKey64, ed.pub, comment);
            }
        } else if (algo.startsWith('ecdsa-')) {
            const curveMap = {'ecdsa-p256': 'nistp256', 'ecdsa-p384': 'nistp384', 'ecdsa-p521': 'nistp521'};
            const named = curveMap[algo];
            if (!named) throw new Error('Unsupported ECDSA curve');
            const webCurve = {nistp256: 'P-256', nistp384: 'P-384', nistp521: 'P-521'}[named];
            const keyPair = await crypto.subtle.generateKey({
                name: 'ECDSA',
                namedCurve: webCurve
            }, true, ['sign', 'verify']);
            if (format === 'pem') {
                publicKeyText = await exportCryptoKeyAsPem(keyPair.publicKey, 'public');
                privateKeyText = await exportCryptoKeyAsPem(keyPair.privateKey, 'private');
            } else {
                const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
                const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
                const x = base64UrlToBytes(jwkPub.x);
                const y = base64UrlToBytes(jwkPub.y);
                const point = new Uint8Array(1 + x.length + y.length);
                point[0] = 0x04;
                point.set(x, 1);
                point.set(y, 1 + x.length);
                const dBytes = base64UrlToBytes(jwkPriv.d);
                sensitiveBuffers.push(dBytes);
                publicKeyText = buildOpenSshEcdsaPublic(jwkPub, named, comment);
                // Pass decoded Uint8Array — single decode, caller owns the buffer
                privateKeyText = buildOpenSSHPrivateKeyECDSA(named, point, dBytes, comment);
                jwkPriv.d = '';
            }
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const validSizes = [2048, 4096, 8192];
            if (!validSizes.includes(size)) throw new Error('Unsupported RSA key size');
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSASSA-PKCS1-v1_5',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            if (format === 'pem') {
                publicKeyText = await exportCryptoKeyAsPem(keyPair.publicKey, 'public');
                privateKeyText = await exportCryptoKeyAsPem(keyPair.privateKey, 'private');
            } else {
                const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
                const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
                // Decode all fields once — caller owns buffers
                const nBytes = base64UrlToBytes(jwkPriv.n);
                const eBytes = base64UrlToBytes(jwkPriv.e);
                const dBytes = base64UrlToBytes(jwkPriv.d);
                const pBytes = base64UrlToBytes(jwkPriv.p);
                const qBytes = base64UrlToBytes(jwkPriv.q);
                const iqmpBytes = base64UrlToBytes(jwkPriv.qi);
                // Only track genuinely sensitive fields (n and e are public)
                sensitiveBuffers.push(dBytes, pBytes, qBytes, iqmpBytes);
                publicKeyText = buildOpenSshRsaPublic(jwkPub, comment);
                // Pass decoded Uint8Arrays — single decode, single owner
                privateKeyText = buildOpenSSHPrivateKeyRSA(nBytes, eBytes, dBytes, pBytes, qBytes, iqmpBytes, comment);
                ['d', 'p', 'q', 'dp', 'dq', 'qi', 'n', 'e'].forEach(field => {
                    jwkPriv[field] = '';
                });
            }
        } else {
            throw new Error('Unknown algorithm');
        }

        return {publicKeyText, privateKeyText, sensitiveBuffers};
    } catch (e) {
        // Zero any sensitive buffers accumulated before the failure
        secureZeroAll(...sensitiveBuffers);
        throw e;
    }
}

/**
 * Map a key generation error to a user-friendly message.
 * @param {Error} e
 * @returns {string}
 */
function formatKeyGenError(e) {
    if (e.message === 'Unsupported ECDSA curve' || e.message === 'Unknown algorithm') {
        return e.message;
    } else if (e.name === 'NotSupportedError') {
        return 'This algorithm is not supported by your browser.';
    } else if (e.name === 'OperationError') {
        return 'Key generation operation failed. Please try again.';
    } else if (!window.isSecureContext) {
        return 'Secure context required. Please use HTTPS.';
    } else {
        return 'Key generation failed: ' + (e.message || 'Unknown error');
    }
}

async function generateKey() {
    const algo = document.getElementById('key-algorithm').value;
    const purpose = document.getElementById('key-purpose').value;
    const format = document.getElementById('key-format')?.value || 'openssh';
    const rawIdentifier = document.getElementById('key-identifier').value.trim();
    const identifier = isValidIdentifier(rawIdentifier) ? rawIdentifier : '';
    const pubOut = document.getElementById('public-key-output');
    const privOut = document.getElementById('private-key-output');
    const errAlert = document.getElementById('key-error-alert');
    const errText = document.getElementById('key-error-text');
    const keygenIcon = document.getElementById('keygen-icon');
    const generateBtn = document.getElementById('generate-key-btn');

    if (rawIdentifier && !identifier) {
        errText.textContent = 'Invalid identifier: only printable ASCII up to 256 chars.';
        errAlert.classList.remove('hidden');
        return;
    }

    // Check for secure context requirement (all algorithms now use WebCrypto)
    if (!window.isSecureContext) {
        errText.textContent = 'Secure context required. Please use HTTPS.';
        errAlert.classList.remove('hidden');
        return;
    }

    // Check crypto.subtle availability
    if (!window.crypto || !window.crypto.subtle) {
        errText.textContent = 'Web Crypto API not available. Please use a modern browser with HTTPS.';
        errAlert.classList.remove('hidden');
        return;
    }

    errAlert.classList.add('hidden');
    pubOut.value = '';
    privOut.value = '';
    // Add spinning animation to the icon (like password regen button)
    if (keygenIcon) keygenIcon.classList.add('animate-spin-reverse');
    generateBtn.disabled = true;

    // Track sensitive buffers for cleanup
    let sensitiveBuffers = [];

    try {
        const result = await generateKeyPair(algo, format, identifier);
        sensitiveBuffers = result.sensitiveBuffers;
        pubOut.value = result.publicKeyText;
        privOut.value = result.privateKeyText;

        // Update UI to show generated state
        const emptyState = document.getElementById('key-empty-state');
        const generatedState = document.getElementById('key-generated-state');
        const keyTypeDisplay = document.getElementById('key-type-display');
        const outputSection = document.getElementById('key-output-section');

        if (emptyState) emptyState.classList.add('hidden');
        if (generatedState) generatedState.classList.remove('hidden');
        if (keyTypeDisplay) {
            // Format algorithm name nicely
            const algoNames = {
                'ed25519': 'Ed25519',
                'ecdsa-p256': 'ECDSA P-256',
                'ecdsa-p384': 'ECDSA P-384',
                'ecdsa-p521': 'ECDSA P-521',
                'rsa-2048': 'RSA 2048',
                'rsa-4096': 'RSA 4096',
                'rsa-8192': 'RSA 8192'
            };
            keyTypeDisplay.textContent = algoNames[algo] || algo;
        }

        // Update library info tooltip
        const libraryTooltip = document.getElementById('key-library-tooltip');
        if (libraryTooltip) {
            libraryTooltip.setAttribute('data-tip', 'Generated using Web Crypto API');
        }

        if (outputSection) outputSection.classList.remove('hidden');

        // Reset to public key tab
        const tabPublic = document.getElementById('tab-public');
        const tabPrivate = document.getElementById('tab-private');
        const panelPublic = document.getElementById('panel-public');
        const panelPrivate = document.getElementById('panel-private');
        if (tabPublic && tabPrivate && panelPublic && panelPrivate) {
            tabPublic.classList.add('border-primary', 'bg-base-100');
            tabPublic.classList.remove('border-transparent');
            tabPublic.setAttribute('aria-selected', 'true');
            tabPublic.setAttribute('tabindex', '0');
            tabPrivate.classList.remove('border-primary', 'bg-base-100');
            tabPrivate.classList.add('border-transparent');
            tabPrivate.setAttribute('aria-selected', 'false');
            tabPrivate.setAttribute('tabindex', '-1');
            panelPublic.classList.remove('hidden');
            panelPrivate.classList.add('hidden');
        }

        updateInstructions(purpose, algo, identifier);
    } catch (e) {
        console.error('Key generation failed:', e);
        errText.textContent = formatKeyGenError(e);
        errAlert.classList.remove('hidden');
    } finally {
        // Remove spinning animation from the icon
        const keygenIcon = document.getElementById('keygen-icon');
        if (keygenIcon) keygenIcon.classList.remove('animate-spin-reverse');
        generateBtn.disabled = false;
        // Best-effort cleanup of sensitive buffers
        secureZeroAll(...sensitiveBuffers);
        sensitiveBuffers = null;
    }
}

// Identifier validation: printable ASCII (space 0x20 to ~ 0x7E), length 0..256
function isValidIdentifier(id) {
    return id.length <= 256 && /^[ -~]*$/.test(id);
}

function updateInstructions(purpose, algo, identifier) {
    const el = document.getElementById('key-instructions');
    if (!el) return;
    // Clear existing
    while (el.firstChild) el.removeChild(el.firstChild);
    const baseName = algoBaseName(algo, purpose);
    if (purpose === 'ssh') {
        const p1 = document.createElement('p');
        p1.textContent = `1. Save Private Key as ~/.ssh/${baseName}; chmod 600 ~/.ssh/${baseName}.`;
        el.appendChild(p1);
        const p2 = document.createElement('p');
        p2.textContent = '2. Append Public Key to ~/.ssh/authorized_keys on the server.';
        el.appendChild(p2);
        const p3 = document.createElement('p');
        p3.textContent = `3. Use: ssh -i ~/.ssh/${baseName} user@host`;
        el.appendChild(p3);
    } else {
        const intro = document.createElement('p');
        intro.textContent = 'Git SSH signing setup:';
        el.appendChild(intro);
        const ol = document.createElement('ol');
        ol.className = 'list-decimal list-inside';
        const steps = [
            `Save private key as ~/.ssh/${baseName}; chmod 600 ~/.ssh/${baseName}`,
            `Add ${baseName}.pub as a Signing Key on GitHub/GitLab`,
            'git config --global gpg.format ssh',
            `git config --global user.signingkey ~/.ssh/${baseName}`,
            '(Optional) git config --global commit.gpgsign true',
            'Sign commits: git commit -S -m "msg"'
        ];
        steps.forEach(t => {
            const li = document.createElement('li');
            li.textContent = t;
            ol.appendChild(li);
        });
        el.appendChild(ol);
    }
}

function attachKeyGenHandlers() {
    const purposeSel = document.getElementById('key-purpose');
    const algoSel = document.getElementById('key-algorithm');
    const idInput = document.getElementById('key-identifier');
    const genBtn = document.getElementById('generate-key-btn');

    if (!purposeSel || !algoSel || !genBtn) return;

    // Tab switching for key output
    const tabPublic = document.getElementById('tab-public');
    const tabPrivate = document.getElementById('tab-private');
    const panelPublic = document.getElementById('panel-public');
    const panelPrivate = document.getElementById('panel-private');

    if (tabPublic && tabPrivate && panelPublic && panelPrivate) {
        const activateTab = (activeTab, inactiveTab, activePanel, inactivePanel) => {
            // Update tab styles
            activeTab.classList.add('border-primary', 'bg-base-100');
            activeTab.classList.remove('border-transparent');
            inactiveTab.classList.remove('border-primary', 'bg-base-100');
            inactiveTab.classList.add('border-transparent');
            // Update ARIA attributes
            activeTab.setAttribute('aria-selected', 'true');
            activeTab.setAttribute('tabindex', '0');
            inactiveTab.setAttribute('aria-selected', 'false');
            inactiveTab.setAttribute('tabindex', '-1');
            // Show/hide panels
            activePanel.classList.remove('hidden');
            inactivePanel.classList.add('hidden');
            // Focus the active tab
            activeTab.focus();
        };

        tabPublic.addEventListener('click', () => {
            activateTab(tabPublic, tabPrivate, panelPublic, panelPrivate);
        });

        tabPrivate.addEventListener('click', () => {
            activateTab(tabPrivate, tabPublic, panelPrivate, panelPublic);
        });

        // Keyboard navigation for tabs (Arrow keys, Home, End)
        const handleTabKeydown = (event) => {
            const isPublicFocused = document.activeElement === tabPublic;
            const isPrivateFocused = document.activeElement === tabPrivate;

            if (!isPublicFocused && !isPrivateFocused) return;

            switch (event.key) {
                case 'ArrowLeft':
                case 'ArrowUp':
                    event.preventDefault();
                    if (isPrivateFocused) {
                        activateTab(tabPublic, tabPrivate, panelPublic, panelPrivate);
                    }
                    break;
                case 'ArrowRight':
                case 'ArrowDown':
                    event.preventDefault();
                    if (isPublicFocused) {
                        activateTab(tabPrivate, tabPublic, panelPrivate, panelPublic);
                    }
                    break;
                case 'Home':
                    event.preventDefault();
                    activateTab(tabPublic, tabPrivate, panelPublic, panelPrivate);
                    break;
                case 'End':
                    event.preventDefault();
                    activateTab(tabPrivate, tabPublic, panelPrivate, panelPublic);
                    break;
            }
        };

        tabPublic.addEventListener('keydown', handleTabKeydown);
        tabPrivate.addEventListener('keydown', handleTabKeydown);
    }

    // Reset UI when settings change
    const resetKeyOutput = () => {
        document.getElementById('public-key-output').value = '';
        document.getElementById('private-key-output').value = '';
        document.getElementById('key-error-alert').classList.add('hidden');
        document.getElementById('key-output-section')?.classList.add('hidden');
        document.getElementById('key-empty-state')?.classList.remove('hidden');
        document.getElementById('key-generated-state')?.classList.add('hidden');
    };

    // Update download button labels based on purpose and algorithm
    const updateDownloadLabels = (purpose, algo) => {
        const baseName = algoBaseName(algo, purpose);
        const publicLabel = document.getElementById('download-public-label');
        const privateLabel = document.getElementById('download-private-label');
        const publicBtn = document.getElementById('download-public-btn');
        const privateBtn = document.getElementById('download-private-btn');
        if (publicLabel) {
            publicLabel.textContent = `${baseName}.pub`;
        }
        if (privateLabel) {
            privateLabel.textContent = baseName;
        }
        if (publicBtn) {
            publicBtn.setAttribute('aria-label', `Download public key as ${baseName}.pub`);
        }
        if (privateBtn) {
            privateBtn.setAttribute('aria-label', `Download private key as ${baseName}`);
        }
    };

    // Update comment placeholder based on purpose
    const updateCommentPlaceholder = (purpose) => {
        if (idInput) {
            if (purpose === 'git') {
                idInput.placeholder = 'your@email.com';
            } else {
                idInput.placeholder = 'user@hostname';
            }
        }
    };

    // Initialize UI based on current selections
    updateDownloadLabels(purposeSel.value, algoSel.value);
    updateCommentPlaceholder(purposeSel.value);

    purposeSel.addEventListener('change', () => {
        if (idInput) idInput.value = '';
        resetKeyOutput();
        updateDownloadLabels(purposeSel.value, algoSel.value);
        updateCommentPlaceholder(purposeSel.value);
        updateInstructions(purposeSel.value, algoSel.value, '');
    });

    algoSel.addEventListener('change', () => {
        resetKeyOutput();
        updateDownloadLabels(purposeSel.value, algoSel.value);
        updateInstructions(purposeSel.value, algoSel.value, idInput?.value.trim() || '');
    });

    if (idInput) {
        idInput.addEventListener('input', () => {
            const v = idInput.value.trim();
            if (v && !isValidIdentifier(v)) {
                idInput.classList.add('input-error');
            } else {
                idInput.classList.remove('input-error');
                updateInstructions(purposeSel.value, algoSel.value, v);
            }
        });
    }

    genBtn.addEventListener('click', generateKey);
}

// Delegated handlers for copy/download/clear buttons (CSP-safe)
document.addEventListener('click', (ev) => {
    const copyEl = ev.target.closest && ev.target.closest('.copy-btn');
    if (copyEl) {
        const targetId = copyEl.getAttribute('data-copy-target');
        if (targetId) copyToClipboard(targetId);
    }
    const dlEl = ev.target.closest && ev.target.closest('.download-btn');
    if (dlEl) {
        const targetId = dlEl.getAttribute('data-download-target');
        if (targetId) downloadKey(targetId);
    }
    const clearEl = ev.target.closest && ev.target.closest('.clear-private-key-btn');
    if (clearEl) {
        clearPrivateKey();
    }
    // Generate share key button (key share pending page)
    const genShareEl = ev.target.closest && ev.target.closest('#generate-share-key-btn');
    if (genShareEl) {
        generateShareKey();
    }
});

document.addEventListener('DOMContentLoaded', attachKeyGenHandlers);

/**
 * Generate a key pair for a pending share and submit the public key to the server.
 * This is called from the share page when a recipient clicks "Generate Key Pair".
 */
async function generateShareKey() {
    const shareId = document.getElementById('share-id')?.value;
    const algo = document.getElementById('share-algorithm')?.value;
    const purpose = document.getElementById('share-purpose')?.value;
    const format = document.getElementById('share-format')?.value || 'openssh';
    const label = document.getElementById('share-label')?.value || '';
    const errAlert = document.getElementById('share-key-error');
    const errText = document.getElementById('share-key-error-text');
    const generateBtn = document.getElementById('generate-share-key-btn');
    const keygenIcon = document.getElementById('share-keygen-icon');

    if (!shareId || !algo || !purpose) {
        if (errText) errText.textContent = 'Missing share configuration.';
        if (errAlert) errAlert.classList.remove('hidden');
        return;
    }

    // Check for secure context requirement (all algorithms use WebCrypto)
    if (!window.isSecureContext) {
        if (errText) errText.textContent = 'Secure context required. Please use HTTPS.';
        if (errAlert) errAlert.classList.remove('hidden');
        return;
    }

    // Check crypto.subtle availability
    if (!window.crypto || !window.crypto.subtle) {
        if (errText) errText.textContent = 'Web Crypto API not available. Please use a modern browser with HTTPS.';
        if (errAlert) errAlert.classList.remove('hidden');
        return;
    }

    if (errAlert) errAlert.classList.add('hidden');

    // Add spinning animation and disable button
    if (keygenIcon) keygenIcon.classList.add('animate-spin-reverse');
    if (generateBtn) generateBtn.disabled = true;

    // Track sensitive buffers for cleanup
    let sensitiveBuffers = [];

    try {
        const comment = label || '';
        const result = await generateKeyPair(algo, format, comment);
        sensitiveBuffers = result.sensitiveBuffers;
        let privateKeyText = result.privateKeyText;

        // Download the private key automatically
        downloadSharePrivateKey(privateKeyText, algo, purpose, format);

        // Drop reference to private key string (best-effort; JS strings are immutable and GC'd)
        privateKeyText = '';

        // POST the public key to the server
        const formData = new URLSearchParams();
        formData.append('public-key', result.publicKeyText);
        formData.append('algorithm', algo);

        const response = await fetch(`/key/share/${shareId}/complete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData.toString()
        });

        const responseHtml = await response.text();

        if (!response.ok) {
            // Server returned an error
            if (errText) errText.textContent = 'Failed to save public key. Please try again.';
            if (errAlert) errAlert.classList.remove('hidden');
            console.error('Server error:', responseHtml);
            return;
        }

        // Success! Replace the share content with the completed view via HTMX swap
        // Using htmx.swap avoids raw innerHTML and ensures any hx-* attributes
        // in the response fragment are properly processed by HTMX.
        const shareContent = document.getElementById('share-content');
        if (shareContent) {
            htmx.swap(shareContent, responseHtml, {swapStyle: 'innerHTML'});
        }

    } catch (e) {
        console.error('Key generation failed:', e);
        if (errText) errText.textContent = formatKeyGenError(e);
        if (errAlert) errAlert.classList.remove('hidden');
    } finally {
        if (keygenIcon) keygenIcon.classList.remove('animate-spin-reverse');
        if (generateBtn) generateBtn.disabled = false;
        secureZeroAll(...sensitiveBuffers);
        sensitiveBuffers = null;
    }
}

/**
 * Download the private key generated on the share page.
 */
function downloadSharePrivateKey(privateKeyText, algo, purpose, format) {
    const baseName = algoBaseName(algo, purpose);
    if (format === 'pem') {
        downloadAsFile(privateKeyText, baseName + '.pem', 'application/x-pem-file');
    } else {
        downloadAsFile(privateKeyText, baseName, 'application/octet-stream');
    }
}

/**
 * Download the public key from a completed share page.
 * Uses hidden inputs for algorithm, purpose, and format to determine filename.
 */
function downloadSharePublicKey() {
    const publicKey = document.getElementById('public-key-display')?.value;
    const algorithm = document.getElementById('share-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('share-purpose')?.value || 'ssh';
    const format = document.getElementById('share-format')?.value || 'openssh';

    if (!publicKey) {
        console.error('No public key found to download');
        return;
    }

    const baseName = algoBaseName(algorithm, purpose);
    if (format === 'pem') {
        downloadAsFile(publicKey, baseName + '.pub.pem', 'application/x-pem-file');
    } else {
        downloadAsFile(publicKey, baseName + '.pub', 'text/plain');
    }
}
