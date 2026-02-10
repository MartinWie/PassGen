function clearPrivateKey() {
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

    // Show a toast notification
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
        var params = {};
        if (languageSelect) params['language-select'] = languageSelect.value;
        if (wordAmountSlider) params['word-amount-slider'] = wordAmountSlider.value;
        if (includeSpecial && includeSpecial.checked) params['include-special'] = 'on';
        if (includeNumbers && includeNumbers.checked) params['include-numbers'] = 'on';
        if (wordSeparator) params['separator'] = wordSeparator.value;
        htmx.ajax('GET', '/word', {target: '#password-input', swap: 'outerHTML', values: params});
    }
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

    // Initialize theme from localStorage or system preference
    initTheme();
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
    // Naming aligned to common OpenSSH conventions
    if (purpose === 'git' || purpose === 'ssh') {
        if (algo === 'ed25519') return purpose === 'git' ? 'id_ed25519_signing' : 'id_ed25519';
        if (algo.startsWith('ecdsa')) return purpose === 'git' ? 'id_ecdsa_signing' : 'id_ecdsa';
        if (algo.startsWith('rsa-')) return purpose === 'git' ? 'id_rsa_signing' : 'id_rsa';
    }
    return 'key';
}

function downloadKey(elementId) {
    const algo = document.getElementById('key-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('key-purpose')?.value || 'ssh';
    const isPublic = elementId.includes('public');
    const baseName = algoBaseName(algo, purpose);
    const filename = isPublic ? baseName + '.pub' : baseName; // keep no extension for private
    const text = document.getElementById(elementId).value;
    // Use octet-stream for private key without extension to avoid some browsers (Safari) appending .txt
    const mime = isPublic ? 'text/plain' : 'application/octet-stream';
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
    const wrapped = b64.replace(/(.{64})/g, '$1\n');
    return `-----BEGIN ${label}-----\n${wrapped}\n-----END ${label}-----`;
}

function buildOpenSSHPrivateKeyEd25519(secretKey64, publicKey32, comment) {
    // secretKey64 is 64-byte Uint8Array (seed+pub), publicKey32 is 32-byte Uint8Array
    // Format: https://github.com/openssh/openssh-portable/blob/master/PROTOCOL.key
    function writeString(arr, str) {
        const enc = new TextEncoder();
        const bytes = enc.encode(str);
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        arr.push(len, bytes);
    }

    function writeBytes(arr, bytes) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        arr.push(len, bytes);
    }

    const algorithm = 'ssh-ed25519';
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    const parts = [magic];
    // ciphername, kdfname, kdfoptions (none)
    writeString(parts, 'none');
    writeString(parts, 'none');
    writeString(parts, '');
    // number of keys (uint32 =1)
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    parts.push(one);
    // public key (wire format)
    // wire: string alg, string pubkey
    const pubWireParts = [];
    writeString(pubWireParts, algorithm);
    writeBytes(pubWireParts, publicKey32);
    const pubWire = pubWireParts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    // length-prefixed public wire
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pubWire.length);
    parts.push(pubLen, pubWire);

    // Private block
    const privParts = [];
    const check = crypto.getRandomValues(new Uint8Array(4));
    privParts.push(check, check.slice()); // two identical check ints
    // key wire inside private: string alg, string pubkey, string priv(64 bytes), string comment
    writeString(privParts, algorithm);
    writeBytes(privParts, publicKey32);
    writeBytes(privParts, secretKey64);
    writeString(privParts, comment || '');
    // padding 1..n
    let totalLen = privParts.reduce((s, b) => s + b.length, 0);
    const padNeeded = (8 - (totalLen % 8)) % 8; // minimal to 8-byte block alignment
    for (let i = 1; i <= padNeeded; i++) {
        privParts.push(new Uint8Array([i]));
    }
    const privBlock = privParts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    // length of private block
    const privLen = new Uint8Array(4);
    new DataView(privLen.buffer).setUint32(0, privBlock.length);
    parts.push(privLen, privBlock);

    const full = parts.reduce((acc, cur) => {
        const tmp = new Uint8Array(acc.length + cur.length);
        tmp.set(acc, 0);
        tmp.set(cur, acc.length);
        return tmp;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
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

function buildOpenSSHPrivateKeyECDSA(curveName, publicPoint, privateScalar, comment) {
    // curveName like nistp256 etc.
    const algo = 'ecdsa-sha2-' + curveName;

    function writeString(arr, data) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, data.length);
        arr.push(len, data);
    }

    function writeText(arr, text) {
        writeString(arr, new TextEncoder().encode(text));
    }

    function mpintRaw(bytes) { // mpint without outer length (we will add length as bignum2 per spec)
        // strip leading zeros
        let i = 0;
        while (i < bytes.length - 1 && bytes[i] === 0) i++;
        bytes = bytes.slice(i);
        if (bytes.length && (bytes[0] & 0x80)) {
            const withZero = new Uint8Array(bytes.length + 1);
            withZero[0] = 0;
            withZero.set(bytes, 1);
            bytes = withZero;
        }
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        return [len, bytes];
    }

    // Public key blob: string alg, string curve, string point
    const pointBlob = (() => {
        const parts = [];
        writeText(parts, algo);
        writeText(parts, curveName);
        writeString(parts, publicPoint);
        return parts.reduce((a, c) => {
            const t = new Uint8Array(a.length + c.length);
            t.set(a);
            t.set(c, a.length);
            return t;
        }, new Uint8Array());
    })();

    // Private block assembly similar to Ed25519 method
    const blocks = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    blocks.push(magic);
    writeText(blocks, 'none');
    writeText(blocks, 'none');
    writeText(blocks, '');
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    blocks.push(one);
    // public key (length + blob)
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pointBlob.length);
    blocks.push(pubLen, pointBlob);
    // private section
    const check = crypto.getRandomValues(new Uint8Array(4));
    const privParts = [];
    privParts.push(check, check.slice());
    writeText(privParts, algo);
    writeText(privParts, curveName);
    writeString(privParts, publicPoint); // public point
    // private scalar mpint
    const scalar = base64UrlToBytes(privateScalar); // JWK d is base64url
    const mp = mpintRaw(scalar);
    privParts.push(...mp);
    writeText(privParts, comment || '');
    // padding to 8
    let privLenCount = privParts.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) privParts.push(new Uint8Array([i]));
    const privBlock = privParts.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const privLenBuf = new Uint8Array(4);
    new DataView(privLenBuf.buffer).setUint32(0, privBlock.length);
    blocks.push(privLenBuf, privBlock);
    const full = blocks.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
}

function buildOpenSSHPrivateKeyRSA(jwk, comment) {
    const algo = 'ssh-rsa';
    const n = base64UrlToBytes(jwk.n), e = base64UrlToBytes(jwk.e), d = base64UrlToBytes(jwk.d),
        p = base64UrlToBytes(jwk.p), q = base64UrlToBytes(jwk.q), iqmp = base64UrlToBytes(jwk.qi);

    function writeString(arr, data) {
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, data.length);
        arr.push(len, data);
    }

    function writeText(arr, text) {
        writeString(arr, new TextEncoder().encode(text));
    }

    function mpintFull(bytes) {
        let i = 0;
        while (i < bytes.length - 1 && bytes[i] === 0) i++;
        bytes = bytes.slice(i);
        if (bytes.length && (bytes[0] & 0x80)) {
            const withZero = new Uint8Array(bytes.length + 1);
            withZero[0] = 0;
            withZero.set(bytes, 1);
            bytes = withZero;
        }
        const len = new Uint8Array(4);
        new DataView(len.buffer).setUint32(0, bytes.length);
        return [len, bytes];
    }

    const pubBlobParts = [];
    writeText(pubBlobParts, algo);
    pubBlobParts.push(...mpintFull(e));
    pubBlobParts.push(...mpintFull(n));
    const pubBlob = pubBlobParts.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const sections = [];
    const magic = new TextEncoder().encode('openssh-key-v1\0');
    sections.push(magic);
    writeText(sections, 'none');
    writeText(sections, 'none');
    writeText(sections, '');
    const one = new Uint8Array(4);
    new DataView(one.buffer).setUint32(0, 1);
    sections.push(one);
    const pubLen = new Uint8Array(4);
    new DataView(pubLen.buffer).setUint32(0, pubBlob.length);
    sections.push(pubLen, pubBlob);
    const check = crypto.getRandomValues(new Uint8Array(4));
    const priv = [];
    priv.push(check, check.slice());
    writeText(priv, algo);
    // key components: n,e,d,iqmp,p,q
    [n, e, d, iqmp, p, q].forEach(b => {
        const pair = mpintFull(b);
        priv.push(...pair);
    });
    writeText(priv, comment || '');
    let privLenCount = priv.reduce((s, b) => s + b.length, 0);
    const pad = (8 - (privLenCount % 8)) % 8;
    for (let i = 1; i <= pad; i++) priv.push(new Uint8Array([i]));
    const privBlock = priv.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const privLenBuf = new Uint8Array(4);
    new DataView(privLenBuf.buffer).setUint32(0, privBlock.length);
    sections.push(privLenBuf, privBlock);
    const full = sections.reduce((a, c) => {
        const t = new Uint8Array(a.length + c.length);
        t.set(a);
        t.set(c, a.length);
        return t;
    }, new Uint8Array());
    const b64 = bytesToBase64(full).replace(/(.{70})/g, '$1\n');
    return `-----BEGIN OPENSSH PRIVATE KEY-----\n${b64}\n-----END OPENSSH PRIVATE KEY-----`;
}

async function generateKey() {
    const algo = document.getElementById('key-algorithm').value;
    const purpose = document.getElementById('key-purpose').value;
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

    // Check for secure context requirement for ECDSA/RSA
    if (algo !== 'ed25519' && !window.isSecureContext) {
        errText.textContent = 'ECDSA and RSA require HTTPS. Use Ed25519 or access via HTTPS.';
        errAlert.classList.remove('hidden');
        return;
    }

    // Check crypto.subtle availability
    if (algo !== 'ed25519' && (!window.crypto || !window.crypto.subtle)) {
        errText.textContent = 'Web Crypto API not available. Use Ed25519 or a modern browser with HTTPS.';
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
        let publicKeyText = '';
        let privateKeyText = '';
        if (algo === 'ed25519') {
            if (!window.nacl || !nacl.sign) throw new Error('TweetNaCl library not loaded. Please refresh the page.');
            const kp = nacl.sign.keyPair();
            const pub = kp.publicKey; // 32 bytes
            const sec = kp.secretKey; // 64 bytes (seed+pub)
            sensitiveBuffers.push(sec); // Track for cleanup
            const blob = buildSshBufferEd25519(pub);
            publicKeyText = 'ssh-ed25519 ' + bytesToBase64(blob) + (identifier ? ' ' + identifier : '');
            privateKeyText = buildOpenSSHPrivateKeyEd25519(sec, pub, identifier);
        } else if (algo.startsWith('ecdsa-')) {
            const curveMap = {'ecdsa-p256': 'nistp256', 'ecdsa-p384': 'nistp384', 'ecdsa-p521': 'nistp521'};
            const named = curveMap[algo];
            if (!named) throw new Error('Unsupported ECDSA curve');
            const webCurve = {nistp256: 'P-256', nistp384: 'P-384', nistp521: 'P-521'}[named];
            const keyPair = await crypto.subtle.generateKey({
                name: 'ECDSA',
                namedCurve: webCurve
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            const x = base64UrlToBytes(jwkPub.x);
            const y = base64UrlToBytes(jwkPub.y);
            const point = new Uint8Array(1 + x.length + y.length);
            point[0] = 0x04;
            point.set(x, 1);
            point.set(y, 1 + x.length);
            // Track private scalar for cleanup (note: buildOpenSSHPrivateKeyECDSA internally 
            // converts jwkPriv.d to bytes again; we track our copy here for cleanup attempt,
            // but JS string immutability means the original string cannot be truly erased)
            const dBytes = base64UrlToBytes(jwkPriv.d);
            sensitiveBuffers.push(dBytes);
            publicKeyText = buildOpenSshEcdsaPublic(jwkPub, named, identifier);
            privateKeyText = buildOpenSSHPrivateKeyECDSA(named, point, jwkPriv.d, identifier);
            // Clear JWK private fields (best effort - strings are immutable in JS)
            jwkPriv.d = '';
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSASSA-PKCS1-v1_5',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            // Track RSA private components for cleanup
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => {
                if (jwkPriv[field]) {
                    sensitiveBuffers.push(base64UrlToBytes(jwkPriv[field]));
                }
            });
            publicKeyText = buildOpenSshRsaPublic(jwkPub, identifier);
            privateKeyText = buildOpenSSHPrivateKeyRSA(jwkPriv, identifier);
            // Clear JWK private fields (best effort - strings are immutable in JS)
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => {
                jwkPriv[field] = '';
            });
        } else {
            throw new Error('Unknown algorithm');
        }
        pubOut.value = publicKeyText;
        privOut.value = privateKeyText;

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
                'rsa-4096': 'RSA 4096'
            };
            keyTypeDisplay.textContent = algoNames[algo] || algo;
        }

        // Update library info tooltip
        const libraryTooltip = document.getElementById('key-library-tooltip');
        if (libraryTooltip) {
            const libraryInfo = algo === 'ed25519'
                ? 'Generated using TweetNaCl library'
                : 'Generated using Web Crypto API';
            libraryTooltip.setAttribute('data-tip', libraryInfo);
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
        // Log detailed error to console for debugging
        console.error('Key generation failed:', e);

        let userMessage;
        // Check for specific error types and provide helpful messages
        if (e.message && e.message.includes('TweetNaCl')) {
            userMessage = e.message;
        } else if (e.message === 'Unsupported ECDSA curve' || e.message === 'Unknown algorithm') {
            userMessage = e.message;
        } else if (e.name === 'NotSupportedError') {
            userMessage = 'This algorithm is not supported by your browser.';
        } else if (e.name === 'OperationError') {
            userMessage = 'Key generation operation failed. Please try again.';
        } else if (!window.isSecureContext) {
            userMessage = 'Secure context required. Please use HTTPS or try Ed25519.';
        } else {
            userMessage = 'Key generation failed: ' + (e.message || 'Unknown error');
        }
        errText.textContent = userMessage;
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
        p1.textContent = '1. Append Public Key to ~/.ssh/authorized_keys.';
        el.appendChild(p1);
        const p2 = document.createElement('p');
        p2.textContent = `2. Save Private Key as ${baseName}; chmod 600 ${baseName}.`;
        el.appendChild(p2);
        const p3 = document.createElement('p');
        p3.textContent = `3. Use: ssh -i ${baseName} user@host.`;
        el.appendChild(p3);
    } else {
        if (algo === 'ed25519') {
            const intro = document.createElement('p');
            intro.textContent = 'Git SSH signing (Ed25519 OpenSSH key):';
            el.appendChild(intro);
            const ol = document.createElement('ol');
            ol.className = 'list-decimal list-inside';
            const steps = [
                `Save private key: ${baseName}`,
                `Public key: ${baseName}.pub → add as Public key.`,
                'git config --global gpg.format ssh',
                `git config --global user.signingkey ${baseName}`,
                '(Optional) git config --global commit.gpgsign true',
                'Sign commits: git commit -S -m "msg"'
            ];
            steps.forEach(t => {
                const li = document.createElement('li');
                li.textContent = t;
                ol.appendChild(li);
            });
            el.appendChild(ol);
        } else {
            const p = document.createElement('p');
            const code = document.createElement('code');
            code.textContent = algo;
            p.append(
                'For Git SSH signing, Ed25519 is strongly recommended. Current algorithm ',
                code,
                ' is usable for SSH auth but not ideal for signing portability.'
            );
            el.appendChild(p);
        }
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
    // Share key button now uses HTMX - no JavaScript handler needed
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

    // Check for secure context requirement for ECDSA/RSA
    if (algo !== 'ed25519' && !window.isSecureContext) {
        if (errText) errText.textContent = 'ECDSA and RSA require HTTPS. Please access via HTTPS.';
        if (errAlert) errAlert.classList.remove('hidden');
        return;
    }

    // Check crypto.subtle availability
    if (algo !== 'ed25519' && (!window.crypto || !window.crypto.subtle)) {
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
        let publicKeyText = '';
        let privateKeyText = '';
        const comment = label || '';

        if (algo === 'ed25519') {
            if (!window.nacl || !nacl.sign) throw new Error('TweetNaCl library not loaded. Please refresh the page.');
            const kp = nacl.sign.keyPair();
            const pub = kp.publicKey;
            const sec = kp.secretKey;
            sensitiveBuffers.push(sec);
            const blob = buildSshBufferEd25519(pub);
            publicKeyText = 'ssh-ed25519 ' + bytesToBase64(blob) + (comment ? ' ' + comment : '');
            privateKeyText = buildOpenSSHPrivateKeyEd25519(sec, pub, comment);
        } else if (algo.startsWith('ecdsa-')) {
            const curveMap = {'ecdsa-p256': 'nistp256', 'ecdsa-p384': 'nistp384', 'ecdsa-p521': 'nistp521'};
            const named = curveMap[algo];
            if (!named) throw new Error('Unsupported ECDSA curve');
            const webCurve = {nistp256: 'P-256', nistp384: 'P-384', nistp521: 'P-521'}[named];
            const keyPair = await crypto.subtle.generateKey({
                name: 'ECDSA',
                namedCurve: webCurve
            }, true, ['sign', 'verify']);
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
            privateKeyText = buildOpenSSHPrivateKeyECDSA(named, point, jwkPriv.d, comment);
            jwkPriv.d = '';
        } else if (algo.startsWith('rsa-')) {
            const size = parseInt(algo.split('-')[1], 10);
            const keyPair = await crypto.subtle.generateKey({
                name: 'RSASSA-PKCS1-v1_5',
                modulusLength: size,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: 'SHA-256'
            }, true, ['sign', 'verify']);
            const jwkPub = await crypto.subtle.exportKey('jwk', keyPair.publicKey);
            const jwkPriv = await crypto.subtle.exportKey('jwk', keyPair.privateKey);
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => {
                if (jwkPriv[field]) {
                    sensitiveBuffers.push(base64UrlToBytes(jwkPriv[field]));
                }
            });
            publicKeyText = buildOpenSshRsaPublic(jwkPub, comment);
            privateKeyText = buildOpenSSHPrivateKeyRSA(jwkPriv, comment);
            ['d', 'p', 'q', 'dp', 'dq', 'qi'].forEach(field => {
                jwkPriv[field] = '';
            });
        } else {
            throw new Error('Unknown algorithm');
        }

        // Download the private key automatically
        downloadSharePrivateKey(privateKeyText, algo, purpose);

        // Clear private key from memory as soon as possible
        privateKeyText = '';

        // POST the public key to the server
        const formData = new URLSearchParams();
        formData.append('public-key', publicKeyText);
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

        let userMessage;
        if (e.message && e.message.includes('TweetNaCl')) {
            userMessage = e.message;
        } else if (e.message === 'Unsupported ECDSA curve' || e.message === 'Unknown algorithm') {
            userMessage = e.message;
        } else if (e.name === 'NotSupportedError') {
            userMessage = 'This algorithm is not supported by your browser.';
        } else if (e.name === 'OperationError') {
            userMessage = 'Key generation operation failed. Please try again.';
        } else if (!window.isSecureContext) {
            userMessage = 'Secure context required. Please use HTTPS.';
        } else {
            userMessage = 'Key generation failed: ' + (e.message || 'Unknown error');
        }
        if (errText) errText.textContent = userMessage;
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
function downloadSharePrivateKey(privateKeyText, algo, purpose) {
    const baseName = algoBaseName(algo, purpose);
    downloadAsFile(privateKeyText, baseName, 'application/octet-stream');
}

/**
 * Download the public key from a completed share page.
 * Uses hidden inputs for algorithm and purpose to determine filename.
 */
function downloadSharePublicKey() {
    const publicKey = document.getElementById('public-key-display')?.value;
    const algorithm = document.getElementById('share-algorithm')?.value || 'ed25519';
    const purpose = document.getElementById('share-purpose')?.value || 'ssh';

    if (!publicKey) {
        console.error('No public key found to download');
        return;
    }

    const baseName = algoBaseName(algorithm, purpose);
    downloadAsFile(publicKey, baseName + '.pub', 'text/plain');
}
